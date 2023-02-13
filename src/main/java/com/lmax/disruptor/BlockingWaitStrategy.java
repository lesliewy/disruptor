/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor;

/**
 * Blocking strategy that uses a lock and condition variable for {@link EventProcessor}s waiting on a barrier.

 *
 * <p>This strategy can be used when throughput and low-latency are not as important as CPU resource.
 */
public final class BlockingWaitStrategy implements WaitStrategy
{
    private final Object mutex = new Object();

    @Override
    public long waitFor(final long sequence, final Sequence cursorSequence, final Sequence dependentSequence, final SequenceBarrier barrier)
        throws AlertException, InterruptedException
    {
        long availableSequence;
        /**
         * cursorSequence:生产者的序号
         * 第一重条件判断：如果消费者消费速度，大于生产者生产速度（即消费者要消费的下一个数据已经大于生产者生产的数据时），那么消费者等待一下
         */
        if (cursorSequence.get() < sequence)
        {
            synchronized (mutex)
            {
                while (cursorSequence.get() < sequence)
                {
                    barrier.checkAlert();
                    mutex.wait();
                }
            }
        }

        /**
         * 第二重条件判断：自旋等待
         * 即当前消费者线程要消费的下一个sequence，大于其前面执行链路（若有依赖关系）的任何一个消费者最小sequence（dependentSequence.get()），
         * 那么这个消费者要自旋等待，直到前面执行链路（若有依赖关系）的任何一个消费者最小sequence（dependentSequence.get()）已经大于等于当前消费者的sequence时，
         * 说明前面执行链路的消费者已经消费完了
         */
        while ((availableSequence = dependentSequence.get()) < sequence)
        {
            barrier.checkAlert();
            Thread.onSpinWait();
        }

        return availableSequence;
    }

    /**
     * 如果生产者新生产一个元素，那么唤醒所有消费者
     */
    @Override
    public void signalAllWhenBlocking()
    {
        synchronized (mutex)
        {
            mutex.notifyAll();
        }
    }

    @Override
    public String toString()
    {
        return "BlockingWaitStrategy{" +
            "mutex=" + mutex +
            '}';
    }
}
