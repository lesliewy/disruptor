package com.lmax.disruptor.examples.longevent;

// tag::example[]
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.EventTranslatorOneArg;

import java.nio.ByteBuffer;

public class LongEventProducerWithTranslator
{
    private final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducerWithTranslator(RingBuffer<LongEvent> ringBuffer)
    {
        this.ringBuffer = ringBuffer;
    }

    /**
     * 一个translator可以看做一个事件初始化器，publicEvent方法会调用它填充Event.
     */
    private static final EventTranslatorOneArg<LongEvent, ByteBuffer> TRANSLATOR =
        new EventTranslatorOneArg<LongEvent, ByteBuffer>()
        {
            public void translateTo(LongEvent event, long sequence, ByteBuffer bb)
            {
                event.set(bb.getLong(0));
            }
        };

    public void onData(ByteBuffer bb)
    {
        ringBuffer.publishEvent(TRANSLATOR, bb);
    }
}
// end::example[]