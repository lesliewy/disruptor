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
 * 单生产者多消费者,消费者之间链内串行，链间并行:
 *                    consumer1-1 - consumer1-2
 * producer - event -
 *                    consumer2-1 - consumer2-2
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

class LongEventHandler5A implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler5A " + " Sequence: " + sequence + " Event: " + event);
    }
}

class LongEventHandler5B implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler5B " + " Sequence: " + sequence + " Event: " + event);
    }
}

class LongEventHandler5C implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler5C " + " Sequence: " + sequence + " Event: " + event);
    }
}

class LongEventHandler5D implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("LongEventHandler5D " + " Sequence: " + sequence + " Event: " + event);
    }
}
