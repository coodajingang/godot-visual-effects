package com.dus.pipeline.core;

/**
 * 抽象算子类，实现了模板方法模式
 * 定义了算子的标准执行流程：before -> doProcess -> after
 * 
 * @param <I> 输入数据类型
 * @param <O> 输出数据类型
 * @author Dus
 * @version 1.0
 */
public abstract class AbstractOperator<I, O> implements Operator<I, O> {
    
    /**
     * 模板方法，定义算子的标准执行流程
     * 流程：before -> doProcess -> after
     * 
     * @param input 输入数据
     * @return 处理后的输出数据
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public final O process(I input) throws Exception {
        before(input);
        O output = doProcess(input);
        after(input, output);
        return output;
    }
    
    /**
     * 子类必须实现的核心业务逻辑方法
     * 
     * @param input 输入数据
     * @return 处理后的输出数据
     * @throws Exception 业务处理过程中可能抛出的异常
     */
    protected abstract O doProcess(I input) throws Exception;
    
    /**
     * 前置处理方法，子类可选择性覆盖
     * 常用于参数校验、日志记录、监控埋点等
     * 
     * @param input 输入数据
     * @throws Exception 前置处理过程中可能抛出的异常
     */
    protected void before(I input) throws Exception {
        // 默认空实现，子类可覆盖
    }
    
    /**
     * 后置处理方法，子类可选择性覆盖
     * 常用于结果记录、指标统计、清理工作等
     * 
     * @param input 输入数据
     * @param output 输出数据
     * @throws Exception 后置处理过程中可能抛出的异常
     */
    protected void after(I input, O output) throws Exception {
        // 默认空实现，子类可覆盖
    }
    
    /**
     * 获取算子名称，默认返回类名
     * 
     * @return 算子名称
     */
    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }
}