package com.dus.pipeline.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 条件批次拆分器
 * 根据条件将批次拆分为满足条件和不满足条件两部分
 * 
 * @param <T> 数据类型
 * @author Dus
 * @version 1.0
 */
public class PredicateBatchSplitter<T> implements BatchSplitter<T> {
    
    private final Predicate<T> predicate;
    
    /**
     * 构造函数
     * 
     * @param predicate 判断条件
     * @throws IllegalArgumentException 如果predicate为null
     */
    public PredicateBatchSplitter(Predicate<T> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("Predicate cannot be null");
        }
        this.predicate = predicate;
    }
    
    @Override
    public List<List<T>> split(List<T> batch) {
        List<List<T>> result = new ArrayList<>();
        
        if (batch == null || batch.isEmpty()) {
            return result;
        }
        
        List<T> matched = new ArrayList<>();
        List<T> unmatched = new ArrayList<>();
        
        for (T item : batch) {
            if (predicate.test(item)) {
                matched.add(item);
            } else {
                unmatched.add(item);
            }
        }
        
        // 只有非空的列表才会被添加到结果中
        if (!matched.isEmpty()) {
            result.add(matched);
        }
        if (!unmatched.isEmpty()) {
            result.add(unmatched);
        }
        
        return result;
    }
    
    /**
     * 获取判断条件
     * 
     * @return 判断条件
     */
    public Predicate<T> getPredicate() {
        return predicate;
    }
}