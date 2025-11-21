package com.dus.pipeline.core;

import com.dus.pipeline.context.PipelineContext;

/**
 * 标记接口：表示该算子能够接收 PipelineContext
 */
public interface ContextAware {
    /**
     * 注入 PipelineContext
     * @param context Pipeline 运行上下文
     */
    void setContext(PipelineContext context);
}