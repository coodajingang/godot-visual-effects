package com.dus.pipeline.core;

import com.dus.pipeline.context.PipelineContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * 异步算子抽象类，支持异步处理
 */
public abstract class AsyncOperator<I, O> implements Operator<I, O>, ContextAware {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected PipelineContext context;
    
    @Override
    public void setContext(PipelineContext context) {
        this.context = context;
    }
    
    /**
     * 获取当前 Context
     */
    protected PipelineContext getContext() {
        return context;
    }
    
    /**
     * 获取 Context 中的属性（便捷方法）
     */
    protected Object getContextProperty(String key) {
        return context != null ? context.getProperty(key) : null;
    }
    
    /**
     * 获取 Context 中的属性（便捷方法，带默认值）
     */
    @SuppressWarnings("unchecked")
    protected <T> T getContextProperty(String key, T defaultValue) {
        return context != null ? context.getProperty(key, defaultValue) : defaultValue;
    }
    
    /**
     * 设置 Context 中的属性（便捷方法）
     */
    protected void setContextProperty(String key, Object value) {
        if (context != null) {
            context.setProperty(key, value);
        }
    }
    
    /**
     * 异步处理输入数据
     */
    public abstract CompletableFuture<O> processAsync(I input);
    
    @Override
    public O process(I input) {
        return processAsync(input).join();
    }
    
    /**
     * 获取执行器，子类可重写以使用自定义执行器
     */
    protected Executor getExecutor() {
        return ForkJoinPool.commonPool();
    }
    
    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }
}