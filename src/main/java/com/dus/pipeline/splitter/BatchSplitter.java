package com.dus.pipeline.splitter;

import java.util.List;

/**
 * 批次拆分器接口
 * 用于将大批次数据拆分为小批次进行处理
 * 
 * @param <T> 数据类型
 * @author Dus
 * @version 1.0
 */
public interface BatchSplitter<T> {
    
    /**
     * 将输入批次拆分为多个小批次
     * 
     * @param batch 输入批次
     * @return 拆分后的批次列表
     */
    List<List<T>> split(List<T> batch);
}