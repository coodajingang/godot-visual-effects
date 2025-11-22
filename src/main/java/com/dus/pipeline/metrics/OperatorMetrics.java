package com.dus.pipeline.metrics;

import java.util.Objects;

/**
 * 算子级别的性能指标统计类
 * 记录单个算子的调用次数、成功/失败数、耗时等统计数据
 *
 * @author Dus
 * @version 1.0
 */
public class OperatorMetrics {
    private final String operatorName;
    private long invokeCount;
    private long successCount;
    private long failureCount;
    private long totalDurationNanos;
    private long minDurationNanos;
    private long maxDurationNanos;

    /**
     * 构造函数
     *
     * @param operatorName 算子名称
     */
    public OperatorMetrics(String operatorName) {
        this.operatorName = Objects.requireNonNull(operatorName, "Operator name cannot be null");
        this.invokeCount = 0;
        this.successCount = 0;
        this.failureCount = 0;
        this.totalDurationNanos = 0;
        this.minDurationNanos = Long.MAX_VALUE;
        this.maxDurationNanos = 0;
    }

    /**
     * 获取算子名称
     *
     * @return 算子名称
     */
    public String getOperatorName() {
        return operatorName;
    }

    /**
     * 获取总调用次数
     *
     * @return 调用次数
     */
    public long getInvokeCount() {
        return invokeCount;
    }

    /**
     * 获取成功次数
     *
     * @return 成功次数
     */
    public long getSuccessCount() {
        return successCount;
    }

    /**
     * 获取失败次数
     *
     * @return 失败次数
     */
    public long getFailureCount() {
        return failureCount;
    }

    /**
     * 获取总耗时（纳秒）
     *
     * @return 总耗时（纳秒）
     */
    public long getTotalDurationNanos() {
        return totalDurationNanos;
    }

    /**
     * 获取最小单次耗时（纳秒）
     *
     * @return 最小单次耗时（纳秒）
     */
    public long getMinDurationNanos() {
        return minDurationNanos == Long.MAX_VALUE ? 0 : minDurationNanos;
    }

    /**
     * 获取最大单次耗时（纳秒）
     *
     * @return 最大单次耗时（纳秒）
     */
    public long getMaxDurationNanos() {
        return maxDurationNanos;
    }

    /**
     * 获取平均耗时（纳秒）
     *
     * @return 平均耗时（纳秒）
     */
    public double getAvgDurationNanos() {
        if (successCount == 0) {
            return 0.0;
        }
        return (double) totalDurationNanos / successCount;
    }

    /**
     * 记录一次成功的调用
     *
     * @param durationNanos 耗时（纳秒）
     */
    protected synchronized void recordSuccess(long durationNanos) {
        this.invokeCount++;
        this.successCount++;
        this.totalDurationNanos += durationNanos;
        this.minDurationNanos = Math.min(minDurationNanos, durationNanos);
        this.maxDurationNanos = Math.max(maxDurationNanos, durationNanos);
    }

    /**
     * 记录一次失败的调用
     *
     * @param durationNanos 耗时（纳秒）
     */
    protected synchronized void recordFailure(long durationNanos) {
        this.invokeCount++;
        this.failureCount++;
        this.totalDurationNanos += durationNanos;
        this.minDurationNanos = Math.min(minDurationNanos, durationNanos);
        this.maxDurationNanos = Math.max(maxDurationNanos, durationNanos);
    }

    /**
     * 重置所有统计数据
     */
    protected synchronized void reset() {
        this.invokeCount = 0;
        this.successCount = 0;
        this.failureCount = 0;
        this.totalDurationNanos = 0;
        this.minDurationNanos = Long.MAX_VALUE;
        this.maxDurationNanos = 0;
    }

    @Override
    public String toString() {
        return "OperatorMetrics{" +
                "operatorName='" + operatorName + '\'' +
                ", invokeCount=" + invokeCount +
                ", successCount=" + successCount +
                ", failureCount=" + failureCount +
                ", totalDurationNanos=" + totalDurationNanos +
                ", minDurationNanos=" + getMinDurationNanos() +
                ", maxDurationNanos=" + maxDurationNanos +
                ", avgDurationNanos=" + String.format("%.2f", getAvgDurationNanos()) +
                '}';
    }
}
