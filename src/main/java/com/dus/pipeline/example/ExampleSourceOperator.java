package com.dus.pipeline.example;

import com.dus.pipeline.core.SourceOperator;
import com.dus.pipeline.context.PipelineContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 示例源算子，生成测试数据
 */
public class ExampleSourceOperator extends SourceOperator<List<Data>> {
    
    private final List<List<Data>> batches;
    private int currentBatch = 0;
    private final int batchSize;
    
    public ExampleSourceOperator(int totalRecords, int batchSize) {
        this.batchSize = batchSize;
        this.batches = generateBatches(totalRecords, batchSize);
    }
    
    /**
     * 生成测试数据批次
     */
    private List<List<Data>> generateBatches(int totalRecords, int batchSize) {
        List<List<Data>> result = new ArrayList<>();
        
        for (int i = 0; i < totalRecords; i += batchSize) {
            List<Data> batch = new ArrayList<>();
            for (int j = 0; j < batchSize && (i + j) < totalRecords; j++) {
                int recordId = i + j + 1;
                Data data = new Data(
                    "data_" + recordId,
                    "Content for record " + recordId,
                    recordId % 2 == 0 ? "even" : "odd"
                );
                batch.add(data);
            }
            result.add(batch);
        }
        
        return result;
    }
    
    @Override
    public boolean hasNext() {
        return currentBatch < batches.size();
    }
    
    @Override
    protected List<Data> doNextBatch() {
        if (!hasNext()) {
            return null;
        }
        
        List<Data> batch = batches.get(currentBatch);
        
        // 记录到 Context
        if (getContext() != null) {
            setContextProperty("last_batch_size", batch.size());
            setContextProperty("current_batch_index", currentBatch);
            setContextProperty("total_batches", batches.size());
        }
        
        currentBatch++;
        return batch;
    }
    
    @Override
    protected void before() {
        if (getContext() != null) {
            setContextProperty("source_start_time", System.currentTimeMillis());
        }
    }
    
    @Override
    protected void after(List<Data> batch) {
        if (getContext() != null && batch != null) {
            setContextProperty("source_end_time", System.currentTimeMillis());
            setContextProperty("records_generated", 
                getContextProperty("records_generated", 0) + batch.size());
        }
    }
}