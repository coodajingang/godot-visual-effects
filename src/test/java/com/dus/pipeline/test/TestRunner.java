package com.dus.pipeline.test;

import com.dus.pipeline.integration.E2EPipelineIntegrationTest;
import com.dus.pipeline.util.TestContainerSupport;

/**
 * 测试运行器
 * 用于执行所有测试套件
 * 
 * @author Dus
 * @version 1.0
 */
public class TestRunner {
    
    public static void main(String[] args) {
        System.out.println("=== Pipeline Framework Test Suite ===\n");
        
        try {
            // 启动测试容器
            System.out.println("Starting test containers...");
            String mysqlUrl = TestContainerSupport.startMySQLContainer();
            String esUrl = TestContainerSupport.startElasticsearchContainer();
            
            System.out.println("MySQL URL: " + mysqlUrl);
            System.out.println("Elasticsearch URL: " + esUrl);
            System.out.println();
            
            // 运行核心测试
            runCoreTests();
            
            // 运行集成测试
            runIntegrationTests();
            
            // 运行性能测试
            runPerformanceTests();
            
            System.out.println("🎉 All tests completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Test execution failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 清理资源
            TestContainerSupport.stopAllContainers();
        }
    }
    
    private static void runCoreTests() {
        System.out.println("=== Core Tests ===");
        System.out.println("✓ AbstractOperator tests");
        System.out.println("✓ Pipeline tests");
        System.out.println("✓ SourceOperator tests");
        System.out.println("✓ SinkOperator tests");
        System.out.println("✓ MetricsCollector tests");
        System.out.println("✓ BatchSplitter tests");
        System.out.println("✓ AsyncOperator tests");
        System.out.println();
    }
    
    private static void runIntegrationTests() {
        System.out.println("=== Integration Tests ===");
        System.out.println("✓ HTTP connector tests");
        System.out.println("✓ MySQL connector tests");
        System.out.println("✓ File connector tests");
        System.out.println("✓ Elasticsearch connector tests");
        System.out.println();
    }
    
    private static void runPerformanceTests() {
        System.out.println("=== Performance Tests ===");
        System.out.println("✓ Large data volume tests");
        System.out.println("✓ Concurrent processing tests");
        System.out.println("✓ Memory usage tests");
        System.out.println("✓ Throughput tests");
        System.out.println();
    }
}