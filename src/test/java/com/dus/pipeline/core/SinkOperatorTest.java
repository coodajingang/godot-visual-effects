package com.dus.pipeline.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * SinkOperator 单元测试
 * 验证数据写入算子的模板方法模式和写入逻辑
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("SinkOperator 测试")
class SinkOperatorTest {
    
    private TestSinkOperator sinkOperator;
    
    @BeforeEach
    void setUp() {
        sinkOperator = new TestSinkOperator();
    }
    
    @Test
    @DisplayName("SinkOperator 模板方法流程：before -> write -> after")
    void testTemplateMethodExecutionOrder() throws Exception {
        // Given
        String inputData = "test data";
        
        // When
        Void result = sinkOperator.process(inputData);
        
        // Then
        assertThat(result).isNull(); // SinkOperator 总是返回null
        assertThat(sinkOperator.isBeforeCalled()).isTrue();
        assertThat(sinkOperator.isWriteCalled()).isTrue();
        assertThat(sinkOperator.isAfterCalled()).isTrue();
        assertThat(sinkOperator.getWrittenData()).isEqualTo(inputData);
        
        // 验证执行顺序
        assertThat(sinkOperator.getExecutionOrder()).isEqualTo("before-write-after");
    }
    
    @Test
    @DisplayName("write() 方法被正确调用")
    void testWriteMethodCalled() throws Exception {
        // Given
        String testData = "important data to write";
        
        // When
        sinkOperator.process(testData);
        
        // Then
        assertThat(sinkOperator.getWrittenData()).isEqualTo(testData);
        assertThat(sinkOperator.getWriteCallCount()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("多次写入数据")
    void testMultipleWrites() throws Exception {
        // Given
        String[] testData = {"data1", "data2", "data3"};
        
        // When
        for (String data : testData) {
            sinkOperator.process(data);
        }
        
        // Then
        assertThat(sinkOperator.getWriteCallCount()).isEqualTo(3);
        assertThat(sinkOperator.getAllWrittenData()).containsExactly(testData);
    }
    
    @Test
    @DisplayName("before() 抛异常时处理")
    void testBeforeException() throws Exception {
        // Given
        BeforeFailingSinkOperator sink = new BeforeFailingSinkOperator();
        String testData = "test data";
        
        // When & Then
        assertThatThrownBy(() -> sink.process(testData))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Before method failed");
        
        assertThat(sink.isBeforeCalled()).isTrue();
        assertThat(sink.isWriteCalled()).isFalse();
        assertThat(sink.isAfterCalled()).isFalse();
    }
    
    @Test
    @DisplayName("write() 抛异常时处理")
    void testWriteException() throws Exception {
        // Given
        WriteFailingSinkOperator sink = new WriteFailingSinkOperator();
        String testData = "test data";
        
        // When & Then
        assertThatThrownBy(() -> sink.process(testData))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Write operation failed");
        
        assertThat(sink.isBeforeCalled()).isTrue();
        assertThat(sink.isWriteCalled()).isTrue();
        assertThat(sink.isAfterCalled()).isFalse();
    }
    
    @Test
    @DisplayName("after() 抛异常时处理")
    void testAfterException() throws Exception {
        // Given
        AfterFailingSinkOperator sink = new AfterFailingSinkOperator();
        String testData = "test data";
        
        // When & Then
        assertThatThrownBy(() -> sink.process(testData))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("After method failed");
        
        assertThat(sink.isBeforeCalled()).isTrue();
        assertThat(sink.isWriteCalled()).isTrue();
        assertThat(sink.isAfterCalled()).isTrue();
        assertThat(sink.getWrittenData()).isEqualTo(testData); // 写入应该成功
    }
    
    @Test
    @DisplayName("处理null数据")
    void testNullDataHandling() throws Exception {
        // When
        Void result = sinkOperator.process(null);
        
        // Then
        assertThat(result).isNull();
        assertThat(sinkOperator.getWrittenData()).isNull();
        assertThat(sinkOperator.getWriteCallCount()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("name() 默认返回类名")
    void testDefaultName() {
        // When
        String name = sinkOperator.name();
        
        // Then
        assertThat(name).isEqualTo("TestSinkOperator");
    }
    
    @Test
    @DisplayName("自定义 name() 实现")
    void testCustomName() {
        // Given
        CustomNameSinkOperator sink = new CustomNameSinkOperator();
        
        // When
        String name = sink.name();
        
        // Then
        assertThat(name).isEqualTo("CustomSink");
    }
    
    @Test
    @DisplayName("批量写入操作")
    void testBatchWriteOperation() throws Exception {
        // Given
        BatchSinkOperator batchSink = new BatchSinkOperator(3); // 批量大小为3
        
        // When
        batchSink.process("data1");
        batchSink.process("data2");
        batchSink.process("data3");
        
        // Then
        assertThat(batchSink.getWriteCallCount()).isEqualTo(1); // 只有一次批量写入
        assertThat(batchSink.getBatchedData()).containsExactly("data1", "data2", "data3");
    }
    
    // 测试辅助类
    
    private static class TestSinkOperator extends SinkOperator<String> {
        private boolean beforeCalled = false;
        private boolean writeCalled = false;
        private boolean afterCalled = false;
        private String executionOrder = "";
        private String writtenData;
        private int writeCallCount = 0;
        private java.util.List<String> allWrittenData = new java.util.ArrayList<>();
        
        @Override
        protected void before(String input) throws Exception {
            beforeCalled = true;
            executionOrder += "before-";
        }
        
        @Override
        protected void write(String input) throws Exception {
            writeCalled = true;
            executionOrder += "write-";
            writtenData = input;
            writeCallCount++;
            allWrittenData.add(input);
        }
        
        @Override
        protected void after(String input, Void output) throws Exception {
            afterCalled = true;
            executionOrder += "after";
        }
        
        // Getter methods for testing
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isWriteCalled() { return writeCalled; }
        public boolean isAfterCalled() { return afterCalled; }
        public String getExecutionOrder() { return executionOrder; }
        public String getWrittenData() { return writtenData; }
        public int getWriteCallCount() { return writeCallCount; }
        public java.util.List<String> getAllWrittenData() { return new java.util.ArrayList<>(allWrittenData); }
    }
    
    private static class BeforeFailingSinkOperator extends SinkOperator<String> {
        private boolean beforeCalled = false;
        private boolean writeCalled = false;
        private boolean afterCalled = false;
        
        @Override
        protected void before(String input) throws Exception {
            beforeCalled = true;
            throw new RuntimeException("Before method failed");
        }
        
        @Override
        protected void write(String input) throws Exception {
            writeCalled = true;
        }
        
        @Override
        protected void after(String input, Void output) throws Exception {
            afterCalled = true;
        }
        
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isWriteCalled() { return writeCalled; }
        public boolean isAfterCalled() { return afterCalled; }
    }
    
    private static class WriteFailingSinkOperator extends SinkOperator<String> {
        private boolean beforeCalled = false;
        private boolean writeCalled = false;
        private boolean afterCalled = false;
        
        @Override
        protected void before(String input) throws Exception {
            beforeCalled = true;
        }
        
        @Override
        protected void write(String input) throws Exception {
            writeCalled = true;
            throw new RuntimeException("Write operation failed");
        }
        
        @Override
        protected void after(String input, Void output) throws Exception {
            afterCalled = true;
        }
        
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isWriteCalled() { return writeCalled; }
        public boolean isAfterCalled() { return afterCalled; }
    }
    
    private static class AfterFailingSinkOperator extends SinkOperator<String> {
        private boolean beforeCalled = false;
        private boolean writeCalled = false;
        private boolean afterCalled = false;
        private String writtenData;
        
        @Override
        protected void before(String input) throws Exception {
            beforeCalled = true;
        }
        
        @Override
        protected void write(String input) throws Exception {
            writeCalled = true;
            writtenData = input;
        }
        
        @Override
        protected void after(String input, Void output) throws Exception {
            afterCalled = true;
            throw new RuntimeException("After method failed");
        }
        
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isWriteCalled() { return writeCalled; }
        public boolean isAfterCalled() { return afterCalled; }
        public String getWrittenData() { return writtenData; }
    }
    
    private static class CustomNameSinkOperator extends SinkOperator<String> {
        @Override
        protected void write(String input) throws Exception {
            // 简单的写入实现
        }
        
        @Override
        public String name() {
            return "CustomSink";
        }
    }
    
    private static class BatchSinkOperator extends SinkOperator<String> {
        private final int batchSize;
        private java.util.List<String> batchData = new java.util.ArrayList<>();
        private int writeCallCount = 0;
        
        public BatchSinkOperator(int batchSize) {
            this.batchSize = batchSize;
        }
        
        @Override
        protected void write(String input) throws Exception {
            batchData.add(input);
            
            if (batchData.size() >= batchSize) {
                // 模拟批量写入
                writeCallCount++;
                batchData.clear();
            }
        }
        
        public int getWriteCallCount() { return writeCallCount; }
        public java.util.List<String> getBatchedData() { return new java.util.ArrayList<>(batchData); }
    }
}