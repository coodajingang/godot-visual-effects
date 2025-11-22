package com.dus.pipeline.async;

import java.util.concurrent.CompletableFuture;

/**
 * 异步数据源算子抽象类
 * 定义了异步获取数据的标准流程
 *
 * @param <O> 输出数据类型
 * @author Dus
 * @version 1.0
 */
public abstract class AsyncSourceOperator<O> {

    /**
     * 异步获取下一批数据
     * 子类必须实现具体的数据源逻辑
     *
     * @return 包含数据批次的 CompletableFuture
     */
    public abstract CompletableFuture<O> nextBatchAsync();

    /**
     * 同步包装方法
     * 通过 join() 阻塞等待异步操作完成
     *
     * @return 数据批次
     */
    public final O nextBatch() {
        return nextBatchAsync().join();
    }

    /**
     * 获取算子名称，默认返回类名
     *
     * @return 算子名称
     */
    public String name() {
        return this.getClass().getSimpleName();
    }
}
