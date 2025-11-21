package com.dus.pipeline.example;

import com.dus.pipeline.core.SinkOperator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 示例写库算子
 * 将处理后的数据写入数据库或其他存储系统
 * 
 * @author Dus
 * @version 1.0
 */
public class WriteToDbOperator extends SinkOperator<List<Map<String, Object>>> {
    
    private final String tableName;
    private final boolean simulateFailure;
    private final AtomicLong totalWritten;
    private final AtomicLong batchCount;
    
    /**
     * 构造函数
     * 
     * @param tableName 目标表名
     * @param simulateFailure 是否模拟写入失败
     */
    public WriteToDbOperator(String tableName, boolean simulateFailure) {
        this.tableName = tableName;
        this.simulateFailure = simulateFailure;
        this.totalWritten = new AtomicLong(0);
        this.batchCount = new AtomicLong(0);
    }
    
    /**
     * 实现具体的写入逻辑
     * 
     * @param input 需要写入的数据
     * @throws Exception 写入过程中可能抛出的异常
     */
    @Override
    protected void write(List<Map<String, Object>> input) throws Exception {
        if (input == null || input.isEmpty()) {
            System.out.println("No data to write to table " + tableName);
            return;
        }
        
        batchCount.incrementAndGet();
        
        // 模拟写入延迟
        Thread.sleep(50);
        
        // 模拟写入失败
        if (simulateFailure && Math.random() < 0.1) {
            throw new Exception("Failed to write to table " + tableName + ": Connection timeout");
        }
        
        // 模拟批量写入
        int writtenCount = 0;
        for (Map<String, Object> record : input) {
            // 模拟单条记录写入
            if (record != null && !record.isEmpty()) {
                writtenCount++;
                totalWritten.incrementAndGet();
            }
        }
        
        System.out.println("Successfully wrote " + writtenCount + " records to table " + tableName);
    }
    
    /**
     * 前置处理：检查写入连接和权限
     * 
     * @param input 输入数据
     * @throws Exception 检查过程中可能抛出的异常
     */
    @Override
    protected void before(List<Map<String, Object>> input) throws Exception {
        System.out.println("Checking write permissions for table " + tableName + "...");
        // 模拟权限检查
        Thread.sleep(10);
        System.out.println("Write permissions OK");
        
        if (input != null) {
            System.out.println("Preparing to write " + input.size() + " records to table " + tableName);
        }
    }
    
    /**
     * 后置处理：记录写入统计信息
     * 
     * @param input 输入数据
     * @param output 输出数据（对于SinkOperator总是null）
     * @throws Exception 记录过程中可能抛出的异常
     */
    @Override
    protected void after(List<Map<String, Object>> input, Void output) throws Exception {
        System.out.println("Write operation completed for batch #" + batchCount.get());
        System.out.println("Total records written to " + tableName + ": " + totalWritten.get());
        
        // 模拟事务提交
        System.out.println("Transaction committed successfully");
    }
    
    /**
     * 获取总写入记录数
     * 
     * @return 总写入记录数
     */
    public long getTotalWritten() {
        return totalWritten.get();
    }
    
    /**
     * 获取批次数
     * 
     * @return 批次数
     */
    public long getBatchCount() {
        return batchCount.get();
    }
    
    /**
     * 重置统计信息
     */
    public void resetStats() {
        totalWritten.set(0);
        batchCount.set(0);
    }
}