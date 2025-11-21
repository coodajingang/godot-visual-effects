package com.dus.pipeline.retry;

/**
 * 监听被跳过的数据
 */
public interface SkipListener {
    /**
     * 当数据被跳过时调用
     * @param input 被跳过的输入数据
     * @param exception 导致跳过的异常
     */
    void onSkipped(Object input, Exception exception);
}