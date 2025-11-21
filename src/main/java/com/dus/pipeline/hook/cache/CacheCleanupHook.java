package com.dus.pipeline.hook.cache;

import com.dus.pipeline.core.PipelineContext;
import com.dus.pipeline.hook.AfterPipelineHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

/**
 * 缓存清理钩子
 */
public class CacheCleanupHook implements AfterPipelineHook {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheCleanupHook.class);
    
    private JedisPool jedisPool;
    private String cacheKeyPattern;  // e.g., "pipeline:*"
    
    public CacheCleanupHook(JedisPool jedisPool, String cacheKeyPattern) {
        this.jedisPool = jedisPool;
        this.cacheKeyPattern = cacheKeyPattern;
    }
    
    @Override
    public void initialize() throws Exception {
        // 验证参数
        if (jedisPool == null) {
            throw new IllegalArgumentException("JedisPool cannot be null");
        }
        if (cacheKeyPattern == null || cacheKeyPattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key pattern cannot be null or empty");
        }
    }
    
    @Override
    public void afterPipeline(PipelineContext context) throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.keys(cacheKeyPattern);
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
                logger.info("Cache cleaned: {} keys deleted", keys.size());
                context.setProperty("cache_cleaned_keys", keys.size());
            } else {
                logger.info("No cache keys found matching pattern: {}", cacheKeyPattern);
            }
        }
    }
    
    @Override
    public void onPipelineFailure(PipelineContext context, Exception exception) throws Exception {
        // 失败时保留缓存用于调试
        logger.warn("Cache preserved for debugging after pipeline failure. Pattern: {}", cacheKeyPattern);
    }
    
    @Override
    public void onPipelineInterrupted(PipelineContext context) throws Exception {
        // 中断时也清理缓存
        logger.info("Cleaning up cache after pipeline interruption");
        afterPipeline(context);
    }
    
    @Override
    public String name() {
        return "CacheCleanupHook";
    }
}