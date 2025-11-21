package com.dus.pipeline.example;

import com.dus.pipeline.core.AbstractOperator;
import com.dus.pipeline.retry.RetryableOperator;
import com.dus.pipeline.retry.RetryStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * 示例验证算子
 */
public class ValidateOperator extends RetryableOperator<List<Data>, List<Data>> {
    
    public ValidateOperator() {
        // 使用指数退避重试策略
        withRetryStrategy(new com.dus.pipeline.retry.ExponentialBackoffRetryStrategy(
            3, 500, 5000, 2.0
        ).addRetryableException(IllegalArgumentException.class));
    }
    
    @Override
    protected List<Data> doProcess(List<Data> input) {
        List<Data> validData = new ArrayList<>();
        
        for (Data data : input) {
            if (isValid(data)) {
                validData.add(data);
            } else {
                throw new IllegalArgumentException("Invalid data: " + data);
            }
        }
        
        // 更新 Context 统计信息
        setContextProperty("validated_count", 
            getContextProperty("validated_count", 0) + validData.size());
        setContextProperty("validation_passed", true);
        
        return validData;
    }
    
    /**
     * 验证数据是否有效
     */
    private boolean isValid(Data data) {
        return data != null && 
               data.getId() != null && !data.getId().isEmpty() &&
               data.getContent() != null && !data.getContent().isEmpty();
    }
    
    @Override
    protected List<Data> getDefaultValue() {
        // 验证失败时返回空列表
        return new ArrayList<>();
    }
    
    @Override
    protected void before(List<Data> input) {
        setContextProperty("validation_start_time", System.currentTimeMillis());
    }
    
    @Override
    protected void after(List<Data> input, List<Data> output) {
        setContextProperty("validation_end_time", System.currentTimeMillis());
        setContextProperty("validation_input_count", input.size());
        setContextProperty("validation_output_count", output.size());
    }
}