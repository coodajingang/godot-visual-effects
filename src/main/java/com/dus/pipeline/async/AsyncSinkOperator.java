package com.dus.pipeline.async;

import java.util.concurrent.CompletableFuture;

/**
 * 异步写入算子抽象类
 * 继承自 AsyncOperator，定义了异步写入数据的标准流程
 *
 * @param <I> 输入数据类型
 * @author Dus
 * @version 1.0
 */
public abstract class AsyncSinkOperator<I> extends AsyncOperator<I, Void> {

    /**
     * 异步写入核心方法，子类必须实现
     * 定义具体的写入逻辑
     *
     * @param input 需要写入的数据
     * @return 表示写入完成的 CompletableFuture
     */
    protected abstract CompletableFuture<Void> writeAsync(I input);

    /**
     * 异步处理实现
     * 调用 writeAsync 进行实际写入
     *
     * @param input 需要写入的数据
     * @return 表示写入完成的 CompletableFuture
     */
    @Override
    public final CompletableFuture<Void> processAsync(I input) {
        return writeAsync(input);
    }
}
