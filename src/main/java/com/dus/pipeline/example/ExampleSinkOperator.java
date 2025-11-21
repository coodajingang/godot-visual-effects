package com.dus.pipeline.example;

import com.dus.pipeline.core.SinkOperator;

import java.util.List;

/**
 * 示例输出算子
 */
public class ExampleSinkOperator extends SinkOperator<List<Data>> {
    
    private long totalRecordsWritten = 0;
    
    @Override
    protected void write(List<Data> input) {
        if (input == null || input.isEmpty()) {
            return;
        }
        
        // 模拟写入操作
        totalRecordsWritten += input.size();
        
        // 记录到 Context
        setContextProperty("total_records_written", totalRecordsWritten);
        setContextProperty("last_write_time", System.currentTimeMillis());
        setContextProperty("sink_batch_count", 
            getContextProperty("sink_batch_count", 0) + 1);
        
        // 输出到控制台（实际应用中可能是数据库、文件等）
        System.out.println("=== Writing " + input.size() + " records to sink ===");
        for (Data data : input) {
            System.out.println("  " + data);
        }
        System.out.println("=== Total written: " + totalRecordsWritten + " ===");
    }
    
    /**
     * 获取总写入记录数
     */
    public long getTotalRecordsWritten() {
        return totalRecordsWritten;
    }
    
    @Override
    protected void before(List<Data> input) {
        setContextProperty("sink_start_time", System.currentTimeMillis());
    }
    
    @Override
    protected void after(List<Data> input, Void output) {
        setContextProperty("sink_end_time", System.currentTimeMillis());
        if (getContext() != null) {
            long duration = System.currentTimeMillis() - 
                getContextProperty("sink_start_time", System.currentTimeMillis());
            setContextProperty("sink_duration_ms", duration);
        }
    }
}