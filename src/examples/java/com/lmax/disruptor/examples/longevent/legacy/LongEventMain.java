package com.lmax.disruptor.examples.longevent.legacy;

// tag::example[]
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.examples.longevent.LongEvent;
import com.lmax.disruptor.examples.longevent.LongEventFactory;
import com.lmax.disruptor.examples.longevent.LongEventHandler;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.nio.ByteBuffer;

public class LongEventMain
{
    public static void main(String[] args) throws Exception
    {
        /**
         * 需要让Disruptor为我们创建事件，我们同时还声明了一个EventFactory来创建Event对象。
         */
        LongEventFactory factory = new LongEventFactory();

        int bufferSize = 1024;
        /**  构造disruptor （事件分发者） */
        Disruptor<LongEvent> disruptor =
                new Disruptor<>(factory, bufferSize, DaemonThreadFactory.INSTANCE);
        /**
         * 连接 消费者 处理器, LongEventHandler类似于消费者, disruptor会回调此处理器的方法
         */
        disruptor.handleEventsWith(new LongEventHandler());
        /** 开启disruptor */
        disruptor.start();

        /**
         * 通过从 环形队列中 获取 序号， 通过序号获取 对应的 事件对象， 将数据填充到 事件对象，再通过 序号将 事件对象 发布出去。
         */
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        LongEventProducer producer = new LongEventProducer(ringBuffer);
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; true; l++)
        {
            bb.putLong(0, l);
            producer.onData(bb);
            Thread.sleep(1000);
        }
    }
}
// end::example[]