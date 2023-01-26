package com.lmax.disruptor.examples.longevent.lambdas;

// tag::example[]
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.examples.longevent.LongEvent;
import com.lmax.disruptor.util.DaemonThreadFactory;
import java.nio.ByteBuffer;

/**
 * 大部分Disruptor中的接口都符合Functional Interface的要求（也就是在接口中仅仅有一个方法）.所以在Disruptor中，可以广泛使用Lambda来代替自定义类
 *
 */
public class LongEventMain
{
    public static void main(String[] args) throws Exception
    {
        int bufferSize = 1024; // <1>

        /**
         * 需要让Disruptor为我们创建事件，EventFactory来创建Event对象。 Java 8中方法引用也是一个lambda
         */
        Disruptor<LongEvent> disruptor = // <2>
                new Disruptor<>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);

        /**
         * 这里的function, 类似于消费者, disruptor会回调此处理器的方法
         */
        disruptor.handleEventsWith((event, sequence, endOfBatch) ->
                System.out.println("Event: " + event)); // <3>
        disruptor.start(); // <4>


        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer(); // <5>
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; true; l++)
        {
            bb.putLong(0, l);
            ringBuffer.publishEvent((event, sequence, buffer) -> event.set(buffer.getLong(0)), bb);
            Thread.sleep(1000);
        }
    }
}
// end::example[]