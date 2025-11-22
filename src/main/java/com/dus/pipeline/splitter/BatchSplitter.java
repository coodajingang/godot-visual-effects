package com.dus.pipeline.splitter;

import java.util.List;

/**
 * 批次拆分器接口（策略模式）
 * 定义了判断是否需要拆分、如何拆分批次的操作
 *
 * @param <T> 批次数据类型
 * @author Dus
 * @version 1.0
 */
public interface BatchSplitter<T> {

    /**
     * 判断是否需要拆分当前批次
     *
     * @param batch 批次数据
     * @return true 表示需要拆分，false 表示不需要拆分
     */
    boolean shouldSplit(T batch);

    /**
     * 拆分批次，返回多个子批次
     *
     * @param batch 批次数据
     * @return 拆分后的子批次列表
     */
    List<T> split(T batch);

    /**
     * 获取拆分器名称
     *
     * @return 拆分器名称
     */
    String name();
}
