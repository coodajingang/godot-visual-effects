package com.dus.pipeline.core;

import com.dus.pipeline.context.PipelineContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 源算子抽象类，负责产生数据
 */
public abstract class SourceOperator<O> implements ContextAware {
    
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
     * 获取下一批数据
     */
    public final O nextBatch() {
        before();
        O batch = doNextBatch();
        after(batch);
        
        // 更新批次计数
        if (context != null) {
            context.incrementBatchCount();
        }
        
        return batch;
    }
    
    /**
     * 检查是否还有更多数据
     */
    public abstract boolean hasNext();
    
    /**
     * 核心数据获取逻辑，子类必须实现
     */
    protected abstract O doNextBatch();
    
    /**
     * 获取数据前置钩子
     */
    protected void before() {
        // 默认空实现，子类可重写
    }
    
    /**
     * 获取数据后置钩子
     */
    protected void after(O batch) {
        // 默认空实现，子类可重写
    }
    
    /**
     * 获取算子名称
     */
    public String name() {
        return this.getClass().getSimpleName();
    }
}