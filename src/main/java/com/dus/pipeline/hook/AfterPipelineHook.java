package com.dus.pipeline.hook;

import com.dus.pipeline.core.PipelineContext;

/**
 * Pipeline 结束后执行的钩子
 */
public interface AfterPipelineHook extends PipelineHook {
    
    /**
     * Pipeline 成功完成后执行
     * 常见场景：删除临时表、提交事务、发送通知
     * 
     * @param context 运行上下文
     * @throws Exception 如果执行失败，仅记录日志，不影响已完成的 Pipeline
     */
    void afterPipeline(PipelineContext context) throws Exception;
    
    /**
     * Pipeline 执行失败后执行
     * 常见场景：回滚事务、清理缓存、删除不完整数据
     * 
     * @param context 运行上下文
     * @param exception 导致失败的异常
     * @throws Exception 如果执行失败，仅记录日志
     */
    void onPipelineFailure(PipelineContext context, Exception exception) throws Exception;
    
    /**
     * Pipeline 被中断后执行
     * 常见场景：释放资源、记录中断点
     * 
     * @param context 运行上下文
     * @throws Exception 如果执行失败，仅记录日志
     */
    void onPipelineInterrupted(PipelineContext context) throws Exception;
}