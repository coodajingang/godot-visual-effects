package com.dus.pipeline.metrics;

import com.dus.pipeline.core.Pipeline;
import com.dus.pipeline.core.SourceOperator;
import com.dus.pipeline.core.AbstractOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;

import static org.assertj.core.api.Assertions.*;

/**
 * PipelineMetricsIntegrationTest 集成测试
 * 验证Pipeline与MetricsCollector的集成功能
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("Pipeline Metrics 集成测试")
class PipelineMetricsIntegrationTest {
    
    private DefaultMetricsCollector metricsCollector;
    
    @BeforeEach
    void setUp() {
        metricsCollector = new DefaultMetricsCollector();
    }
    
    @AfterEach
    void tearDown() {
        if (metricsCollector != null) {
            metricsCollector.reset();
        }
    }
    
    @Test
    @DisplayName("Pipeline 集成 MetricsCollector")
    void testPipelineWithMetricsCollector() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(3, 2);
        TestOperator operator1 = new TestOperator("Processor1", 10); // 10ms延迟
        TestOperator operator2 = new TestOperator("Processor2", 5);  // 5ms延迟
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source, metricsCollector)
                .addOperator(operator1)
                .addOperator(operator2);
        
        // When
        pipeline.run();
        
        // Then
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        
        // 验证指标收集
        var allMetrics = metricsCollector.getAllMetrics();
        assertThat(allMetrics).hasSize(3); // Source + 2个算子
        
        // 验证Source指标
        var sourceMetrics = metricsCollector.getOperatorMetrics(source.name());
        assertThat(sourceMetrics).isNotNull();
        assertThat(sourceMetrics.getSuccessCount()).isEqualTo(4); // 3次数据 + 1次null
        assertThat(sourceMetrics.getFailureCount()).isEqualTo(0);
        assertThat(sourceMetrics.getTotalDurationNanos()).isGreaterThan(0);
        
        // 验证Operator1指标
        var op1Metrics = metricsCollector.getOperatorMetrics(operator1.name());
        assertThat(op1Metrics).isNotNull();
        assertThat(op1Metrics.getSuccessCount()).isEqualTo(3);
        assertThat(op1Metrics.getFailureCount()).isEqualTo(0);
        assertThat(op1Metrics.getTotalDurationNanos()).isGreaterThan(10_000_000L * 3); // 至少30ms
        
        // 验证Operator2指标
        var op2Metrics = metricsCollector.getOperatorMetrics(operator2.name());
        assertThat(op2Metrics).isNotNull();
        assertThat(op2Metrics.getSuccessCount()).isEqualTo(3);
        assertThat(op2Metrics.getFailureCount()).isEqualTo(0);
        assertThat(op2Metrics.getTotalDurationNanos()).isGreaterThan(5_000_000L * 3); // 至少15ms
    }
    
    @Test
    @DisplayName("完整算子链的性能报告")
    void testCompleteOperatorChainPerformanceReport() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(5, 3);
        TestOperator transform = new TestOperator("Transform", 2);
        TestOperator enrich = new TestOperator("Enrich", 8);
        TestSinkOperator sink = new TestSinkOperator("Sink", 1);
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source, metricsCollector)
                .addOperator(transform)
                .addOperator(enrich)
                .addOperator(sink);
        
        // When
        long startTime = System.currentTimeMillis();
        pipeline.run();
        long endTime = System.currentTimeMillis();
        
        // Then
        var allMetrics = metricsCollector.getAllMetrics();
        assertThat(allMetrics).hasSize(4);
        
        // 验证总执行时间
        long totalPipelineTime = endTime - startTime;
        assertThat(totalPipelineTime).isGreaterThan(0);
        
        // 验证每个算子的执行时间
        var sourceMetrics = metricsCollector.getOperatorMetrics(source.name());
        var transformMetrics = metricsCollector.getOperatorMetrics(transform.name());
        var enrichMetrics = metricsCollector.getOperatorMetrics(enrich.name());
        var sinkMetrics = metricsCollector.getOperatorMetrics(sink.name());
        
        // 验证调用次数
        assertThat(sourceMetrics.getSuccessCount()).isEqualTo(6); // 5次数据 + 1次null
        assertThat(transformMetrics.getSuccessCount()).isEqualTo(5);
        assertThat(enrichMetrics.getSuccessCount()).isEqualTo(5);
        assertThat(sinkMetrics.getSuccessCount()).isEqualTo(5);
        
        // 验证没有失败
        assertThat(allMetrics.values()).allMatch(metrics -> metrics.getFailureCount() == 0);
        
        // 验证处理时间符合预期（考虑并发和调度开销）
        assertThat(transformMetrics.getAverageDurationNanos()).isGreaterThan(2_000_000L);
        assertThat(enrichMetrics.getAverageDurationNanos()).isGreaterThan(8_000_000L);
        assertThat(sinkMetrics.getAverageDurationNanos()).isGreaterThan(1_000_000L);
    }
    
    @Test
    @DisplayName("Metrics 中记录的数据与实际执行一致")
    void testMetricsConsistencyWithActualExecution() throws Exception {
        // Given
        int batchCount = 4;
        int batchSize = 2;
        TestSourceOperator source = new TestSourceOperator(batchCount, batchSize);
        CountingOperator countingOperator = new CountingOperator("Counter");
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source, metricsCollector)
                .addOperator(countingOperator);
        
        // When
        pipeline.run();
        
        // Then
        // 验证实际执行次数
        assertThat(countingOperator.getProcessCallCount()).isEqualTo(batchCount);
        assertThat(countingOperator.getProcessedItems()).isEqualTo(batchCount * batchSize);
        
        // 验证Metrics记录的次数
        var operatorMetrics = metricsCollector.getOperatorMetrics(countingOperator.name());
        assertThat(operatorMetrics).isNotNull();
        assertThat(operatorMetrics.getSuccessCount()).isEqualTo(batchCount);
        assertThat(operatorMetrics.getFailureCount()).isEqualTo(0);
        
        // 验证Source的调用次数
        var sourceMetrics = metricsCollector.getOperatorMetrics(source.name());
        assertThat(sourceMetrics.getSuccessCount()).isEqualTo(batchCount + 1); // +1 for null
    }
    
    @Test
    @DisplayName("printMetricsReport() 输出格式验证")
    void testPrintMetricsReportOutput() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(2, 3);
        TestOperator operator1 = new TestOperator("Processor1", 5);
        TestOperator operator2 = new TestOperator("Processor2", 10);
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source, metricsCollector)
                .addOperator(operator1)
                .addOperator(operator2);
        
        // When
        pipeline.run();
        
        // Then - 验证报告输出不会抛出异常
        assertThatCode(() -> pipeline.printMetricsReport()).doesNotThrowAnyException();
        
        // 验证MetricsCollector的独立报告输出
        assertThatCode(() -> metricsCollector.printMetricsReport()).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("异常情况下的指标记录")
    void testMetricsRecordingWithExceptions() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(3, 2);
        TestOperator normalOperator = new TestOperator("Normal", 1);
        FailingOperator failingOperator = new FailingOperator("Failing", 2);
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source, metricsCollector)
                .addOperator(normalOperator)
                .addOperator(failingOperator);
        
        // When & Then
        assertThatThrownBy(() -> pipeline.run())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Simulated failure");
        
        // 验证指标记录
        var sourceMetrics = metricsCollector.getOperatorMetrics(source.name());
        var normalMetrics = metricsCollector.getOperatorMetrics(normalOperator.name());
        var failingMetrics = metricsCollector.getOperatorMetrics(failingOperator.name());
        
        // Source应该有成功记录
        assertThat(sourceMetrics.getSuccessCount()).isGreaterThan(0);
        assertThat(sourceMetrics.getFailureCount()).isEqualTo(0);
        
        // Normal算子应该有成功记录（在失败之前）
        assertThat(normalMetrics.getSuccessCount()).isGreaterThan(0);
        assertThat(normalMetrics.getFailureCount()).isEqualTo(0);
        
        // Failing算子应该有失败记录
        assertThat(failingMetrics.getSuccessCount()).isEqualTo(0);
        assertThat(failingMetrics.getFailureCount()).isEqualTo(1);
        assertThat(failingMetrics.getTotalDurationNanos()).isGreaterThan(2_000_000L); // 至少2ms
    }
    
    @Test
    @DisplayName("多次运行管道的指标累积")
    void testMultiplePipelineRunsMetricAccumulation() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(2, 2);
        TestOperator operator = new TestOperator("Processor", 3);
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source, metricsCollector)
                .addOperator(operator);
        
        // When - 运行多次
        pipeline.run();
        
        // 创建新的Pipeline实例但使用同一个MetricsCollector
        TestSourceOperator source2 = new TestSourceOperator(1, 3);
        TestOperator operator2 = new TestOperator("Processor", 4);
        Pipeline<String, Void> pipeline2 = new Pipeline<>(source2, metricsCollector)
                .addOperator(operator2);
        pipeline2.run();
        
        // Then
        var processorMetrics = metricsCollector.getOperatorMetrics("Processor");
        assertThat(processorMetrics).isNotNull();
        assertThat(processorMetrics.getSuccessCount()).isEqualTo(3); // 2 + 1
        assertThat(processorMetrics.getFailureCount()).isEqualTo(0);
        
        // 验证两个Source的指标都被记录
        var allMetrics = metricsCollector.getAllMetrics();
        long sourceCount = allMetrics.keySet().stream()
            .filter(name -> name.contains("TestSourceOperator"))
            .count();
        assertThat(sourceCount).isEqualTo(2); // 两个不同的Source实例
    }
    
    // 测试辅助类
    
    private static class TestSourceOperator extends SourceOperator<String> {
        private final int maxBatches;
        private final int batchSize;
        private int currentBatch = 0;
        
        public TestSourceOperator(int maxBatches, int batchSize) {
            this.maxBatches = maxBatches;
            this.batchSize = batchSize;
        }
        
        @Override
        protected String doNextBatch() throws Exception {
            if (currentBatch >= maxBatches) {
                return null;
            }
            
            StringBuilder batch = new StringBuilder();
            for (int i = 0; i < batchSize; i++) {
                if (i > 0) batch.append(",");
                batch.append("Data_").append(currentBatch).append("_").append(i);
            }
            
            currentBatch++;
            return batch.toString();
        }
    }
    
    private static class TestOperator extends AbstractOperator<String, String> {
        private final String name;
        private final long delayMs;
        
        public TestOperator(String name, long delayMs) {
            this.name = name;
            this.delayMs = delayMs;
        }
        
        @Override
        protected String doProcess(String input) throws Exception {
            if (delayMs > 0) {
                Thread.sleep(delayMs);
            }
            return "processed_" + input;
        }
        
        @Override
        public String name() {
            return name;
        }
    }
    
    private static class CountingOperator extends AbstractOperator<String, String> {
        private final String name;
        private int processCallCount = 0;
        private int processedItems = 0;
        
        public CountingOperator(String name) {
            this.name = name;
        }
        
        @Override
        protected String doProcess(String input) throws Exception {
            processCallCount++;
            if (input != null && !input.isEmpty()) {
                processedItems += input.split(",").length;
            }
            return input;
        }
        
        @Override
        public String name() {
            return name;
        }
        
        public int getProcessCallCount() { return processCallCount; }
        public int getProcessedItems() { return processedItems; }
    }
    
    private static class TestSinkOperator extends AbstractOperator<String, Void> {
        private final String name;
        private final long delayMs;
        
        public TestSinkOperator(String name, long delayMs) {
            this.name = name;
            this.delayMs = delayMs;
        }
        
        @Override
        protected Void doProcess(String input) throws Exception {
            if (delayMs > 0) {
                Thread.sleep(delayMs);
            }
            return null;
        }
        
        @Override
        public String name() {
            return name;
        }
    }
    
    private static class FailingOperator extends AbstractOperator<String, String> {
        private final String name;
        private final long delayMs;
        private int callCount = 0;
        
        public FailingOperator(String name, long delayMs) {
            this.name = name;
            this.delayMs = delayMs;
        }
        
        @Override
        protected String doProcess(String input) throws Exception {
            callCount++;
            if (delayMs > 0) {
                Thread.sleep(delayMs);
            }
            throw new RuntimeException("Simulated failure");
        }
        
        @Override
        public String name() {
            return name;
        }
        
        public int getCallCount() { return callCount; }
    }
}