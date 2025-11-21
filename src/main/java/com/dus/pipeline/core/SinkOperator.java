package com.dus.pipeline.core;

/**
 * 数据写入算子抽象类，继承自AbstractOperator
 * 定义了数据写入的标准流程，输出类型为Void
 * 
 * @param <I> 输入数据类型
 * @author Dus
 * @version 1.0
 */
public abstract class SinkOperator<I> extends AbstractOperator<I, Void> {
    
    /**
     * 模板方法，定义数据写入的标准流程
     * 调用具体的写入逻辑
     * 
     * @param input 需要写入的数据
     * @return null（写入操作无返回值）
     * @throws Exception 写入过程中可能抛出的异常
     */
    @Override
    protected final Void doProcess(I input) throws Exception {
        write(input);
        return null;
    }
    
    /**
     * 子类必须实现的具体写入逻辑
     * 
     * @param input 需要写入的数据
     * @throws Exception 写入过程中可能抛出的异常
     */
    protected abstract void write(I input) throws Exception;
}