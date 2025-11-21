package com.dus.pipeline.core;

/**
 * Pipeline 状态枚举
 */
public enum PipelineStatus {
    /**
     * 初始化状态
     */
    INITIALIZED,
    
    /**
     * 运行中
     */
    RUNNING,
    
    /**
     * 已停止（正常完成）
     */
    STOPPED,
    
    /**
     * 失败
     */
    FAILED,
    
    /**
     * 被中断
     */
    INTERRUPTED
}