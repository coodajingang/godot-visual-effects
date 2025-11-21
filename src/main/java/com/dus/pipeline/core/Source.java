package com.dus.pipeline.core;

/**
 * 数据源接口
 */
public interface Source<I> {
    
    /**
     * 获取下一批数据
     * @return 下一批数据，如果没有更多数据则返回 null
     * @throws Exception 如果获取数据失败
     */
    I nextBatch() throws Exception;
    
    /**
     * 数据源名称
     * @return 数据源名称
     */
    String name();
}