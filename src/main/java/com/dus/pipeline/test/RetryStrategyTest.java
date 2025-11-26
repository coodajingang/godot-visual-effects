package com.dus.pipeline.test;

import com.dus.pipeline.retry.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 重试策略测试
 */
public class RetryStrategyTest {
    
    @Test
    public void testNoRetryStrategy() {
        RetryStrategy strategy = new NoRetryStrategy();
        
        assertFalse(strategy.shouldRetry(1, new RuntimeException("Test")));
        assertEquals(0, strategy.getWaitTimeMs(1));
        assertEquals(1, strategy.getMaxAttempts());
        assertEquals("NoRetryStrategy", strategy.name());
    }
    
    @Test
    public void testFixedDelayRetryStrategy() {
        FixedDelayRetryStrategy strategy = new FixedDelayRetryStrategy(3, 1000);
        
        // 测试重试条件
        assertTrue(strategy.shouldRetry(1, new RuntimeException("Test")));
        assertTrue(strategy.shouldRetry(2, new RuntimeException("Test")));
        assertFalse(strategy.shouldRetry(3, new RuntimeException("Test"))); // 达到最大次数
        
        // 测试等待时间
        assertEquals(1000, strategy.getWaitTimeMs(1));
        assertEquals(1000, strategy.getWaitTimeMs(2));
        assertEquals(1000, strategy.getWaitTimeMs(3));
        
        // 测试基本属性
        assertEquals(3, strategy.getMaxAttempts());
        assertTrue(strategy.name().contains("FixedDelayRetryStrategy"));
    }
    
    @Test
    public void testFixedDelayRetryStrategyWithExceptionFiltering() {
        FixedDelayRetryStrategy strategy = new FixedDelayRetryStrategy(3, 1000);
        strategy.removeRetryableException(Exception.class);
        strategy.addRetryableException(IOException.class);
        
        // 只有 IOException 会重试
        assertTrue(strategy.shouldRetry(1, new IOException("Network error")));
        assertFalse(strategy.shouldRetry(1, new RuntimeException("Runtime error")));
    }
    
    @Test
    public void testExponentialBackoffRetryStrategy() {
        ExponentialBackoffRetryStrategy strategy = new ExponentialBackoffRetryStrategy(
            5, 1000, 10000, 2.0);
        
        // 测试重试条件
        assertTrue(strategy.shouldRetry(1, new RuntimeException("Test")));
        assertTrue(strategy.shouldRetry(4, new RuntimeException("Test")));
        assertFalse(strategy.shouldRetry(5, new RuntimeException("Test"))); // 达到最大次数
        
        // 测试指数增长的等待时间
        assertEquals(1000, strategy.getWaitTimeMs(1)); // 1000 * 2^0
        assertEquals(2000, strategy.getWaitTimeMs(2)); // 1000 * 2^1
        assertEquals(4000, strategy.getWaitTimeMs(3)); // 1000 * 2^2
        assertEquals(8000, strategy.getWaitTimeMs(4)); // 1000 * 2^3
        
        // 测试最大延迟限制
        assertEquals(10000, strategy.getWaitTimeMs(10)); // 超过最大延迟
        
        // 测试基本属性
        assertEquals(5, strategy.getMaxAttempts());
        assertTrue(strategy.name().contains("ExponentialBackoffRetryStrategy"));
    }
    
    @Test
    public void testExponentialBackoffRetryStrategyWithExceptionFiltering() {
        ExponentialBackoffRetryStrategy strategy = new ExponentialBackoffRetryStrategy(
            3, 500, 5000, 2.0);
        strategy.removeRetryableException(Exception.class);
        strategy.addRetryableException(TimeoutException.class);
        
        // 只有 TimeoutException 会重试
        assertTrue(strategy.shouldRetry(1, new TimeoutException()));
        assertFalse(strategy.shouldRetry(1, new RuntimeException()));
    }
    
    @Test
    public void testAdaptiveRetryStrategy() {
        AdaptiveRetryStrategy strategy = new AdaptiveRetryStrategy()
            .withDefaults(2, 500)
            .configureException(RuntimeException.class, 3, 1000)
            .configureException(IllegalArgumentException.class, 1, 0);
        
        // 测试不同异常类型的重试次数
        assertTrue(strategy.shouldRetry(1, new RuntimeException()));
        assertTrue(strategy.shouldRetry(2, new RuntimeException()));
        assertFalse(strategy.shouldRetry(3, new RuntimeException()));
        
        assertTrue(strategy.shouldRetry(1, new IllegalArgumentException()));
        assertFalse(strategy.shouldRetry(2, new IllegalArgumentException()));
        
        // 测试默认重试次数（对于未配置的异常）
        assertTrue(strategy.shouldRetry(1, new IOException()));
        assertFalse(strategy.shouldRetry(3, new IOException()));
        
        // 测试基本属性
        assertEquals(3, strategy.getMaxAttempts()); // 取最大值
        assertEquals("AdaptiveRetryStrategy", strategy.name());
    }
    
    @Test
    public void testAdaptiveRetryStrategyGetWaitTimeForException() {
        AdaptiveRetryStrategy strategy = new AdaptiveRetryStrategy()
            .withDefaults(100, 200)
            .configureException(RuntimeException.class, 3, 1000);
        
        assertEquals(1000, strategy.getWaitTimeForException(new RuntimeException()));
        assertEquals(200, strategy.getWaitTimeForException(new IOException()));
    }
    
    @Test
    public void testRetryStrategyMaxAttemptsReached() {
        FixedDelayRetryStrategy strategy = new FixedDelayRetryStrategy(2, 500);
        
        // 测试达到最大重试次数后的行为
        assertTrue(strategy.shouldRetry(1, new Exception()));
        assertFalse(strategy.shouldRetry(2, new Exception())); // 第2次尝试，达到最大次数
        assertFalse(strategy.shouldRetry(3, new Exception())); // 超过最大次数
    }
}