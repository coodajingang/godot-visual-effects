package com.dus.pipeline.core;

/**
 * 数据处理操作符接口
 */
public interface Operator<I, O> {
    
    /**
     * 处理数据
     * @param input 输入数据
     * @return 处理后的输出数据
     * @throws Exception 如果处理失败
     */
    O process(I input) throws Exception;
    
    /**
     * 操作符名称
     * @return 操作符名称
     */
    String name();
}