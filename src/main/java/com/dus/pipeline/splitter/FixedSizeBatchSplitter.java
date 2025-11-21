package com.dus.pipeline.splitter;

import java.util.ArrayList;
import java.util.List;

/**
 * 固定大小批次拆分器
 * 将批次按照指定大小进行拆分
 * 
 * @param <T> 数据类型
 * @author Dus
 * @version 1.0
 */
public class FixedSizeBatchSplitter<T> implements BatchSplitter<T> {
    
    private final int batchSize;
    
    /**
     * 构造函数
     * 
     * @param batchSize 每个批次的大小
     * @throws IllegalArgumentException 如果batchSize小于等于0
     */
    public FixedSizeBatchSplitter(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        this.batchSize = batchSize;
    }
    
    @Override
    public List<List<T>> split(List<T> batch) {
        List<List<T>> result = new ArrayList<>();
        
        if (batch == null || batch.isEmpty()) {
            return result;
        }
        
        for (int i = 0; i < batch.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, batch.size());
            result.add(new ArrayList<>(batch.subList(i, endIndex)));
        }
        
        return result;
    }
    
    /**
     * 获取批次大小
     * 
     * @return 批次大小
     */
    public int getBatchSize() {
        return batchSize;
    }
}