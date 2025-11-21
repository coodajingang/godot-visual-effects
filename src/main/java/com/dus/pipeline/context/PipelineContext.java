package com.dus.pipeline.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline 运行上下文，用于在算子间共享数据和配置
 */
public class PipelineContext {
    
    private final Map<String, Object> properties = new ConcurrentHashMap<>();
    private final long startTime;
    private long batchCount = 0;
    
    public PipelineContext() {
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * 获取上下文属性
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    /**
     * 获取上下文属性（带默认值）
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        Object value = properties.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * 设置上下文属性
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    /**
     * 移除上下文属性
     */
    public Object removeProperty(String key) {
        return properties.remove(key);
    }
    
    /**
     * 检查是否包含某个属性
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * 获取所有属性
     */
    public Map<String, Object> getAllProperties() {
        return new HashMap<>(properties);
    }
    
    /**
     * 清空所有属性
     */
    public void clearProperties() {
        properties.clear();
    }
    
    /**
     * 获取 Pipeline 开始时间
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * 获取当前批次计数
     */
    public long getBatchCount() {
        return batchCount;
    }
    
    /**
     * 增加批次计数
     */
    public void incrementBatchCount() {
        batchCount++;
    }
    
    /**
     * 设置批次计数
     */
    public void setBatchCount(long batchCount) {
        this.batchCount = batchCount;
    }
    
    /**
     * 获取 Pipeline 运行时长（毫秒）
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    @Override
    public String toString() {
        return "PipelineContext{" +
                "properties=" + properties.size() +
                ", startTime=" + startTime +
                ", batchCount=" + batchCount +
                ", elapsedTime=" + getElapsedTime() + "ms" +
                '}';
    }
}