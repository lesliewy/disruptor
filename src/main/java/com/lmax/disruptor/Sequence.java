package com.lmax.disruptor;


import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

class LhsPadding
{
    protected byte
        p10, p11, p12, p13, p14, p15, p16, p17,
        p20, p21, p22, p23, p24, p25, p26, p27,
        p30, p31, p32, p33, p34, p35, p36, p37,
        p40, p41, p42, p43, p44, p45, p46, p47,
        p50, p51, p52, p53, p54, p55, p56, p57,
        p60, p61, p62, p63, p64, p65, p66, p67,
        p70, p71, p72, p73, p74, p75, p76, p77;
}

class Value extends LhsPadding
{
    protected long value;
}

class RhsPadding extends Value
{
    protected byte
        p90, p91, p92, p93, p94, p95, p96, p97,
        p100, p101, p102, p103, p104, p105, p106, p107,
        p110, p111, p112, p113, p114, p115, p116, p117,
        p120, p121, p122, p123, p124, p125, p126, p127,
        p130, p131, p132, p133, p134, p135, p136, p137,
        p140, p141, p142, p143, p144, p145, p146, p147,
        p150, p151, p152, p153, p154, p155, p156, p157;
}

/**
 * Concurrent sequence class used for tracking the progress of
 * the ring buffer and event processors.  Support a number
 * of concurrent operations including CAS and order writes.
 *
 * <p>Also attempts to be more efficient with regards to false
 * sharing by adding padding around the volatile field.
 */

/**
 * Sequence作用:
 *   在RingBuffer缓冲区中，Sequence标示着写入进度，例如每次生产者要写入数据进缓冲区时，都要调用RingBuffer.next()来获得下一个可使用的相对位置。
 *   对于生产者和消费者来说，Sequence标示着它们的事件序号
 *
 * 序号Sequence需要满足以下条件:
 * 条件一: 消费者的序号Sequence的数值必须小于生产者的序号Sequence的数值
 * 条件二: 消费者的序号Sequence的数值必须小于依赖关系中前置的消费者的序号Sequence的数值
 * 条件三: 生产者的序号Sequence的数值不能大于消费者正在消费的序号Sequence的数值,防止生产者速度过快,将还没有来得及消费的事件消息覆盖
 * 条件一和条件二在SequenceBarrier中的waitFor() 方法中实现:
 * 条件三是针对生产者建立的SequenceBarrier,逻辑判定发生在生产者从RingBuffer获取下一个可用的entry时,RingBuffer会将获取下一个可用的entry委托给Sequencer处理:
 *
 * Sequence是如何消除伪共享: 真正使用到的变量是Value类的value，它的前后空间都由8个long型的变量填补了，对于一个大小为64字节的缓存行，它刚好被填补满（一个long型变量value，
 * 8个字节加上前/后个7long型变量填补，7*8=56，56+8=64字节）。
 * 这样做每次把变量value读进高速缓存中时，都能把缓存行填充满，保证每次处理数据时都不会与其他变量发生冲突。
 * 当然，对于大小为64个字节的缓存行来说，如果缓存行大小大于64个字节，那么还是会出现伪共享问题，但是毕竟非64个字节的Cache Line并不是当前的主流。
 *
 */
public class Sequence extends RhsPadding
{
    static final long INITIAL_VALUE = -1L;
    /** jdk 17 新增， 之前版本使用的是 Unsafe */
    private static final VarHandle VALUE_FIELD;

    static
    {
        try
        {
            VALUE_FIELD = MethodHandles.lookup().in(Sequence.class)
                    .findVarHandle(Sequence.class, "value", long.class);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a sequence initialised to -1.
     */
    public Sequence()
    {
        this(INITIAL_VALUE);
    }

    /**
     * Create a sequence with a specified initial value.
     *
     * @param initialValue The initial value for this sequence.
     */
    public Sequence(final long initialValue)
    {
        VarHandle.releaseFence();
        this.value = initialValue;
    }

    /**
     * Perform a volatile read of this sequence's value.
     *
     * @return The current value of the sequence.
     */
    public long get()
    {
        long value = this.value;
        VarHandle.acquireFence();
        return value;
    }

    /**
     * Perform an ordered write of this sequence.  The intent is
     * a Store/Store barrier between this write and any previous
     * store.
     *
     * @param value The new value for the sequence.
     */
    public void set(final long value)
    {
        VarHandle.releaseFence();
        this.value = value;
    }

    /**
     * Performs a volatile write of this sequence.  The intent is
     * a Store/Store barrier between this write and any previous
     * write and a Store/Load barrier between this write and any
     * subsequent volatile read.
     *
     * @param value The new value for the sequence.
     */
    public void setVolatile(final long value)
    {
        VarHandle.releaseFence();
        this.value = value;
        VarHandle.fullFence();
    }

    /**
     * Perform a compare and set operation on the sequence.
     *
     * @param expectedValue The expected current value.
     * @param newValue      The value to update to.
     * @return true if the operation succeeds, false otherwise.
     */
    public boolean compareAndSet(final long expectedValue, final long newValue)
    {
        return VALUE_FIELD.compareAndSet(this, expectedValue, newValue);
    }

    /**
     * Atomically increment the sequence by one.
     *
     * @return The value after the increment
     */
    public long incrementAndGet()
    {
        return addAndGet(1);
    }

    /**
     * Atomically add the supplied value.
     *
     * @param increment The value to add to the sequence.
     * @return The value after the increment.
     */
    public long addAndGet(final long increment)
    {
        return (long) VALUE_FIELD.getAndAdd(this, increment) + increment;
    }

    /**
     * Perform an atomic getAndAdd operation on the sequence.
     *
     * @param increment The value to add to the sequence.
     * @return the value before increment
     */
    public long getAndAdd(final long increment)
    {
        return (long) VALUE_FIELD.getAndAdd(this, increment);
    }

    @Override
    public String toString()
    {
        return Long.toString(get());
    }
}