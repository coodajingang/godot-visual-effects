package com.dus.pipeline;

import com.dus.pipeline.core.PipelineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PipelineContext 测试
 */
public class PipelineContextTest {
    
    private PipelineContext context;
    
    @BeforeEach
    void setUp() {
        context = new PipelineContext();
    }
    
    @Test
    @DisplayName("上下文自动初始化")
    void testContextAutoInitialization() {
        // Then
        assertNotNull(context.getRunId());
        assertTrue(context.getRunId().startsWith("pipeline-"));
        assertTrue(context.getStartTime() > 0);
        assertEquals(0, context.getBatchCount());
        assertEquals(0, context.getTotalRecordCount());
        assertTrue(context.getProperties().isEmpty());
    }
    
    @Test
    @DisplayName("记录运行时间")
    void testElapsedTimeRecording() throws InterruptedException {
        // Given
        long startTime = context.getStartTime();
        
        // When
        Thread.sleep(10); // 等待至少10ms
        
        // Then
        long elapsedTime = context.getElapsedTimeMs();
        assertTrue(elapsedTime >= 10, "Elapsed time should be at least 10ms");
        assertTrue(elapsedTime < 1000, "Elapsed time should be reasonable");
    }
    
    @Test
    @DisplayName("记录批次和数据条数统计")
    void testBatchAndRecordCounting() {
        // Given
        assertEquals(0, context.getBatchCount());
        assertEquals(0, context.getTotalRecordCount());
        
        // When
        context.incrementBatchCount();
        context.addToTotalRecordCount(100);
        
        // Then
        assertEquals(1, context.getBatchCount());
        assertEquals(100, context.getTotalRecordCount());
        
        // When
        context.setBatchCount(5);
        context.setTotalRecordCount(500);
        
        // Then
        assertEquals(5, context.getBatchCount());
        assertEquals(500, context.getTotalRecordCount());
    }
    
    @Test
    @DisplayName("自定义属性存储")
    void testCustomProperties() {
        // Given
        assertFalse(context.hasProperty("test"));
        assertNull(context.getProperty("test"));
        
        // When
        context.setProperty("test", "value");
        context.setProperty("number", 42);
        
        // Then
        assertTrue(context.hasProperty("test"));
        assertEquals("value", context.getProperty("test"));
        assertEquals(42, context.getProperty("number"));
        
        // When
        context.removeProperty("test");
        
        // Then
        assertFalse(context.hasProperty("test"));
        assertNull(context.getProperty("test"));
        assertEquals(42, context.getProperty("number"));
    }
    
    @Test
    @DisplayName("属性映射的副本返回")
    void testPropertiesCopyReturned() {
        // Given
        context.setProperty("key1", "value1");
        context.setProperty("key2", "value2");
        
        // When
        var properties = context.getProperties();
        properties.put("key3", "value3"); // 修改返回的映射
        
        // Then
        assertEquals(2, context.getProperties().size()); // 原始上下文不应该被修改
        assertFalse(context.hasProperty("key3"));
    }
    
    @Test
    @DisplayName("自定义运行ID设置")
    void testCustomRunId() {
        // Given
        String originalRunId = context.getRunId();
        
        // When
        context.setRunId("custom-run-id");
        
        // Then
        assertEquals("custom-run-id", context.getRunId());
        assertNotEquals(originalRunId, context.getRunId());
    }
    
    @Test
    @DisplayName("自定义启动时间设置")
    void testCustomStartTime() {
        // Given
        long originalStartTime = context.getStartTime();
        long customTime = 1234567890L;
        
        // When
        context.setStartTime(customTime);
        
        // Then
        assertEquals(customTime, context.getStartTime());
        assertNotEquals(originalStartTime, context.getStartTime());
    }
    
    @Test
    @DisplayName("toString 方法输出")
    void testToStringOutput() {
        // Given
        context.setProperty("test", "value");
        context.incrementBatchCount();
        context.addToTotalRecordCount(100);
        
        // When
        String output = context.toString();
        
        // Then
        assertTrue(output.contains(context.getRunId()));
        assertTrue(output.contains("batchCount=1"));
        assertTrue(output.contains("totalRecordCount=100"));
        assertTrue(output.contains("test=value"));
    }
}