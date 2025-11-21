package com.dus.pipeline.core;

/**
 * 数据源算子抽象类，实现了模板方法模式
 * 定义了数据获取的标准流程：before -> doNextBatch -> after
 * 
 * @param <O> 输出数据类型
 * @author Dus
 * @version 1.0
 */
public abstract class SourceOperator<O> implements Operator<Void, O> {
    
    /**
     * 模板方法，定义数据获取的标准流程
     * 流程：before -> doNextBatch -> after
     * 
     * @param input 无用参数，保持接口一致性
     * @return 获取的数据批次
     * @throws Exception 数据获取过程中可能抛出的异常
     */
    @Override
    public final O process(Void input) throws Exception {
        before();
        O batch = doNextBatch();
        after(batch);
        return batch;
    }
    
    /**
     * 获取下一批数据，子类必须实现具体的数据源逻辑
     * 
     * @return 数据批次
     * @throws Exception 数据获取过程中可能抛出的异常
     */
    protected abstract O doNextBatch() throws Exception;
    
    /**
     * 前置处理方法，子类可选择性覆盖
     * 常用于连接检查、资源初始化、参数准备等
     * 
     * @throws Exception 前置处理过程中可能抛出的异常
     */
    protected void before() throws Exception {
        // 默认空实现，子类可覆盖
    }
    
    /**
     * 后置处理方法，子类可选择性覆盖
     * 常用于资源清理、状态更新、监控记录等
     * 
     * @param batch 获取的数据批次
     * @throws Exception 后置处理过程中可能抛出的异常
     */
    protected void after(O batch) throws Exception {
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
    
    /**
     * 便捷方法，直接获取下一批数据
     * 
     * @return 数据批次
     * @throws Exception 数据获取过程中可能抛出的异常
     */
    public final O nextBatch() throws Exception {
        return process(null);
    }
}