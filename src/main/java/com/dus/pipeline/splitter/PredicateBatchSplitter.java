package com.dus.pipeline.splitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 按条件拆分列表批次的拆分器实现
 * 根据条件函数将列表中的元素分组
 *
 * @param <T> 列表中元素的类型
 * @author Dus
 * @version 1.0
 */
public class PredicateBatchSplitter<T> implements BatchSplitter<List<T>> {

    private final Function<T, String> classifier;

    /**
     * 构造函数
     *
     * @param classifier 分类函数，根据元素返回分组键
     */
    public PredicateBatchSplitter(Function<T, String> classifier) {
        this.classifier = classifier;
    }

    @Override
    public boolean shouldSplit(List<T> batch) {
        if (batch == null || batch.size() <= 1) {
            return false;
        }
        String firstKey = classifier.apply(batch.get(0));
        for (int i = 1; i < batch.size(); i++) {
            if (!classifier.apply(batch.get(i)).equals(firstKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<List<T>> split(List<T> batch) {
        if (batch == null || batch.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<T>> grouped = new HashMap<>();
        for (T item : batch) {
            String key = classifier.apply(item);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }

        return new ArrayList<>(grouped.values());
    }

    @Override
    public String name() {
        return "PredicateBatchSplitter";
    }
}
