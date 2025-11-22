package com.dus.pipeline.core;

import com.dus.pipeline.metrics.DefaultMetricsCollector;
import com.dus.pipeline.metrics.MetricsCollector;
import com.dus.pipeline.metrics.OperatorMetrics;
import com.dus.pipeline.splitter.BatchSplitter;
import com.dus.pipeline.splitter.NoBatchSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 管道类，负责流程调度和算子链管理
 * 支持链式调用添加算子，并按顺序执行算子链
 * 支持生命周期管理、性能指标收集和批次拆分
 * 
 * @param <I> 初始输入数据类型
 * @param <O> 最终输出数据类型
 * @author Dus
 * @version 1.0
 */
public class Pipeline<I, O> {
    
    private final SourceOperator<I> source;
    private final List<Operator<?, ?>> operators;
    private final ExecutorService executor;
    private MetricsCollector metricsCollector;
    private BatchSplitter<I> batchSplitter;
    private PipelineStatus status;
    private final AtomicLong totalBatchesProcessed;
    private long pipelineStartTime;
    private long pipelineDurationNanos;
    
    /**
     * 构造函数，初始化管道
     * 
     * @param source 数据源算子
     * @throws IllegalArgumentException 如果source为null
     */
    public Pipeline(SourceOperator<I> source) {
        this.source = Objects.requireNonNull(source, "Source operator cannot be null");
        this.operators = new ArrayList<>();
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Pipeline-Executor");
            t.setDaemon(false);
            return t;
        });
        this.metricsCollector = new DefaultMetricsCollector();
        this.batchSplitter = new NoBatchSplitter<>();
        this.status = PipelineStatus.INIT;
        this.totalBatchesProcessed = new AtomicLong(0);
        this.pipelineStartTime = 0;
        this.pipelineDurationNanos = 0;
    }
    
    /**
     * 添加算子到管道中，支持链式调用
     * 
     * @param <T> 算子的输出类型
     * @param operator 要添加的算子
     * @return 当前管道实例，支持链式调用
     * @throws IllegalArgumentException 如果operator为null
     */
    public <T> Pipeline<I, T> addOperator(Operator<?, T> operator) {
        Objects.requireNonNull(operator, "Operator cannot be null");
        this.operators.add(operator);
        @SuppressWarnings("unchecked")
        Pipeline<I, T> result = (Pipeline<I, T>) this;
        return result;
    }
    
    /**
     * 启动管道执行
     * 循环从数据源获取数据，并通过算子链进行处理
     * 支持 metrics 收集和 batch 拆分
     * 
     * @throws Exception 管道执行过程中可能抛出的异常
     */
    public void run() throws Exception {
        System.out.println("Starting pipeline execution...");
        System.out.println("Source: " + source.name());
        System.out.println("Operators: " + operators.stream().map(Operator::name).reduce((a, b) -> a + " -> " + b).orElse("None"));
        System.out.println("BatchSplitter: " + batchSplitter.name());
        System.out.println("---");
        
        status = PipelineStatus.RUNNING;
        pipelineStartTime = System.nanoTime();
        
        try {
            int batchCount = 0;
            while (status == PipelineStatus.RUNNING) {
                try {
                    // 从数据源获取下一批数据
                    I batch = source.nextBatch();
                    if (batch == null) {
                        System.out.println("No more data available. Pipeline execution completed.");
                        break;
                    }
                    
                    // 判断是否需要拆分
                    List<I> batches;
                    if (batchSplitter.shouldSplit(batch)) {
                        batches = batchSplitter.split(batch);
                        System.out.println("Batch split into " + batches.size() + " sub-batches");
                    } else {
                        batches = new ArrayList<>();
                        batches.add(batch);
                    }
                    
                    // 处理每个子批次
                    for (I subBatch : batches) {
                        batchCount++;
                        System.out.println("Processing batch " + batchCount);
                        
                        // 依次执行算子链
                        Object currentData = subBatch;
                        for (Operator<?, ?> operator : operators) {
                            long startTime = System.nanoTime();
                            try {
                                @SuppressWarnings("unchecked")
                                Operator<Object, Object> typedOperator = (Operator<Object, Object>) operator;
                                currentData = typedOperator.process(currentData);
                                
                                long duration = System.nanoTime() - startTime;
                                metricsCollector.recordSuccess(operator.name(), duration);
                                System.out.println("  -> " + operator.name() + " completed (duration: " + (duration / 1_000_000.0) + "ms)");
                            } catch (Exception e) {
                                long duration = System.nanoTime() - startTime;
                                metricsCollector.recordFailure(operator.name(), duration, e);
                                System.err.println("Error in operator " + operator.name() + ": " + e.getMessage());
                                throw e;
                            }
                        }
                        
                        totalBatchesProcessed.incrementAndGet();
                        System.out.println("Batch " + batchCount + " processed successfully");
                        System.out.println("---");
                    }
                    
                } catch (Exception e) {
                    System.err.println("Pipeline execution failed on batch " + (batchCount + 1) + ": " + e.getMessage());
                    status = PipelineStatus.FAILED;
                    throw e;
                }
            }
            
            pipelineDurationNanos = System.nanoTime() - pipelineStartTime;
            if (status == PipelineStatus.RUNNING) {
                status = PipelineStatus.STOPPED;
            }
            System.out.println("Total batches processed: " + totalBatchesProcessed.get());
            System.out.println("Pipeline execution finished successfully.");
        } finally {
            if (status == PipelineStatus.RUNNING) {
                status = PipelineStatus.STOPPED;
            }
        }
    }
    
    /**
     * 记录开始处理算子的时间
     * 用于内部 metrics 统计
     *
     * @param operatorName 算子名称
     * @return 记录的开始时间（纳秒）
     */
    private long recordOperatorStartTime(String operatorName) {
        metricsCollector.recordStart(operatorName);
        return System.nanoTime();
        System.out.println("---");
        
        int batchCount = 0;
        while (true) {
            try {
                // 从数据源获取下一批数据
                I batch = source.nextBatch();
                if (batch == null) {
                    System.out.println("No more data available. Pipeline execution completed.");
                    break;
                }
                
                batchCount++;
                System.out.println("Processing batch " + batchCount);
                
                // 依次执行算子链
                Object currentData = batch;
                for (Operator<?, ?> operator : operators) {
                    try {
                        @SuppressWarnings("unchecked")
                        Operator<Object, Object> typedOperator = (Operator<Object, Object>) operator;
                        currentData = typedOperator.process(currentData);
                        System.out.println("  -> " + operator.name() + " completed");
                    } catch (Exception e) {
                        System.err.println("Error in operator " + operator.name() + ": " + e.getMessage());
                        throw e;
                    }
                }
                
                System.out.println("Batch " + batchCount + " processed successfully");
                System.out.println("---");
                
            } catch (Exception e) {
                System.err.println("Pipeline execution failed on batch " + (batchCount + 1) + ": " + e.getMessage());
                throw e;
            }
        }
        
        System.out.println("Total batches processed: " + batchCount);
        System.out.println("Pipeline execution finished successfully.");
    }
    
    /**
     * 获取数据源算子
     * 
     * @return 数据源算子
     */
    public SourceOperator<I> getSource() {
        return source;
    }
    
    /**
     * 获取算子列表的副本
     * 
     * @return 算子列表副本
     */
    public List<Operator<?, ?>> getOperators() {
        return new ArrayList<>(operators);
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
     * 获取 metrics 收集器
     *
     * @return metrics 收集器
     */
    public MetricsCollector getMetricsCollector() {
        return metricsCollector;
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
        System.out.println("\n========== Pipeline Metrics Report ==========");
        System.out.println("Pipeline Status: " + status);
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
        System.out.println("============================================\n");
    }

    /**
     * 设置批次拆分器
     *
     * @param batchSplitter 批次拆分器
     */
    public void setBatchSplitter(BatchSplitter<I> batchSplitter) {
        this.batchSplitter = Objects.requireNonNull(batchSplitter, "BatchSplitter cannot be null");
    }

    /**
     * 获取批次拆分器
     *
     * @return 批次拆分器
     */
    public BatchSplitter<I> getBatchSplitter() {
        return batchSplitter;
    }

    /**
     * 获取当前 Pipeline 状态
     *
     * @return Pipeline 状态
     */
    public PipelineStatus getStatus() {
        return status;
    }

    /**
     * 获取内部 executor
     *
     * @return ExecutorService
     */
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * 优雅关闭，等待当前批次完成
     */
    public void shutdown() {
        status = PipelineStatus.STOPPING;
        executor.shutdown();
    }

    /**
     * 强制关闭，中断当前处理
     */
    public void shutdownNow() {
        status = PipelineStatus.STOPPING;
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
}