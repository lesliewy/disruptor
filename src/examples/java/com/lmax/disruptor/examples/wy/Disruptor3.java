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
 * 单生产者多消费者,消费者之间串行.
 */
public class Disruptor3
{
    public static void main(String[] args) throws InterruptedException
    {
        int bufferSize = 1024;
        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());
        /** 消费者串行，消费的是同一个事件.*/
        disruptor.handleEventsWith(new LongEventHandler3A()).then(new LongEventHandler3B()).then(new LongEventHandler3C());
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

class LongEventHandler3A implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler3A " + " Sequence: " + sequence + " Event: " + event);
    }
}

class LongEventHandler3B implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler3B " + " Sequence: " + sequence + " Event: " + event);
    }
}

class LongEventHandler3C implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler3C " + " Sequence: " + sequence + " Event: " + event);
    }
}
