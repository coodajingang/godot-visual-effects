package com.dus.pipeline.example;

import com.dus.pipeline.core.Pipeline;
import com.dus.pipeline.context.PipelineContext;
import com.dus.pipeline.retry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline 框架使用示例
 */
public class PipelineExample {
    
    private static final Logger logger = LoggerFactory.getLogger(PipelineExample.class);
    
    public static void main(String[] args) {
        // 创建 Pipeline 上下文
        PipelineContext context = new PipelineContext();
        context.setProperty("enrichment_api_url", "http://api.example.com");
        context.setProperty("timeout", 5000);
        context.setProperty("enrich_prefix", "processed_");
        
        // 创建源算子
        ExampleSourceOperator source = new ExampleSourceOperator(20, 5);
        
        // 创建 Pipeline
        Pipeline<List<Data>, Void> pipeline = new Pipeline<>(source)
            .withContext(context)
            .addPreHook(() -> logger.info("=== Pipeline Starting ==="))
            .addPostHook(() -> logger.info("=== Pipeline Completed ==="))
            .addOperator(new ValidateOperator()
                .withRetryStrategy(new FixedDelayRetryStrategy(2, 500)))
            .addOperator(new EnrichOperator()
                .withFailureRate(0.2) // 20% 失败率
                .withRetryStrategy(new ExponentialBackoffRetryStrategy(
                    3, 1000, 5000, 2.0))
                .withSkipStrategy(new SkipFailedRecordsStrategy(2)
                    .setSkipListener((input, exception) -> 
                        logger.warn("Skipped batch: {}", exception.getMessage()))))
            .addOperator(new ExampleSinkOperator());
        
        try {
            // 执行 Pipeline
            pipeline.run();
            
            // 输出结果统计
            printPipelineResults(context);
            
        } catch (Exception e) {
            logger.error("Pipeline execution failed", e);
        }
    }
    
    /**
     * 输出 Pipeline 执行结果
     */
    private static void printPipelineResults(PipelineContext context) {
        logger.info("=== Pipeline Execution Results ===");
        logger.info("Total execution time: {} ms", context.getElapsedTime());
        logger.info("Total batches processed: {}", context.getBatchCount());
        
        // 输出所有 Context 属性
        logger.info("=== Context Properties ===");
        context.getAllProperties().forEach((key, value) -> 
            logger.info("{} = {}", key, value));
    }
    
    /**
     * 演示不同的重试策略
     */
    public static void demonstrateRetryStrategies() {
        logger.info("=== Demonstrating Retry Strategies ===");
        
        // 固定延迟重试
        RetryStrategy fixedRetry = new FixedDelayRetryStrategy(3, 1000);
        fixedRetry.addRetryableException(RuntimeException.class);
        logger.info("Fixed retry strategy: {}", fixedRetry.name());
        
        // 指数退避重试
        RetryStrategy exponentialRetry = new ExponentialBackoffRetryStrategy(
            5, 1000, 30000, 2.0);
        exponentialRetry.addRetryableException(IllegalStateException.class);
        logger.info("Exponential retry strategy: {}", exponentialRetry.name());
        
        // 自适应重试
        AdaptiveRetryStrategy adaptiveRetry = new AdaptiveRetryStrategy()
            .withDefaults(2, 500)
            .configureException(RuntimeException.class, 3, 1000)
            .configureException(IllegalArgumentException.class, 1, 0);
        logger.info("Adaptive retry strategy: {}", adaptiveRetry.name());
        
        // 跳过策略
        SkipStrategy skipStrategy = new SkipFailedRecordsStrategy(3)
            .addSkippableException(ValidationException.class)
            .setSkipListener((input, exception) -> 
                logger.warn("Skipped record: {}", input));
        logger.info("Skip strategy: {}", skipStrategy.name());
    }
    
    /**
     * 演示 Context 在算子间的共享
     */
    public static void demonstrateContextSharing() {
        logger.info("=== Demonstrating Context Sharing ===");
        
        PipelineContext context = new PipelineContext();
        
        // 设置初始配置
        context.setProperty("api_key", "secret123");
        context.setProperty("batch_size", 100);
        context.setProperty("processing_mode", "streaming");
        
        // 创建带有 Context 的算子
        ExampleSourceOperator source = new ExampleSourceOperator(10, 3);
        
        Pipeline<List<Data>, Void> pipeline = new Pipeline<>(source)
            .withContext(context)
            .addOperator(new EnrichOperator())
            .addOperator(new ExampleSinkOperator());
        
        pipeline.run();
        
        // 检查 Context 中的共享信息
        logger.info("Final context state: {}", context);
    }
}