package com.dus.pipeline.core;

/**
 * 基础算子接口，定义了数据处理的契约
 * 
 * @param <I> 输入数据类型
 * @param <O> 输出数据类型
 * @author Dus
 * @version 1.0
 */
public interface Operator<I, O> {
    
    /**
     * 处理输入数据并返回输出结果
     * 
     * @param input 输入数据
     * @return 处理后的输出数据
     * @throws Exception 处理过程中可能抛出的异常
     */
    O process(I input) throws Exception;
    
    /**
     * 获取算子名称，用于日志和监控
     * 
     * @return 算子名称
     */
    String name();
}