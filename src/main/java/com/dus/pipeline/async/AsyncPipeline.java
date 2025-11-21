package com.dus.pipeline.async;

import com.dus.pipeline.core.Pipeline;
import com.dus.pipeline.core.SourceOperator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 异步管道类
 * 支持异步执行的管道实现
 * 
 * @param <I> 初始输入数据类型
 * @param <O> 最终输出数据类型
 * @author Dus
 * @version 1.0
 */
public class AsyncPipeline<I, O> extends Pipeline<I, O> {
    
    private volatile boolean isShutdown = false;
    
    /**
     * 构造函数
     * 
     * @param source 数据源算子
     * @throws IllegalArgumentException 如果source为null
     */
    public AsyncPipeline(SourceOperator<I> source) {
        super(source);
    }
    
    /**
     * 异步启动管道执行
     * 
     * @return 包含执行结果的CompletableFuture
     */
    public CompletableFuture<Void> runAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                run();
            } catch (Exception e) {
                throw new RuntimeException("Pipeline execution failed", e);
            }
        });
    }
    
    /**
     * 关闭管道
     */
    public void shutdown() {
        isShutdown = true;
    }
    
    /**
     * 等待管道执行完成
     * 
     * @param timeoutMillis 超时时间（毫秒）
     * @return 如果在超时时间内完成返回true，否则返回false
     */
    public boolean awaitTermination(long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        while (!isShutdown && (System.currentTimeMillis() - startTime) < timeoutMillis) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return isShutdown;
    }
}