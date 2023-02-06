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
 * 多组消费者相互隔离场景: 1和2消费同一事件；  1-1, 1-2竞争消费，而不是串行消费.
 *                    consumer1-1  consumer1-2
 * producer - event -
 *                    consumer2-1  consumer2-2
 * disruptor.handleEventsWithWorkerPool: 4.0版本没有这个方法.
 */
public class Disruptor6
{
    public static void main(String[] args) throws InterruptedException
    {
        int bufferSize = 1024;
        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
//        disruptor.handleEventsWith(new LongEventHandler5A()).then(new LongEventHandler5B());
//        disruptor.handleEventsWith(new LongEventHandler5C()).then(new LongEventHandler5D());
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

class LongEventHandler6A implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler6A " + " Sequence: " + sequence + " Event: " + event);
    }
}

class LongEventHandler6B implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler6B " + " Sequence: " + sequence + " Event: " + event);
    }
}

class LongEventHandler6C implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler6C " + " Sequence: " + sequence + " Event: " + event);
    }
}

class LongEventHandler6D implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler6D " + " Sequence: " + sequence + " Event: " + event);
    }
}
