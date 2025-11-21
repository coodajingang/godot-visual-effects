package com.dus.pipeline.example;

import com.dus.pipeline.core.*;
import com.dus.pipeline.hook.db.*;
import com.dus.pipeline.hook.cache.*;
import com.dus.pipeline.hook.notification.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import redis.clients.jedis.JedisPool;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Pipeline 使用示例
 */
public class PipelineExample {
    
    private static final Logger logger = LoggerFactory.getLogger(PipelineExample.class);
    
    /**
     * 基础示例：创建和删除临时表
     */
    public static void basicExample(DataSource dataSource) {
        logger.info("=== Basic Example: Temporary Table ===");
        
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSourceOperator())
            .addBeforeHook(new TemporaryTableCreationHook(
                dataSource,
                "CREATE TEMPORARY TABLE temp_data (id INT, name VARCHAR(100))"
            ))
            .addOperator(new TransformOperator())
            .addOperator(new WriteToDbOperator(dataSource, "temp_data"))
            .addAfterHook(new TemporaryTableCleanupHook(
                dataSource,
                "DROP TABLE IF EXISTS temp_data"
            ));

        try {
            pipeline.run();
            logger.info("Basic example completed successfully");
        } catch (Exception e) {
            logger.error("Basic example failed", e);
        }
    }
    
    /**
     * 复杂示例：多个钩子 + 通知
     */
    public static void complexExample(DataSource dataSource, JedisPool jedisPool, 
                                     EmailService emailService, MetricsRegistry metricsRegistry) {
        logger.info("=== Complex Example: Multiple Hooks + Notification ===");
        
        PipelineContext context = new PipelineContext();
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSourceOperator())
            .withContext(context)
            .addBeforeHook(new CacheInitializationHook(jedisPool, "pipeline:cache:"))
            .addBeforeHook(new DatabaseCleanupHook(dataSource, "DELETE FROM staging_table"))
            .addOperator(new EnrichOperator())
            .addOperator(new WriteToDbOperator(dataSource, "final_table"))
            .addAfterHook(new CacheCleanupHook(jedisPool, "pipeline:cache:*"))
            .addAfterHook(new MetricsReportingHook(metricsRegistry, pipeline))
            .addAfterHook(new NotificationHook(emailService, "admin@example.com"));

        try {
            pipeline.run();
            logger.info("Complex example completed. Total records: {}", context.getTotalRecordCount());
        } catch (Exception e) {
            logger.error("Complex example failed", e);
        }
    }
    
    /**
     * 异步 Pipeline 示例
     */
    public static void asyncExample(DataSource dataSource, EmailService emailService) {
        logger.info("=== Async Pipeline Example ===");
        
        AsyncPipeline<List<String>, Void> asyncPipeline = new AsyncPipeline<>(new TestSourceOperator())
            .addBeforeHook(new TemporaryTableCreationHook(
                dataSource,
                "CREATE TEMPORARY TABLE temp_async_data (id INT, name VARCHAR(100))"
            ))
            .addOperator(new AsyncTransformOperator())
            .addOperator(new WriteToDbOperator(dataSource, "temp_async_data"))
            .addAfterHook(new TemporaryTableCleanupHook(
                dataSource,
                "DROP TABLE IF EXISTS temp_async_data"
            ))
            .addAfterHook(new NotificationHook(emailService, "team@example.com"));

        asyncPipeline.runAsync()
            .thenAccept(v -> logger.info("Async pipeline completed"))
            .exceptionally(e -> {
                logger.error("Async pipeline failed", e);
                return null;
            });
    }
    
    // 测试用的简单操作符
    static class TestSourceOperator implements Source<List<String>> {
        private int count = 0;
        
        @Override
        public List<String> nextBatch() {
            if (count >= 3) {
                return null;
            }
            return Arrays.asList("data" + (++count), "test" + count);
        }
        
        @Override
        public String name() {
            return "TestSourceOperator";
        }
    }
    
    static class TransformOperator implements Operator<List<String>, List<String>> {
        @Override
        public List<String> process(List<String> input) {
            return input.stream()
                .map(s -> "transformed_" + s)
                .toList();
        }
        
        @Override
        public String name() {
            return "TransformOperator";
        }
    }
    
    static class EnrichOperator implements Operator<List<String>, List<String>> {
        @Override
        public List<String> process(List<String> input) {
            return input.stream()
                .map(s -> s + "_enriched")
                .toList();
        }
        
        @Override
        public String name() {
            return "EnrichOperator";
        }
    }
    
    static class AsyncTransformOperator implements Operator<List<String>, List<String>> {
        @Override
        public List<String> process(List<String> input) {
            // 模拟异步处理
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return input.stream()
                .map(s -> "async_transformed_" + s)
                .toList();
        }
        
        @Override
        public String name() {
            return "AsyncTransformOperator";
        }
    }
    
    static class WriteToDbOperator implements Operator<List<String>, Void> {
        private final DataSource dataSource;
        private final String tableName;
        
        public WriteToDbOperator(DataSource dataSource, String tableName) {
            this.dataSource = dataSource;
            this.tableName = tableName;
        }
        
        @Override
        public Void process(List<String> input) {
            // 模拟写入数据库
            logger.info("Writing {} records to table {}", input.size(), tableName);
            return null;
        }
        
        @Override
        public String name() {
            return "WriteToDbOperator[" + tableName + "]";
        }
    }
}