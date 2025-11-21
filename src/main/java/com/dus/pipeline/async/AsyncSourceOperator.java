package com.dus.pipeline.async;

import com.dus.pipeline.core.SourceOperator;
import java.util.concurrent.CompletableFuture;

/**
 * 异步数据源算子抽象类
 * 支持异步获取数据的源算子
 * 
 * @param <O> 输出数据类型
 * @author Dus
 * @version 1.0
 */
public abstract class AsyncSourceOperator<O> extends SourceOperator<O> {
    
    /**
     * 异步获取下一批数据
     * 
     * @return 包含数据批次的CompletableFuture
     */
    protected abstract CompletableFuture<O> doNextBatchAsync();
    
    /**
     * 同步获取下一批数据，内部调用异步方法并等待结果
     * 
     * @return 数据批次
     * @throws Exception 获取过程中可能抛出的异常
     */
    @Override
    protected final O doNextBatch() throws Exception {
        return doNextBatchAsync().get();
    }
}