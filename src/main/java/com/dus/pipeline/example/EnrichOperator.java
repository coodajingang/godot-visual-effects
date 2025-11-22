package com.dus.pipeline.example;

import com.dus.pipeline.core.AbstractOperator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 示例富化算子
 * 对输入数据进行富化处理，如添加额外信息、关联查询等
 * 
 * @author Dus
 * @version 1.0
 */
public class EnrichOperator extends AbstractOperator<List<String>, List<Map<String, Object>>> {
    
    private final Map<String, Object> enrichmentData;
    private final boolean enableCache;
    private final Map<String, Map<String, Object>> cache;
    
    /**
     * 构造函数
     * 
     * @param enrichmentData 富化数据
     * @param enableCache 是否启用缓存
     */
    public EnrichOperator(Map<String, Object> enrichmentData, boolean enableCache) {
        this.enrichmentData = new HashMap<>(enrichmentData);
        this.enableCache = enableCache;
        this.cache = enableCache ? new ConcurrentHashMap<>() : null;
    }
    
    /**
     * 实现具体的富化逻辑
     * 
     * @param input 输入数据列表
     * @return 富化后的数据列表
     * @throws Exception 富化过程中可能抛出的异常
     */
    @Override
    protected List<Map<String, Object>> doProcess(List<String> input) throws Exception {
        if (input == null || input.isEmpty()) {
            return input.stream().map(data -> new HashMap<String, Object>()).collect(Collectors.toList());
        }
        
        // 使用Java8 Stream API进行数据富化
        return input.stream()
                .map(this::enrichSingleRecord)
                .collect(Collectors.toList());
    }
    
    /**
     * 富化单条记录
     * 
     * @param data 原始数据
     * @return 富化后的数据
     */
    private Map<String, Object> enrichSingleRecord(String data) {
        if (enableCache && cache.containsKey(data)) {
            return cache.get(data);
        }
        
        Map<String, Object> enrichedRecord = new HashMap<>();
        enrichedRecord.put("original_data", data);
        enrichedRecord.put("timestamp", System.currentTimeMillis());
        enrichedRecord.put("data_length", data != null ? data.length() : 0);
        
        // 添加富化数据
        enrichedRecord.putAll(enrichmentData);
        
        // 模拟关联查询
        enrichedRecord.put("external_id", "EXT_" + Math.abs(data.hashCode()));
        enrichedRecord.put("category", getCategory(data));
        
        if (enableCache) {
            cache.put(data, enrichedRecord);
        }
        
        return enrichedRecord;
    }
    
    /**
     * 根据数据内容获取分类
     * 
     * @param data 数据
     * @return 分类
     */
    private String getCategory(String data) {
        if (data == null) return "UNKNOWN";
        if (data.contains("Data_0")) return "PRIME";
        if (data.contains("Data_1")) return "SECONDARY";
        return "STANDARD";
    }
    
    /**
     * 前置处理：准备富化资源
     * 
     * @param input 输入数据
     * @throws Exception 准备过程中可能抛出的异常
     */
    @Override
    protected void before(List<String> input) throws Exception {
        System.out.println("Preparing enrichment resources...");
        if (enableCache) {
            System.out.println("Cache enabled, current size: " + cache.size());
        }
    }
    
    /**
     * 后置处理：记录富化统计信息
     * 
     * @param input 输入数据
     * @param output 输出数据
     * @throws Exception 记录过程中可能抛出的异常
     */
    @Override
    protected void after(List<String> input, List<Map<String, Object>> output) throws Exception {
        if (input != null && output != null) {
            System.out.println("Enrichment completed: " + input.size() + " -> " + output.size() + " records");
            if (enableCache) {
                System.out.println("Cache size after enrichment: " + cache.size());
            }
        }
    }
    
    /**
     * 获取当前缓存大小
     * 
     * @return 缓存大小
     */
    public int getCacheSize() {
        return enableCache ? cache.size() : 0;
    }
}