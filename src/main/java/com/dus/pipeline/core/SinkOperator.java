package com.dus.pipeline.core;

/**
 * 输出算子抽象类，负责消费数据
 */
public abstract class SinkOperator<I> extends AbstractOperator<I, Void> {
    
    @Override
    protected final Void doProcess(I input) {
        write(input);
        return null;
    }
    
    /**
     * 写入数据的具体实现，子类必须实现
     */
    protected abstract void write(I input);
}