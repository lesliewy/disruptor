package com.lmax.disruptor.examples.wy;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.examples.longevent.LongEvent;
import com.lmax.disruptor.examples.longevent.LongEventProducer;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.nio.ByteBuffer;

/**
 * 单生产者多消费者,消费者之间菱形: 消费者1,2并行， 最后消费者3.
 *                    consumer1
 * producer - event -            - consumer3
 *                    consumer2
 */
public class Disruptor4
{
    public static void main(String[] args) throws InterruptedException
    {
        int bufferSize = 1024;
        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        /** 4A, 4B都会消费事件, 并行执行，都完了最后传递给4C.*/
        disruptor.handleEventsWith(new LongEventHandler4A(), new LongEventHandler4B()).then(new LongEventHandler4C());
        disruptor.start();

        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducer producer = new LongEventProducer(ringBuffer);
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long i = 0; true; i++)
        {
            bb.putLong(0, i);
            producer.onData(bb);
            Thread.sleep(1000);
        }
    }
}

class LongEventHandler4A implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler4A " + " Sequence: " + sequence + " Event: " + event);
    }
}

class LongEventHandler4B implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler4B " + " Sequence: " + sequence + " Event: " + event);
    }
}

class LongEventHandler4C implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler4C " + " Sequence: " + sequence + " Event: " + event);
    }
}
