package com.dus.pipeline;

import com.dus.pipeline.core.*;
import com.dus.pipeline.hook.cache.CacheCleanupHook;
import com.dus.pipeline.hook.cache.CacheInitializationHook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 缓存钩子集成测试
 */
public class CacheHookIntegrationTest {
    
    @Mock
    private JedisPool mockJedisPool;
    
    @Mock
    private Jedis mockJedis;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockJedisPool.getResource()).thenReturn(mockJedis);
    }
    
    @Test
    @DisplayName("初始化缓存")
    void testInitializeCache() throws Exception {
        // Given
        String cacheKeyPrefix = "pipeline:test";
        CacheInitializationHook hook = new CacheInitializationHook(mockJedisPool, cacheKeyPrefix);
        PipelineContext context = new PipelineContext();
        
        // When
        hook.initialize();
        hook.beforePipeline(context);
        
        // Then
        verify(mockJedis).set(cacheKeyPrefix + ":initialized", "true");
        verify(mockJedis).expire(cacheKeyPrefix + ":initialized", 3600);
        assertEquals(Boolean.TRUE, context.getProperty("cache_initialized"));
    }
    
    @Test
    @DisplayName("Pipeline 完成后清理缓存")
    void testCleanCacheAfterPipelineCompletion() throws Exception {
        // Given
        String cacheKeyPattern = "pipeline:test:*";
        Set<String> mockKeys = new HashSet<>(Arrays.asList("pipeline:test:1", "pipeline:test:2"));
        
        when(mockJedis.keys(cacheKeyPattern)).thenReturn(mockKeys);
        
        CacheCleanupHook hook = new CacheCleanupHook(mockJedisPool, cacheKeyPattern);
        PipelineContext context = new PipelineContext();
        
        // When
        hook.initialize();
        hook.afterPipeline(context);
        
        // Then
        verify(mockJedis).keys(cacheKeyPattern);
        verify(mockJedis).del("pipeline:test:1", "pipeline:test:2");
        assertEquals(2L, context.getProperty("cache_cleaned_keys"));
    }
    
    @Test
    @DisplayName("失败时保留缓存用于调试")
    void testPreserveCacheOnFailure() throws Exception {
        // Given
        String cacheKeyPattern = "pipeline:test:*";
        CacheCleanupHook hook = new CacheCleanupHook(mockJedisPool, cacheKeyPattern);
        PipelineContext context = new PipelineContext();
        Exception testException = new RuntimeException("Test failure");
        
        // When
        hook.initialize();
        hook.onPipelineFailure(context, testException);
        
        // Then
        verify(mockJedis, never()).keys(anyString());
        verify(mockJedis, never()).del(anyString());
    }
    
    @Test
    @DisplayName("中断时清理缓存")
    void testCleanCacheOnInterruption() throws Exception {
        // Given
        String cacheKeyPattern = "pipeline:test:*";
        Set<String> mockKeys = new HashSet<>(Arrays.asList("pipeline:test:1"));
        
        when(mockJedis.keys(cacheKeyPattern)).thenReturn(mockKeys);
        
        CacheCleanupHook hook = new CacheCleanupHook(mockJedisPool, cacheKeyPattern);
        PipelineContext context = new PipelineContext();
        
        // When
        hook.initialize();
        hook.onPipelineInterrupted(context);
        
        // Then
        verify(mockJedis).keys(cacheKeyPattern);
        verify(mockJedis).del("pipeline:test:1");
    }
    
    @Test
    @DisplayName("完整缓存钩子集成测试")
    void testCompleteCacheHookIntegration() throws Exception {
        // Given
        String cacheKeyPrefix = "pipeline:integration";
        String cacheKeyPattern = "pipeline:integration:*";
        Set<String> mockKeys = new HashSet<>(Arrays.asList(
            "pipeline:integration:data1", 
            "pipeline:integration:data2"
        ));
        
        when(mockJedis.keys(cacheKeyPattern)).thenReturn(mockKeys);
        
        CacheInitializationHook initHook = new CacheInitializationHook(mockJedisPool, cacheKeyPrefix);
        CacheCleanupHook cleanupHook = new CacheCleanupHook(mockJedisPool, cacheKeyPattern);
        
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addBeforeHook(initHook)
            .addOperator(new NoOpOperator())
            .addAfterHook(cleanupHook);
        
        // When
        pipeline.run();
        
        // Then
        verify(mockJedis).set(cacheKeyPrefix + ":initialized", "true");
        verify(mockJedis).expire(cacheKeyPrefix + ":initialized", 3600);
        verify(mockJedis).keys(cacheKeyPattern);
        verify(mockJedis).del("pipeline:integration:data1", "pipeline:integration:data2");
    }
    
    @Test
    @DisplayName("缓存连接异常处理")
    void testCacheConnectionException() {
        // Given
        String cacheKeyPrefix = "pipeline:test";
        when(mockJedisPool.getResource()).thenThrow(new JedisConnectionException("Connection failed"));
        
        CacheInitializationHook hook = new CacheInitializationHook(mockJedisPool, cacheKeyPrefix);
        
        // When & Then
        assertThrows(JedisConnectionException.class, () -> {
            hook.initialize();
            hook.beforePipeline(new PipelineContext());
        });
    }
    
    // 测试辅助类
    static class TestSource implements Source<List<String>> {
        private int count = 0;
        
        @Override
        public List<String> nextBatch() {
            return count++ < 1 ? Arrays.asList("test" + count) : null;
        }
        
        @Override
        public String name() {
            return "TestSource";
        }
    }
    
    static class NoOpOperator implements Operator<List<String>, List<String>> {
        @Override
        public List<String> process(List<String> input) {
            return input;
        }
        
        @Override
        public String name() {
            return "NoOpOperator";
        }
    }
}