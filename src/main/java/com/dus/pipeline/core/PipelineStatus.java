package com.dus.pipeline.core;

/**
 * Pipeline 生命周期状态枚举
 * 定义了 Pipeline 的各个生命周期状态
 *
 * @author Dus
 * @version 1.0
 */
public enum PipelineStatus {
    /**
     * 初始化状态
     */
    INIT,

    /**
     * 运行中状态
     */
    RUNNING,

    /**
     * 停止中状态
     */
    STOPPING,

    /**
     * 已停止状态
     */
    STOPPED,

    /**
     * 失败状态
     */
    FAILED
}
