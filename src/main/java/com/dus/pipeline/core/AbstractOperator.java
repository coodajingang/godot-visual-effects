package com.dus.pipeline.core;

import com.dus.pipeline.context.PipelineContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象算子基类，支持 Context 注入和生命周期钩子
 */
public abstract class AbstractOperator<I, O> implements Operator<I, O>, ContextAware {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected PipelineContext context;
    
    @Override
    public void setContext(PipelineContext context) {
        this.context = context;
    }
    
    @Override
    public final O process(I input) {
        before(input);
        O output = doProcess(input);
        after(input, output);
        return output;
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
     * 处理前置钩子
     */
    protected void before(I input) {
        // 默认空实现，子类可重写
    }
    
    /**
     * 核心处理逻辑，子类必须实现
     */
    protected abstract O doProcess(I input);
    
    /**
     * 处理后置钩子
     */
    protected void after(I input, O output) {
        // 默认空实现，子类可重写
    }
    
    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }
}