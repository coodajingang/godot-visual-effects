package com.dus.pipeline.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 管道类，负责流程调度和算子链管理
 * 支持链式调用添加算子，并按顺序执行算子链
 * 
 * @param <I> 初始输入数据类型
 * @param <O> 最终输出数据类型
 * @author Dus
 * @version 1.0
 */
public class Pipeline<I, O> {
    
    private final SourceOperator<I> source;
    private final List<Operator<?, ?>> operators;
    
    /**
     * 构造函数，初始化管道
     * 
     * @param source 数据源算子
     * @throws IllegalArgumentException 如果source为null
     */
    public Pipeline(SourceOperator<I> source) {
        this.source = Objects.requireNonNull(source, "Source operator cannot be null");
        this.operators = new ArrayList<>();
    }
    
    /**
     * 添加算子到管道中，支持链式调用
     * 
     * @param <T> 算子的输出类型
     * @param operator 要添加的算子
     * @return 当前管道实例，支持链式调用
     * @throws IllegalArgumentException 如果operator为null
     */
    public <T> Pipeline<I, T> addOperator(Operator<?, T> operator) {
        Objects.requireNonNull(operator, "Operator cannot be null");
        this.operators.add(operator);
        @SuppressWarnings("unchecked")
        Pipeline<I, T> result = (Pipeline<I, T>) this;
        return result;
    }
    
    /**
     * 启动管道执行
     * 循环从数据源获取数据，并通过算子链进行处理
     * 
     * @throws Exception 管道执行过程中可能抛出的异常
     */
    public void run() throws Exception {
        System.out.println("Starting pipeline execution...");
        System.out.println("Source: " + source.name());
        System.out.println("Operators: " + operators.stream().map(Operator::name).reduce((a, b) -> a + " -> " + b).orElse("None"));
        System.out.println("---");
        
        int batchCount = 0;
        while (true) {
            try {
                // 从数据源获取下一批数据
                I batch = source.nextBatch();
                if (batch == null) {
                    System.out.println("No more data available. Pipeline execution completed.");
                    break;
                }
                
                batchCount++;
                System.out.println("Processing batch " + batchCount);
                
                // 依次执行算子链
                Object currentData = batch;
                for (Operator<?, ?> operator : operators) {
                    try {
                        @SuppressWarnings("unchecked")
                        Operator<Object, Object> typedOperator = (Operator<Object, Object>) operator;
                        currentData = typedOperator.process(currentData);
                        System.out.println("  -> " + operator.name() + " completed");
                    } catch (Exception e) {
                        System.err.println("Error in operator " + operator.name() + ": " + e.getMessage());
                        throw e;
                    }
                }
                
                System.out.println("Batch " + batchCount + " processed successfully");
                System.out.println("---");
                
            } catch (Exception e) {
                System.err.println("Pipeline execution failed on batch " + (batchCount + 1) + ": " + e.getMessage());
                throw e;
            }
        }
        
        System.out.println("Total batches processed: " + batchCount);
        System.out.println("Pipeline execution finished successfully.");
    }
    
    /**
     * 获取数据源算子
     * 
     * @return 数据源算子
     */
    public SourceOperator<I> getSource() {
        return source;
    }
    
    /**
     * 获取算子列表的副本
     * 
     * @return 算子列表副本
     */
    public List<Operator<?, ?>> getOperators() {
        return new ArrayList<>(operators);
    }
}