package com.dus.pipeline.splitter;

import java.util.ArrayList;
import java.util.List;

/**
 * 不拆分的批次拆分器实现
 * 始终返回原始批次，不进行任何拆分
 *
 * @param <T> 批次数据类型
 * @author Dus
 * @version 1.0
 */
public class NoBatchSplitter<T> implements BatchSplitter<T> {

    @Override
    public boolean shouldSplit(T batch) {
        return false;
    }

    @Override
    public List<T> split(T batch) {
        List<T> result = new ArrayList<>();
        result.add(batch);
        return result;
    }

    @Override
    public String name() {
        return "NoBatchSplitter";
    }
}
