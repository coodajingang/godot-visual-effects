package com.dus.pipeline.async;

import com.dus.pipeline.core.AbstractOperator;
import java.util.concurrent.CompletableFuture;

/**
 * 异步算子抽象类
 * 支持异步处理数据的算子基类
 * 
 * @param <I> 输入数据类型
 * @param <O> 输出数据类型
 * @author Dus
 * @version 1.0
 */
public abstract class AsyncOperator<I, O> extends AbstractOperator<I, O> {
    
    /**
     * 同步处理方法，内部调用异步处理并等待结果
     * 
     * @param input 输入数据
     * @return 处理后的输出数据
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    protected final O doProcess(I input) throws Exception {
        try {
            return processAsync(input).get();
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * 异步处理方法，子类必须实现
     * 
     * @param input 输入数据
     * @return 包含处理结果的CompletableFuture
     */
    protected abstract CompletableFuture<O> processAsync(I input);
}