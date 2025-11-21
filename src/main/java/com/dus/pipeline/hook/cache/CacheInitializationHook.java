package com.dus.pipeline.hook.cache;

import com.dus.pipeline.core.PipelineContext;
import com.dus.pipeline.hook.BeforePipelineHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 缓存初始化钩子
 */
public class CacheInitializationHook implements BeforePipelineHook {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheInitializationHook.class);
    
    private JedisPool jedisPool;
    private String cacheKeyPrefix;
    
    public CacheInitializationHook(JedisPool jedisPool, String cacheKeyPrefix) {
        this.jedisPool = jedisPool;
        this.cacheKeyPrefix = cacheKeyPrefix;
    }
    
    @Override
    public void initialize() throws Exception {
        // 验证参数
        if (jedisPool == null) {
            throw new IllegalArgumentException("JedisPool cannot be null");
        }
        if (cacheKeyPrefix == null || cacheKeyPrefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key prefix cannot be null or empty");
        }
    }
    
    @Override
    public void beforePipeline(PipelineContext context) throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            // 预热缓存或初始化
            String initKey = cacheKeyPrefix + ":initialized";
            jedis.set(initKey, "true");
            jedis.expire(initKey, 3600);  // 1 小时过期
            logger.info("Cache initialized: {}", initKey);
            context.setProperty("cache_initialized", true);
        }
    }
    
    @Override
    public String name() {
        return "CacheInitializationHook";
    }
}