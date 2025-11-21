package com.dus.pipeline.example;

import com.dus.pipeline.core.AbstractOperator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 示例转换算子
 * 对输入数据进行转换处理，如数据清洗、格式转换等
 * 
 * @author Dus
 * @version 1.0
 */
public class TransformOperator extends AbstractOperator<List<String>, List<String>> {
    
    private final String prefix;
    private final boolean toUpperCase;
    private long processedCount;
    
    /**
     * 构造函数
     * 
     * @param prefix 数据前缀
     * @param toUpperCase 是否转换为大写
     */
    public TransformOperator(String prefix, boolean toUpperCase) {
        this.prefix = prefix;
        this.toUpperCase = toUpperCase;
        this.processedCount = 0;
    }
    
    /**
     * 实现具体的转换逻辑
     * 
     * @param input 输入数据列表
     * @return 转换后的数据列表
     * @throws Exception 转换过程中可能抛出的异常
     */
    @Override
    protected List<String> doProcess(List<String> input) throws Exception {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // 使用Java8 Stream API进行数据转换
        List<String> transformed = input.stream()
                .filter(data -> data != null && !data.trim().isEmpty())
                .map(data -> {
                    String result = prefix + data;
                    return toUpperCase ? result.toUpperCase() : result;
                })
                .collect(Collectors.toList());
        
        processedCount += transformed.size();
        return transformed;
    }
    
    /**
     * 前置处理：验证输入数据
     * 
     * @param input 输入数据
     * @throws Exception 验证过程中可能抛出的异常
     */
    @Override
    protected void before(List<String> input) throws Exception {
        if (input != null) {
            System.out.println("Starting transformation for " + input.size() + " records");
        }
    }
    
    /**
     * 后置处理：记录转换统计信息
     * 
     * @param input 输入数据
     * @param output 输出数据
     * @throws Exception 记录过程中可能抛出的异常
     */
    @Override
    protected void after(List<String> input, List<String> output) throws Exception {
        if (input != null && output != null) {
            System.out.println("Transformation completed: " + input.size() + " -> " + output.size() + " records");
            System.out.println("Total processed so far: " + processedCount + " records");
        }
    }
    
    /**
     * 获取已处理的记录总数
     * 
     * @return 已处理记录数
     */
    public long getProcessedCount() {
        return processedCount;
    }
}