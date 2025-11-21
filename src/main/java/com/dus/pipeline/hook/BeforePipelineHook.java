package com.dus.pipeline.hook;

import com.dus.pipeline.core.PipelineContext;

/**
 * Pipeline 开始前执行的钩子
 */
public interface BeforePipelineHook extends PipelineHook {
    
    /**
     * Pipeline 开始前执行
     * 常见场景：清理数据库、初始化缓存、创建临时表、预热连接池
     * 
     * @param context 运行上下文，包含配置参数等
     * @throws Exception 如果执行失败，Pipeline 会中止
     */
    void beforePipeline(PipelineContext context) throws Exception;
}