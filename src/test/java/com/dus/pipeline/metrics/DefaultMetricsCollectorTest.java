package com.dus.pipeline.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultMetricsCollector 单元测试
 * 验证指标收集器的功能和线程安全性
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("DefaultMetricsCollector 测试")
class DefaultMetricsCollectorTest {
    
    private DefaultMetricsCollector collector;
    
    @BeforeEach
    void setUp() {
        collector = new DefaultMetricsCollector();
    }
    
    @AfterEach
    void tearDown() {
        if (collector != null) {
            collector.reset();
        }
    }
    
    @Test
    @DisplayName("记录单个算子的成功调用次数")
    void testRecordSuccess() {
        // Given
        String operatorName = "TestOperator";
        long duration = 1_000_000;
        
        // When
        collector.recordSuccess(operatorName, duration);
        
        // Then
        OperatorMetrics metrics = collector.getOperatorMetrics(operatorName);
        assertThat(metrics).isNotNull();
        assertThat(metrics.getOperatorName()).isEqualTo(operatorName);
        assertThat(metrics.getSuccessCount()).isEqualTo(1);
        assertThat(metrics.getFailureCount()).isEqualTo(0);
        assertThat(metrics.getTotalCount()).isEqualTo(1);
        assertThat(metrics.getTotalDurationNanos()).isEqualTo(duration);
    }
    
    @Test
    @DisplayName("记录单个算子的失败次数")
    void testRecordFailure() {
        // Given
        String operatorName = "FailingOperator";
        long duration = 500_000;
        
        // When
        collector.recordFailure(operatorName, duration);
        
        // Then
        OperatorMetrics metrics = collector.getOperatorMetrics(operatorName);
        assertThat(metrics).isNotNull();
        assertThat(metrics.getSuccessCount()).isEqualTo(0);
        assertThat(metrics.getFailureCount()).isEqualTo(1);
        assertThat(metrics.getTotalCount()).isEqualTo(1);
        assertThat(metrics.getTotalDurationNanos()).isEqualTo(duration);
    }
    
    @Test
    @DisplayName("getAllMetrics() 返回所有算子统计")
    void testGetAllMetrics() {
        // Given
        collector.recordSuccess("Operator1", 1_000_000);
        collector.recordSuccess("Operator2", 2_000_000);
        collector.recordFailure("Operator1", 500_000);
        collector.recordSuccess("Operator3", 1_500_000);
        
        // When
        Map<String, OperatorMetrics> allMetrics = collector.getAllMetrics();
        
        // Then
        assertThat(allMetrics).hasSize(3);
        assertThat(allMetrics).containsKeys("Operator1", "Operator2", "Operator3");
        
        // 验证Operator1的指标
        OperatorMetrics op1Metrics = allMetrics.get("Operator1");
        assertThat(op1Metrics.getSuccessCount()).isEqualTo(1);
        assertThat(op1Metrics.getFailureCount()).isEqualTo(1);
        assertThat(op1Metrics.getTotalDurationNanos()).isEqualTo(1_500_000);
        
        // 验证Operator2的指标
        OperatorMetrics op2Metrics = allMetrics.get("Operator2");
        assertThat(op2Metrics.getSuccessCount()).isEqualTo(1);
        assertThat(op2Metrics.getFailureCount()).isEqualTo(0);
        assertThat(op2Metrics.getTotalDurationNanos()).isEqualTo(2_000_000);
        
        // 验证Operator3的指标
        OperatorMetrics op3Metrics = allMetrics.get("Operator3");
        assertThat(op3Metrics.getSuccessCount()).isEqualTo(1);
        assertThat(op3Metrics.getFailureCount()).isEqualTo(0);
        assertThat(op3Metrics.getTotalDurationNanos()).isEqualTo(1_500_000);
    }
    
    @Test
    @DisplayName("reset() 清空统计数据")
    void testReset() {
        // Given - 先添加一些数据
        collector.recordSuccess("Operator1", 1_000_000);
        collector.recordFailure("Operator2", 500_000);
        collector.recordSuccess("Operator1", 2_000_000);
        
        // 验证数据存在
        assertThat(collector.getAllMetrics()).hasSize(2);
        
        // When
        collector.reset();
        
        // Then
        assertThat(collector.getAllMetrics()).isEmpty();
        assertThat(collector.getOperatorMetrics("Operator1")).isNull();
        assertThat(collector.getOperatorMetrics("Operator2")).isNull();
    }
    
    @Test
    @DisplayName("多线程并发记录（线程安全性）")
    void testConcurrentRecording() throws InterruptedException {
        // Given
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // When - 并发记录不同算子的指标
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String operatorName = "Operator" + (threadId % 3); // 3个不同的算子
                        long duration = (threadId * operationsPerThread + j) * 1000;
                        
                        if (j % 3 == 0) {
                            collector.recordSuccess(operatorName, duration);
                        } else {
                            collector.recordFailure(operatorName, duration);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
        
        // Then
        Map<String, OperatorMetrics> allMetrics = collector.getAllMetrics();
        assertThat(allMetrics).hasSize(3); // Operator0, Operator1, Operator2
        
        // 验证总操作数
        int totalOperations = allMetrics.values().stream()
            .mapToInt(OperatorMetrics::getTotalCount)
            .sum();
        assertThat(totalOperations).isEqualTo(threadCount * operationsPerThread);
        
        // 验证每个算子都有数据
        for (int i = 0; i < 3; i++) {
            String operatorName = "Operator" + i;
            OperatorMetrics metrics = allMetrics.get(operatorName);
            assertThat(metrics).isNotNull();
            assertThat(metrics.getTotalCount()).isGreaterThan(0);
            assertThat(metrics.getTotalDurationNanos()).isGreaterThan(0);
        }
    }
    
    @Test
    @DisplayName("获取不存在的算子指标")
    void testGetNonExistentOperatorMetrics() {
        // When
        OperatorMetrics metrics = collector.getOperatorMetrics("NonExistentOperator");
        
        // Then
        assertThat(metrics).isNull();
    }
    
    @Test
    @DisplayName("多个算子的混合记录")
    void testMixedOperatorRecording() {
        // Given
        String[] operators = {"Source", "Transform", "Sink"};
        long[][] durations = {
            {1_000_000, 2_000_000, 1_500_000}, // Source durations
            {500_000, 750_000, 600_000},       // Transform durations  
            {2_000_000, 1_800_000, 2_200_000}  // Sink durations
        };
        
        // When
        for (int i = 0; i < operators.length; i++) {
            for (int j = 0; j < durations[i].length; j++) {
                if (j % 2 == 0) {
                    collector.recordSuccess(operators[i], durations[i][j]);
                } else {
                    collector.recordFailure(operators[i], durations[i][j]);
                }
            }
        }
        
        // Then
        Map<String, OperatorMetrics> allMetrics = collector.getAllMetrics();
        assertThat(allMetrics).hasSize(3);
        
        // 验证Source
        OperatorMetrics sourceMetrics = allMetrics.get("Source");
        assertThat(sourceMetrics.getSuccessCount()).isEqualTo(2);
        assertThat(sourceMetrics.getFailureCount()).isEqualTo(1);
        assertThat(sourceMetrics.getTotalDurationNanos()).isEqualTo(4_500_000);
        
        // 验证Transform
        OperatorMetrics transformMetrics = allMetrics.get("Transform");
        assertThat(transformMetrics.getSuccessCount()).isEqualTo(1);
        assertThat(transformMetrics.getFailureCount()).isEqualTo(2);
        assertThat(transformMetrics.getTotalDurationNanos()).isEqualTo(1_850_000);
        
        // 验证Sink
        OperatorMetrics sinkMetrics = allMetrics.get("Sink");
        assertThat(sinkMetrics.getSuccessCount()).isEqualTo(2);
        assertThat(sinkMetrics.getFailureCount()).isEqualTo(1);
        assertThat(sinkMetrics.getTotalDurationNanos()).isEqualTo(6_000_000);
    }
    
    @Test
    @DisplayName("printMetricsReport() 输出格式验证")
    void testPrintMetricsReportOutput() {
        // Given
        collector.recordSuccess("TestOperator", 1_000_000);
        collector.recordFailure("TestOperator", 500_000);
        collector.recordSuccess("TestOperator", 2_000_000);
        
        // When & Then - 验证不会抛出异常
        assertThatCode(() -> collector.printMetricsReport()).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("高并发下的指标一致性")
    void testHighConcurrencyConsistency() throws InterruptedException {
        // Given
        int threadCount = 50;
        int operationsPerThread = 200;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String operatorName = "Operator" + (threadId % 5);
                        long duration = (long)(Math.random() * 10_000_000); // 0-10ms
                        
                        if (Math.random() > 0.2) {
                            collector.recordSuccess(operatorName, duration);
                        } else {
                            collector.recordFailure(operatorName, duration);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
        
        // Then
        Map<String, OperatorMetrics> allMetrics = collector.getAllMetrics();
        assertThat(allMetrics).hasSize(5);
        
        // 验证数据一致性
        int totalSuccess = allMetrics.values().stream().mapToInt(OperatorMetrics::getSuccessCount).sum();
        int totalFailure = allMetrics.values().stream().mapToInt(OperatorMetrics::getFailureCount).sum();
        int totalCount = allMetrics.values().stream().mapToInt(OperatorMetrics::getTotalCount).sum();
        
        assertThat(totalCount).isEqualTo(totalSuccess + totalFailure);
        assertThat(totalCount).isEqualTo(threadCount * operationsPerThread);
    }
    
    @Test
    @DisplayName("空收集器的操作")
    void testEmptyCollectorOperations() {
        // Given - 空收集器
        
        // When & Then
        assertThat(collector.getAllMetrics()).isEmpty();
        assertThat(collector.getOperatorMetrics("AnyOperator")).isNull();
        
        // 重置空收集器应该不会抛出异常
        assertThatCode(() -> collector.reset()).doesNotThrowAnyException();
        
        // 打印空报告应该不会抛出异常
        assertThatCode(() -> collector.printMetricsReport()).doesNotThrowAnyException();
    }
}