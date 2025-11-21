package com.dus.pipeline.core;

import com.dus.pipeline.metrics.MetricsCollector;
import com.dus.pipeline.metrics.DefaultMetricsCollector;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 管道类，负责流程调度和算子链管理
 * 支持链式调用添加算子，并按顺序执行算子链
 * 
 * @param <I> 初始输入数据类型
 * @param <O> 最终输出数据类型
 * @author Dus
 * @version 1.0
 */
public class Pipeline<I, O> {
    
    /**
     * 管道状态枚举
     */
    public enum PipelineStatus {
        INIT, RUNNING, STOPPING, STOPPED, FAILED
    }
    
    private final SourceOperator<I> source;
    private final List<Operator<?, ?>> operators;
    private final MetricsCollector metricsCollector;
    private final AtomicReference<PipelineStatus> status;
    
    /**
     * 构造函数，初始化管道
     * 
     * @param source 数据源算子
     * @throws IllegalArgumentException 如果source为null
     */
    public Pipeline(SourceOperator<I> source) {
        this(source, new DefaultMetricsCollector());
    }
    
    /**
     * 构造函数，初始化管道并指定指标收集器
     * 
     * @param source 数据源算子
     * @param metricsCollector 指标收集器
     * @throws IllegalArgumentException 如果source或metricsCollector为null
     */
    public Pipeline(SourceOperator<I> source, MetricsCollector metricsCollector) {
        this.source = Objects.requireNonNull(source, "Source operator cannot be null");
        this.metricsCollector = Objects.requireNonNull(metricsCollector, "Metrics collector cannot be null");
        this.operators = new ArrayList<>();
        this.status = new AtomicReference<>(PipelineStatus.INIT);
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
     * 
     * @throws Exception 管道执行过程中可能抛出的异常
     */
    public void run() throws Exception {
        if (!status.compareAndSet(PipelineStatus.INIT, PipelineStatus.RUNNING)) {
            throw new IllegalStateException("Pipeline is already running or has been stopped");
        }
        
        try {
            System.out.println("Starting pipeline execution...");
            System.out.println("Source: " + source.name());
            System.out.println("Operators: " + operators.stream().map(Operator::name).reduce((a, b) -> a + " -> " + b).orElse("None"));
            System.out.println("---");
            
            int batchCount = 0;
            while (status.get() == PipelineStatus.RUNNING) {
                try {
                    // 从数据源获取下一批数据
                    long sourceStartTime = System.nanoTime();
                    I batch = source.nextBatch();
                    long sourceDuration = System.nanoTime() - sourceStartTime;
                    
                    if (batch == null) {
                        metricsCollector.recordSuccess(source.name(), sourceDuration);
                        System.out.println("No more data available. Pipeline execution completed.");
                        break;
                    }
                    
                    metricsCollector.recordSuccess(source.name(), sourceDuration);
                    batchCount++;
                    System.out.println("Processing batch " + batchCount);
                    
                    // 依次执行算子链
                    Object currentData = batch;
                    for (Operator<?, ?> operator : operators) {
                        if (status.get() != PipelineStatus.RUNNING) {
                            break;
                        }
                        
                        long operatorStartTime = System.nanoTime();
                        try {
                            @SuppressWarnings("unchecked")
                            Operator<Object, Object> typedOperator = (Operator<Object, Object>) operator;
                            currentData = typedOperator.process(currentData);
                            long operatorDuration = System.nanoTime() - operatorStartTime;
                            metricsCollector.recordSuccess(operator.name(), operatorDuration);
                            System.out.println("  -> " + operator.name() + " completed");
                        } catch (Exception e) {
                            long operatorDuration = System.nanoTime() - operatorStartTime;
                            metricsCollector.recordFailure(operator.name(), operatorDuration);
                            System.err.println("Error in operator " + operator.name() + ": " + e.getMessage());
                            throw e;
                        }
                    }
                    
                    System.out.println("Batch " + batchCount + " processed successfully");
                    System.out.println("---");
                    
                } catch (Exception e) {
                    status.set(PipelineStatus.FAILED);
                    System.err.println("Pipeline execution failed on batch " + (batchCount + 1) + ": " + e.getMessage());
                    throw e;
                }
            }
            
            if (status.get() == PipelineStatus.RUNNING) {
                status.set(PipelineStatus.STOPPED);
            }
            
            System.out.println("Total batches processed: " + batchCount);
            System.out.println("Pipeline execution finished successfully.");
            
        } catch (Exception e) {
            status.set(PipelineStatus.FAILED);
            throw e;
        }
    }
    
    /**
     * 优雅关闭管道
     */
    public void shutdown() {
        status.compareAndSet(PipelineStatus.RUNNING, PipelineStatus.STOPPING);
        status.set(PipelineStatus.STOPPED);
        System.out.println("Pipeline shutdown completed.");
    }
    
    /**
     * 等待管道执行完成
     * 
     * @param timeoutMillis 超时时间（毫秒）
     * @return 如果在超时时间内完成返回true，否则返回false
     */
    public boolean awaitTermination(long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        PipelineStatus currentStatus;
        while ((currentStatus = status.get()) == PipelineStatus.RUNNING || currentStatus == PipelineStatus.STOPPING) {
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                return false;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }
    
    /**
     * 获取当前管道状态
     * 
     * @return 管道状态
     */
    public PipelineStatus getStatus() {
        return status.get();
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
     * 获取指标收集器
     * 
     * @return 指标收集器
     */
    public MetricsCollector getMetricsCollector() {
        return metricsCollector;
    }
    
    /**
     * 打印指标报告
     */
    public void printMetricsReport() {
        metricsCollector.printMetricsReport();
    }
}