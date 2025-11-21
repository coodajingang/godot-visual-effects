package com.dus.pipeline.test;

import com.dus.pipeline.retry.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 跳过策略测试
 */
public class SkipStrategyTest {
    
    @Test
    public void testNoSkipStrategy() {
        SkipStrategy strategy = new NoSkipStrategy();
        
        assertFalse(strategy.shouldSkip(1, "test_data", new RuntimeException("Test")));
        assertFalse(strategy.shouldSkip(5, "test_data", new RuntimeException("Test")));
        assertEquals(1, strategy.getMaxAttempts());
        assertEquals("NoSkipStrategy", strategy.name());
    }
    
    @Test
    public void testSkipFailedRecordsStrategy() {
        List<Object> skippedRecords = new ArrayList<>();
        List<Exception> skippedExceptions = new ArrayList<>();
        
        SkipListener listener = (input, exception) -> {
            skippedRecords.add(input);
            skippedExceptions.add(exception);
        };
        
        SkipFailedRecordsStrategy strategy = new SkipFailedRecordsStrategy(3)
            .setSkipListener(listener);
        
        // 测试未达到最大尝试次数
        assertFalse(strategy.shouldSkip(1, "test_data", new RuntimeException("Test")));
        assertFalse(strategy.shouldSkip(2, "test_data", new RuntimeException("Test")));
        
        // 测试达到最大尝试次数，应该跳过
        assertTrue(strategy.shouldSkip(3, "test_data", new RuntimeException("Test")));
        
        // 验证监听器被调用
        assertEquals(1, skippedRecords.size());
        assertEquals("test_data", skippedRecords.get(0));
        assertEquals("Test", skippedExceptions.get(0).getMessage());
        
        // 测试基本属性
        assertEquals(3, strategy.getMaxAttempts());
        assertEquals("SkipFailedRecordsStrategy", strategy.name());
    }
    
    @Test
    public void testSkipFailedRecordsStrategyWithExceptionFiltering() {
        List<Object> skippedRecords = new ArrayList<>();
        
        SkipFailedRecordsStrategy strategy = new SkipFailedRecordsStrategy(2)
            .removeSkippableException(Exception.class)
            .addSkippableException(IllegalArgumentException.class)
            .setSkipListener((input, exception) -> skippedRecords.add(input));
        
        // 只有 IllegalArgumentException 会跳过
        assertTrue(strategy.shouldSkip(2, "test_data", new IllegalArgumentException("Invalid")));
        assertFalse(strategy.shouldSkip(2, "test_data", new RuntimeException("Runtime")));
        
        // 验证只有符合条件的异常被跳过
        assertEquals(1, skippedRecords.size());
    }
    
    @Test
    public void testSkipFailedRecordsStrategyMultipleSkips() {
        List<Object> skippedRecords = new ArrayList<>();
        
        SkipFailedRecordsStrategy strategy = new SkipFailedRecordsStrategy(2)
            .setSkipListener((input, exception) -> skippedRecords.add(input));
        
        // 多次跳过测试
        assertTrue(strategy.shouldSkip(2, "data1", new RuntimeException("Error1")));
        assertTrue(strategy.shouldSkip(2, "data2", new RuntimeException("Error2")));
        
        // 验证所有跳过的记录都被记录
        assertEquals(2, skippedRecords.size());
        assertTrue(skippedRecords.contains("data1"));
        assertTrue(skippedRecords.contains("data2"));
    }
    
    @Test
    public void testSkipFailedRecordsStrategyGetSkippableExceptions() {
        SkipFailedRecordsStrategy strategy = new SkipFailedRecordsStrategy(3)
            .addSkippableException(IllegalArgumentException.class)
            .addSkippableException(NullPointerException.class);
        
        // 验证可跳过的异常类型
        assertEquals(3, strategy.getSkippableExceptions().size()); // 默认 Exception + 2个添加的
        assertTrue(strategy.getSkippableExceptions().contains(Exception.class));
        assertTrue(strategy.getSkippableExceptions().contains(IllegalArgumentException.class));
        assertTrue(strategy.getSkippableExceptions().contains(NullPointerException.class));
    }
    
    @Test
    public void testSkipFailedRecordsStrategyRemoveSkippableException() {
        SkipFailedRecordsStrategy strategy = new SkipFailedRecordsStrategy(2)
            .removeSkippableException(Exception.class)
            .addSkippableException(IllegalArgumentException.class);
        
        // 移除默认的 Exception 后，只有 IllegalArgumentException 可以跳过
        assertTrue(strategy.shouldSkip(2, "data", new IllegalArgumentException("Invalid")));
        assertFalse(strategy.shouldSkip(2, "data", new RuntimeException("Runtime")));
        
        // 验证异常类型列表
        assertEquals(1, strategy.getSkippableExceptions().size());
        assertTrue(strategy.getSkippableExceptions().contains(IllegalArgumentException.class));
    }
    
    @Test
    public void testSkipFailedRecordsStrategyWithoutListener() {
        SkipFailedRecordsStrategy strategy = new SkipFailedRecordsStrategy(2);
        
        // 没有监听器的情况下应该也能正常工作
        assertDoesNotThrow(() -> {
            boolean shouldSkip = strategy.shouldSkip(2, "test_data", new RuntimeException("Test"));
            assertTrue(shouldSkip);
        });
        
        assertNull(strategy.getSkipListener());
    }
    
    @Test
    public void testSkipStrategyMaxAttempts() {
        SkipFailedRecordsStrategy strategy = new SkipFailedRecordsStrategy(5);
        
        assertEquals(5, strategy.getMaxAttempts());
        
        // 测试在达到最大尝试次数之前不会跳过
        for (int i = 1; i < 5; i++) {
            assertFalse(strategy.shouldSkip(i, "data", new RuntimeException("Test")));
        }
        
        // 达到最大尝试次数时跳过
        assertTrue(strategy.shouldSkip(5, "data", new RuntimeException("Test")));
    }
}