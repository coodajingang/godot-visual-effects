package com.dus.pipeline.util;

import com.dus.pipeline.core.SourceOperator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 模拟数据源
 * 用于测试的各种数据源实现
 * 
 * @author Dus
 * @version 1.0
 */
public class MockDataSource {
    
    /**
     * 创建简单的字符串数据源
     * 
     * @param maxBatches 最大批次数
     * @param batchSize 每批次大小
     * @return 数据源算子
     */
    public static SourceOperator<String> createStringSource(int maxBatches, int batchSize) {
        return new SourceOperator<String>() {
            private int currentBatch = 0;
            
            @Override
            protected String doNextBatch() throws Exception {
                if (currentBatch >= maxBatches) {
                    return null;
                }
                
                StringBuilder batch = new StringBuilder();
                for (int i = 0; i < batchSize; i++) {
                    if (i > 0) batch.append(",");
                    batch.append("mock_data_").append(currentBatch).append("_").append(i);
                }
                
                currentBatch++;
                return batch.toString();
            }
        };
    }
    
    /**
     * 创建Map数据源（模拟JSON数据）
     * 
     * @param maxBatches 最大批次数
     * @param batchSize 每批次大小
     * @return 数据源算子
     */
    public static SourceOperator<List<Map<String, Object>>> createMapSource(int maxBatches, int batchSize) {
        return new SourceOperator<List<Map<String, Object>>>() {
            private int currentBatch = 0;
            private int globalId = 1;
            
            @Override
            protected List<Map<String, Object>> doNextBatch() throws Exception {
                if (currentBatch >= maxBatches) {
                    return null;
                }
                
                List<Map<String, Object>> batch = new ArrayList<>();
                for (int i = 0; i < batchSize; i++) {
                    Map<String, Object> item = TestDataFactory.createMapList(1).get(0);
                    item.put("id", globalId++);
                    item.put("batchId", currentBatch);
                    item.put("batchIndex", i);
                    batch.add(item);
                }
                
                currentBatch++;
                return batch;
            }
        };
    }
    
    /**
     * 创建用户数据源
     * 
     * @param maxBatches 最大批次数
     * @param batchSize 每批次大小
     * @return 用户数据源算子
     */
    public static SourceOperator<List<Map<String, Object>>> createUserSource(int maxBatches, int batchSize) {
        return new SourceOperator<List<Map<String, Object>>>() {
            private int currentBatch = 0;
            private int userId = 1;
            
            @Override
            protected List<Map<String, Object>> doNextBatch() throws Exception {
                if (currentBatch >= maxBatches) {
                    return null;
                }
                
                List<Map<String, Object>> batch = new ArrayList<>();
                List<Map<String, Object>> users = TestDataFactory.createUserData(batchSize);
                
                for (Map<String, Object> user : users) {
                    user.put("id", userId++);
                    user.put("batchId", currentBatch);
                }
                
                batch.addAll(users);
                currentBatch++;
                return batch;
            }
        };
    }
    
    /**
     * 创建日志数据源
     * 
     * @param maxBatches 最大批次数
     * @param batchSize 每批次大小
     * @return 日志数据源算子
     */
    public static SourceOperator<List<Map<String, Object>>> createLogSource(int maxBatches, int batchSize) {
        return new SourceOperator<List<Map<String, Object>>>() {
            private int currentBatch = 0;
            private int logId = 1;
            
            @Override
            protected List<Map<String, Object>> doNextBatch() throws Exception {
                if (currentBatch >= maxBatches) {
                    return null;
                }
                
                List<Map<String, Object>> batch = new ArrayList<>();
                List<Map<String, Object>> logs = TestDataFactory.createLogData(batchSize);
                
                for (Map<String, Object> log : logs) {
                    log.put("id", logId++);
                    log.put("batchId", currentBatch);
                }
                
                batch.addAll(logs);
                currentBatch++;
                return batch;
            }
        };
    }
    
    /**
     * 创建订单数据源
     * 
     * @param maxBatches 最大批次数
     * @param batchSize 每批次大小
     * @return 订单数据源算子
     */
    public static SourceOperator<List<Map<String, Object>>> createOrderSource(int maxBatches, int batchSize) {
        return new SourceOperator<List<Map<String, Object>>>() {
            private int currentBatch = 0;
            
            @Override
            protected List<Map<String, Object>> doNextBatch() throws Exception {
                if (currentBatch >= maxBatches) {
                    return null;
                }
                
                List<Map<String, Object>> batch = TestDataFactory.createOrderData(batchSize);
                
                for (Map<String, Object> order : batch) {
                    order.put("batchId", currentBatch);
                }
                
                currentBatch++;
                return batch;
            }
        };
    }
    
    /**
     * 创建失败数据源（模拟错误）
     * 
     * @param failAfterBatches 在多少批次后失败
     * @param batchSize 每批次大小
     * @return 失败数据源算子
     */
    public static SourceOperator<String> createFailingSource(int failAfterBatches, int batchSize) {
        return new SourceOperator<String>() {
            private int currentBatch = 0;
            
            @Override
            protected String doNextBatch() throws Exception {
                if (currentBatch >= failAfterBatches) {
                    throw new RuntimeException("Simulated data source failure after " + failAfterBatches + " batches");
                }
                
                StringBuilder batch = new StringBuilder();
                for (int i = 0; i < batchSize; i++) {
                    if (i > 0) batch.append(",");
                    batch.append("data_before_failure_").append(currentBatch).append("_").append(i);
                }
                
                currentBatch++;
                return batch.toString();
            }
        };
    }
    
    /**
     * 创建慢速数据源（模拟网络延迟）
     * 
     * @param maxBatches 最大批次数
     * @param batchSize 每批次大小
     * @param delayMs 延迟时间（毫秒）
     * @return 慢速数据源算子
     */
    public static SourceOperator<String> createSlowSource(int maxBatches, int batchSize, long delayMs) {
        return new SourceOperator<String>() {
            private int currentBatch = 0;
            
            @Override
            protected String doNextBatch() throws Exception {
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
                
                if (currentBatch >= maxBatches) {
                    return null;
                }
                
                StringBuilder batch = new StringBuilder();
                for (int i = 0; i < batchSize; i++) {
                    if (i > 0) batch.append(",");
                    batch.append("slow_data_").append(currentBatch).append("_").append(i);
                }
                
                currentBatch++;
                return batch.toString();
            }
        };
    }
    
    /**
     * 创建随机大小数据源
     * 
     * @param maxBatches 最大批次数
     * @param minBatchSize 最小批次大小
     * @param maxBatchSize 最大批次大小
     * @return 随机大小数据源算子
     */
    public static SourceOperator<String> createRandomSizeSource(int maxBatches, int minBatchSize, int maxBatchSize) {
        return new SourceOperator<String>() {
            private int currentBatch = 0;
            
            @Override
            protected String doNextBatch() throws Exception {
                if (currentBatch >= maxBatches) {
                    return null;
                }
                
                int batchSize = ThreadLocalRandom.current().nextInt(minBatchSize, maxBatchSize + 1);
                StringBuilder batch = new StringBuilder();
                for (int i = 0; i < batchSize; i++) {
                    if (i > 0) batch.append(",");
                    batch.append("random_size_data_").append(currentBatch).append("_").append(i);
                }
                
                currentBatch++;
                return batch.toString();
            }
        };
    }
    
    /**
     * 创建空数据源（返回空批次）
     * 
     * @param maxBatches 最大批次数
     * @return 空数据源算子
     */
    public static SourceOperator<String> createEmptySource(int maxBatches) {
        return new SourceOperator<String>() {
            private int currentBatch = 0;
            
            @Override
            protected String doNextBatch() throws Exception {
                if (currentBatch >= maxBatches) {
                    return null;
                }
                currentBatch++;
                return ""; // 空批次
            }
        };
    }
}