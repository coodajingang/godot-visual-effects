package com.dus.pipeline.async;

import com.dus.pipeline.metrics.DefaultMetricsCollector;
import com.dus.pipeline.metrics.MetricsCollector;
import com.dus.pipeline.metrics.OperatorMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 异步管道类
 * 支持异步算子链的调度和执行
 * 返回 CompletableFuture 支持非阻塞操作
 *
 * @param <I> 初始输入数据类型
 * @param <O> 最终输出数据类型
 * @author Dus
 * @version 1.0
 */
public class AsyncPipeline<I, O> {

    private final AsyncSourceOperator<I> source;
    private final List<AsyncOperator<?, ?>> operators;
    private final ExecutorService executor;
    private MetricsCollector metricsCollector;
    private final AtomicLong totalBatchesProcessed;
    private long pipelineStartTime;
    private long pipelineDurationNanos;

    /**
     * 构造函数，初始化异步管道
     *
     * @param source 异步数据源算子
     * @throws IllegalArgumentException 如果source为null
     */
    public AsyncPipeline(AsyncSourceOperator<I> source) {
        this.source = Objects.requireNonNull(source, "Source operator cannot be null");
        this.operators = new ArrayList<>();
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "AsyncPipeline-Executor");
            t.setDaemon(false);
            return t;
        });
        this.metricsCollector = new DefaultMetricsCollector();
        this.totalBatchesProcessed = new AtomicLong(0);
        this.pipelineStartTime = 0;
        this.pipelineDurationNanos = 0;
    }

    /**
     * 添加异步算子到管道中，支持链式调用
     *
     * @param <T> 算子的输出类型
     * @param operator 要添加的异步算子
     * @return 当前管道实例，支持链式调用
     * @throws IllegalArgumentException 如果operator为null
     */
    public <T> AsyncPipeline<I, T> addOperator(AsyncOperator<?, T> operator) {
        Objects.requireNonNull(operator, "Operator cannot be null");
        this.operators.add(operator);
        @SuppressWarnings("unchecked")
        AsyncPipeline<I, T> result = (AsyncPipeline<I, T>) this;
        return result;
    }

    /**
     * 异步运行管道
     * 返回 CompletableFuture 用于非阻塞等待
     *
     * @return 表示管道执行完成的 CompletableFuture
     */
    public CompletableFuture<Void> runAsync() {
        return CompletableFuture.runAsync(() -> {
            System.out.println("Starting async pipeline execution...");
            System.out.println("Source: " + source.name());
            System.out.println("Operators: " + operators.size());
            System.out.println("---");

            pipelineStartTime = System.nanoTime();

            try {
                while (true) {
                    // 从数据源异步获取下一批数据
                    I batch = source.nextBatch();
                    if (batch == null || isEmpty(batch)) {
                        System.out.println("No more data available. Pipeline execution completed.");
                        break;
                    }

                    System.out.println("Processing batch");

                    // 构建异步操作链
                    CompletableFuture<Object> future =
                            CompletableFuture.completedFuture((Object) batch);

                    for (AsyncOperator<?, ?> operator : operators) {
                        future = future.thenCompose(data -> {
                            try {
                                long startTime = System.nanoTime();

                                @SuppressWarnings("unchecked")
                                AsyncOperator<Object, Object> typedOperator = 
                                    (AsyncOperator<Object, Object>) operator;

                                return typedOperator.processAsync(data)
                                        .whenComplete((result, ex) -> {
                                            long duration = System.nanoTime() - startTime;
                                            if (ex != null) {
                                                metricsCollector.recordFailure(operator.name(), duration, ex);
                                                System.err.println("Error in operator " + operator.name() + 
                                                    ": " + ex.getMessage());
                                            } else {
                                                metricsCollector.recordSuccess(operator.name(), duration);
                                                System.out.println("  -> " + operator.name() + " completed (async)");
                                            }
                                        });
                            } catch (Exception e) {
                                return CompletableFuture.failedFuture(e);
                            }
                        });
                    }

                    // 等待该批次完成
                    future.join();

                    totalBatchesProcessed.incrementAndGet();
                    System.out.println("Batch processed successfully");
                    System.out.println("---");
                }

                pipelineDurationNanos = System.nanoTime() - pipelineStartTime;
                System.out.println("Total batches processed: " + totalBatchesProcessed.get());
                System.out.println("Pipeline execution finished successfully.");
            } catch (Exception e) {
                System.err.println("Pipeline execution failed: " + e.getMessage());
                e.printStackTrace();
            }
        }, executor);
    }

    /**
     * 判断批次是否为空
     *
     * @param batch 批次数据
     * @return 如果为空返回 true
     */
    private boolean isEmpty(I batch) {
        if (batch == null) {
            return true;
        }
        if (batch instanceof List) {
            return ((List<?>) batch).isEmpty();
        }
        return false;
    }

    /**
     * 设置 metrics 收集器
     *
     * @param metricsCollector metrics 收集器
     */
    public void setMetricsCollector(MetricsCollector metricsCollector) {
        this.metricsCollector = Objects.requireNonNull(metricsCollector, "MetricsCollector cannot be null");
    }

    /**
     * 获取所有算子的 metrics
     *
     * @return 包含所有算子 metrics 的 Map
     */
    public Map<String, OperatorMetrics> getMetrics() {
        return metricsCollector.getAllMetrics();
    }

    /**
     * 打印可读的性能报告
     */
    public void printMetricsReport() {
        System.out.println("\n========== AsyncPipeline Metrics Report ==========");
        System.out.println("Total Batches Processed: " + totalBatchesProcessed.get());
        System.out.println("Total Duration: " + (pipelineDurationNanos / 1_000_000.0) + " ms");
        System.out.println("\nOperator Metrics:");
        System.out.println("---");

        Map<String, OperatorMetrics> metrics = metricsCollector.getAllMetrics();
        for (Map.Entry<String, OperatorMetrics> entry : metrics.entrySet()) {
            OperatorMetrics m = entry.getValue();
            System.out.println(String.format(
                "%s: invoke=%d, success=%d, failure=%d, avg=%.2fms, min=%dms, max=%dms",
                m.getOperatorName(),
                m.getInvokeCount(),
                m.getSuccessCount(),
                m.getFailureCount(),
                m.getAvgDurationNanos() / 1_000_000.0,
                m.getMinDurationNanos() / 1_000_000,
                m.getMaxDurationNanos() / 1_000_000
            ));
        }
        System.out.println("================================================\n");
    }

    /**
     * 优雅关闭
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * 强制关闭
     */
    public void shutdownNow() {
        executor.shutdownNow();
    }

    /**
     * 等待流程终止
     *
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 如果成功终止返回 true，超时返回 false
     * @throws InterruptedException 如果等待被中断
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    /**
     * 获取内部 executor
     *
     * @return ExecutorService
     */
    public ExecutorService getExecutor() {
        return executor;
    }
}
