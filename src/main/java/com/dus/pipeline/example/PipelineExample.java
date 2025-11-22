package com.dus.pipeline.example;

import com.dus.pipeline.core.Pipeline;
import java.util.HashMap;
import java.util.Map;

/**
 * 管道使用示例
 * 演示如何构建和执行一个完整的数据处理管道
 * 
 * @author Dus
 * @version 1.0
 */
public class PipelineExample {
    
    /**
     * 主方法，演示管道的创建和执行
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            System.out.println("=== Java8 Pipeline/Operator Framework Example ===\n");
            
            // 示例1：基础数据处理管道
            runBasicPipeline();
            
            System.out.println("\n" + "=".repeat(50) + "\n");
            
            // 示例2：带缓存的高级数据处理管道
            runAdvancedPipeline();
            
        } catch (Exception e) {
            System.err.println("Pipeline execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 运行基础数据处理管道示例
     * 
     * @throws Exception 管道执行异常
     */
    private static void runBasicPipeline() throws Exception {
        System.out.println("Example 1: Basic Data Processing Pipeline");
        System.out.println("Data Flow: Source -> Transform -> Write to DB\n");
        
        // 创建数据源算子
        MySourceOperator source = new MySourceOperator(5, 10);
        
        // 创建转换算子
        TransformOperator transform = new TransformOperator("PROCESSED_", true);
        
        // 创建写库算子
        WriteToDbOperator sink = new WriteToDbOperator("processed_data", false);
        
        // 构建管道
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(source)
                .addOperator(transform)
                .addOperator(sink);
        
        // 执行管道
        pipeline.run();
        
        // 输出统计信息
        System.out.println("\nPipeline Statistics:");
        System.out.println("- Transform operator processed: " + transform.getProcessedCount() + " records");
        System.out.println("- Write operator written: " + sink.getTotalWritten() + " records in " + sink.getBatchCount() + " batches");
    }
    
    /**
     * 运行高级数据处理管道示例
     * 
     * @throws Exception 管道执行异常
     */
    private static void runAdvancedPipeline() throws Exception {
        System.out.println("Example 2: Advanced Data Processing Pipeline with Enrichment");
        System.out.println("Data Flow: Source -> Transform -> Enrich -> Write to DB\n");
        
        // 创建数据源算子
        MySourceOperator source = new MySourceOperator(3, 8);
        
        // 创建转换算子
        TransformOperator transform = new TransformOperator("ENHANCED_", false);
        
        // 准备富化数据
        Map<String, Object> enrichmentData = new HashMap<>();
        enrichmentData.put("system", "PipelineFramework");
        enrichmentData.put("version", "1.0");
        enrichmentData.put("environment", "production");
        
        // 创建富化算子（启用缓存）
        EnrichOperator enrich = new EnrichOperator(enrichmentData, true);
        
        // 创建写库算子
        WriteToDbOperator sink = new WriteToDbOperator("enriched_data", false);
        
        // 构建管道
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(source)
                .addOperator(transform)
                .addOperator(enrich)
                .addOperator(sink);
        
        // 执行管道
        pipeline.run();
        
        // 输出统计信息
        System.out.println("\nPipeline Statistics:");
        System.out.println("- Transform operator processed: " + transform.getProcessedCount() + " records");
        System.out.println("- Enrich operator cache size: " + enrich.getCacheSize() + " entries");
        System.out.println("- Write operator written: " + sink.getTotalWritten() + " records in " + sink.getBatchCount() + " batches");
    }
    
    /**
     * 演示错误处理管道
     * 
     * @throws Exception 管道执行异常
     */
    public static void runErrorHandlingPipeline() throws Exception {
        System.out.println("Example 3: Error Handling Pipeline");
        System.out.println("This example demonstrates how the framework handles errors\n");
        
        // 创建数据源算子
        MySourceOperator source = new MySourceOperator(3, 5);
        
        // 创建转换算子
        TransformOperator transform = new TransformOperator("ERROR_TEST_", true);
        
        // 创建会模拟失败的写库算子
        WriteToDbOperator sink = new WriteToDbOperator("error_test_table", true);
        
        // 构建管道
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(source)
                .addOperator(transform)
                .addOperator(sink);
        
        try {
            // 执行管道（可能会失败）
            pipeline.run();
        } catch (Exception e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }
        
        // 输出统计信息
        System.out.println("\nPipeline Statistics (before failure):");
        System.out.println("- Transform operator processed: " + transform.getProcessedCount() + " records");
        System.out.println("- Write operator written: " + sink.getTotalWritten() + " records in " + sink.getBatchCount() + " batches");
    }
}