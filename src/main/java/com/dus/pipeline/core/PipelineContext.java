package com.dus.pipeline.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Pipeline 运行上下文，存储状态和配置信息
 */
public class PipelineContext {
    
    // 运行 ID，用于关联日志
    private String runId;
    
    // 启动时间
    private long startTime;
    
    // 用户自定义参数
    private Map<String, Object> properties = new HashMap<>();
    
    // Pipeline 处理的批次数
    private long batchCount;
    
    // Pipeline 处理的总数据条数
    private long totalRecordCount;
    
    public PipelineContext() {
        this.runId = generateRunId();
        this.startTime = System.currentTimeMillis();
    }
    
    private String generateRunId() {
        return "pipeline-" + System.currentTimeMillis() + "-" + System.nanoTime();
    }
    
    // 获取属性
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    public void removeProperty(String key) {
        properties.remove(key);
    }
    
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }
    
    // 获取运行信息
    public String getRunId() {
        return runId;
    }
    
    public void setRunId(String runId) {
        this.runId = runId;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getElapsedTimeMs() {
        return System.currentTimeMillis() - startTime;
    }
    
    // 获取统计信息
    public long getBatchCount() {
        return batchCount;
    }
    
    public void setBatchCount(long batchCount) {
        this.batchCount = batchCount;
    }
    
    public void incrementBatchCount() {
        this.batchCount++;
    }
    
    public long getTotalRecordCount() {
        return totalRecordCount;
    }
    
    public void setTotalRecordCount(long totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }
    
    public void addToTotalRecordCount(long count) {
        this.totalRecordCount += count;
    }
    
    @Override
    public String toString() {
        return String.format("PipelineContext{runId='%s', startTime=%d, elapsedTimeMs=%d, batchCount=%d, totalRecordCount=%d, properties=%s}",
                runId, startTime, getElapsedTimeMs(), batchCount, totalRecordCount, properties);
    }
}