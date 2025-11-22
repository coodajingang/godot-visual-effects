package com.dus.pipeline.example;

import com.dus.pipeline.core.SourceOperator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 示例数据源算子
 * 模拟从数据库或消息队列获取数据批次
 * 
 * @author Dus
 * @version 1.0
 */
public class MySourceOperator extends SourceOperator<List<String>> {
    
    private final int maxBatches;
    private final int batchSize;
    private int currentBatch;
    private final Random random;
    
    /**
     * 构造函数
     * 
     * @param maxBatches 最大批次数
     * @param batchSize 每批次数据量
     */
    public MySourceOperator(int maxBatches, int batchSize) {
        this.maxBatches = maxBatches;
        this.batchSize = batchSize;
        this.currentBatch = 0;
        this.random = new Random();
    }
    
    /**
     * 实现具体的数据获取逻辑
     * 
     * @return 数据批次
     * @throws Exception 数据获取过程中可能抛出的异常
     */
    @Override
    protected List<String> doNextBatch() throws Exception {
        if (currentBatch >= maxBatches) {
            return null; // 没有更多数据
        }
        
        // 模拟数据获取延迟
        Thread.sleep(100);
        
        List<String> batch = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            batch.add("Data_" + currentBatch + "_" + i + "_" + random.nextInt(1000));
        }
        
        currentBatch++;
        return batch;
    }
    
    /**
     * 前置处理：检查连接状态
     * 
     * @throws Exception 检查过程中可能抛出的异常
     */
    @Override
    protected void before() throws Exception {
        System.out.println("Checking database connection...");
        // 模拟连接检查
        if (random.nextDouble() < 0.1) {
            throw new Exception("Database connection failed");
        }
        System.out.println("Database connection OK");
    }
    
    /**
     * 后置处理：记录获取统计信息
     * 
     * @param batch 获取的数据批次
     * @throws Exception 记录过程中可能抛出的异常
     */
    @Override
    protected void after(List<String> batch) throws Exception {
        if (batch != null) {
            System.out.println("Retrieved " + batch.size() + " records from database");
        }
    }
}