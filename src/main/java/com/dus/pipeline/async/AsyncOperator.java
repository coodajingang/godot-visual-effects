package com.dus.pipeline.async;

import com.dus.pipeline.core.Operator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * 异步算子抽象类
 * 定义了异步处理的标准流程，返回 CompletableFuture
 * 提供了同步包装方法用于兼容同步 pipeline
 *
 * @param <I> 输入数据类型
 * @param <O> 输出数据类型
 * @author Dus
 * @version 1.0
 */
public abstract class AsyncOperator<I, O> implements Operator<I, O> {

    /**
     * 异步处理核心方法，子类必须实现
     * 返回 CompletableFuture 表示异步操作
     *
     * @param input 输入数据
     * @return 包含处理结果的 CompletableFuture
     */
    public abstract CompletableFuture<O> processAsync(I input);

    /**
     * 获取异步执行器
     * 默认使用 ForkJoinPool.commonPool()
     *
     * @return 执行器
     */
    protected Executor getExecutor() {
        return ForkJoinPool.commonPool();
    }

    /**
     * 同步包装方法
     * 通过 join() 阻塞等待异步操作完成
     * 用于兼容同步 pipeline
     *
     * @param input 输入数据
     * @return 处理结果
     * @throws Exception 如果异步操作中抛出异常
     */
    @Override
    public O process(I input) throws Exception {
        try {
            return processAsync(input).join();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }
}
