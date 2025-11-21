package com.dus.pipeline.hook;

/**
 * Pipeline 生命周期钩子接口
 */
public interface PipelineHook {
    
    /**
     * 钩子执行前的初始化
     * @throws Exception 如果初始化失败
     */
    void initialize() throws Exception;
    
    /**
     * 钩子的名称，用于日志和监控
     */
    String name();
}