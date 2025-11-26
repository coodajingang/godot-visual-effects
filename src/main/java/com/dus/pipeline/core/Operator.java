package com.dus.pipeline.core;

/**
 * 算子接口，定义数据处理的基本操作
 */
public interface Operator<I, O> {
    
    /**
     * 处理输入数据并返回输出结果
     * @param input 输入数据
     * @return 输出结果
     */
    O process(I input);
    
    /**
     * 获取算子名称
     * @return 算子名称
     */
    String name();
}