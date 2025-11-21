package com.dus.pipeline.splitter;

import java.util.ArrayList;
import java.util.List;

/**
 * 按固定大小拆分列表批次的拆分器实现
 * 将列表按指定大小拆分成多个子列表
 *
 * @param <T> 列表中元素的类型
 * @author Dus
 * @version 1.0
 */
public class FixedSizeBatchSplitter<T> implements BatchSplitter<List<T>> {

    private final int batchSize;

    /**
     * 构造函数
     *
     * @param batchSize 每个子批次的大小（必须大于0）
     * @throws IllegalArgumentException 如果batchSize <= 0
     */
    public FixedSizeBatchSplitter(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be greater than 0");
        }
        this.batchSize = batchSize;
    }

    @Override
    public boolean shouldSplit(List<T> batch) {
        return batch != null && batch.size() > batchSize;
    }

    @Override
    public List<List<T>> split(List<T> batch) {
        List<List<T>> result = new ArrayList<>();
        if (batch == null || batch.isEmpty()) {
            return result;
        }

        for (int i = 0; i < batch.size(); i += batchSize) {
            int end = Math.min(i + batchSize, batch.size());
            result.add(new ArrayList<>(batch.subList(i, end)));
        }

        return result;
    }

    @Override
    public String name() {
        return "FixedSizeBatchSplitter(size=" + batchSize + ")";
    }
}
