package com.dus.pipeline.example;

import com.dus.pipeline.core.AbstractOperator;
import com.dus.pipeline.retry.RetryableOperator;
import com.dus.pipeline.retry.RetryStrategy;
import com.dus.pipeline.retry.SkipStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 示例富化算子，演示 Context 使用和重试机制
 */
public class EnrichOperator extends RetryableOperator<List<Data>, List<Data>> {
    
    private final Random random = new Random();
    private double failureRate = 0.1; // 10% 失败率
    
    public EnrichOperator() {
        // 默认使用固定延迟重试策略
        withRetryStrategy(new com.dus.pipeline.retry.FixedDelayRetryStrategy(3, 1000)
            .addRetryableException(RuntimeException.class));
    }
    
    public EnrichOperator withFailureRate(double failureRate) {
        this.failureRate = failureRate;
        return this;
    }
    
    @Override
    protected List<Data> doProcess(List<Data> input) {
        // 模拟随机失败
        if (random.nextDouble() < failureRate) {
            throw new RuntimeException("Simulated enrichment failure");
        }
        
        // 从 Context 获取配置
        String apiUrl = getContextProperty("enrichment_api_url", "http://default.api.com");
        int timeout = getContextProperty("timeout", 5000);
        String enrichPrefix = getContextProperty("enrich_prefix", "enriched_");
        
        List<Data> enriched = new ArrayList<>();
        
        for (Data data : input) {
            Data enrichedData = new Data(
                enrichPrefix + data.getId(),
                data.getContent() + " [enriched via " + apiUrl + "]",
                data.getType()
            );
            enrichedData.setTimestamp(data.getTimestamp());
            enriched.add(enrichedData);
        }
        
        // 更新 Context 统计信息
        long startTime = getContextProperty("enrich_start_time", System.currentTimeMillis());
        setContextProperty("last_enrich_time", System.currentTimeMillis());
        setContextProperty("enrich_count", 
            getContextProperty("enrich_count", 0) + input.size());
        setContextProperty("enrich_duration_ms", System.currentTimeMillis() - startTime);
        
        return enriched;
    }
    
    @Override
    protected List<Data> getDefaultValue() {
        // 跳过时返回空列表
        return new ArrayList<>();
    }
    
    @Override
    protected void before(List<Data> input) {
        setContextProperty("enrich_start_time", System.currentTimeMillis());
        if (getContext() != null) {
            getContext().setProperty("processing_batch", input.size());
        }
    }
    
    @Override
    protected void after(List<Data> input, List<Data> output) {
        if (getContext() != null) {
            getContext().setProperty("last_batch_processed", input.size());
        }
    }
}