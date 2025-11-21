package com.dus.pipeline.example.async;

import com.dus.pipeline.async.AsyncSinkOperator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 异步写入数据库算子示例
 * 异步批量写库（模拟异步数据库操作）
 *
 * @author Dus
 * @version 1.0
 */
public class AsyncWriteToDbOperator extends AsyncSinkOperator<List<Map<String, String>>> {

    @Override
    protected CompletableFuture<Void> writeAsync(List<Map<String, String>> input) {
        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println("[AsyncWriteToDbOperator] Writing " + input.size() + " records to database...");
                Thread.sleep(30);
                for (Map<String, String> record : input) {
                    System.out.println("  DB Insert: " + record);
                }
                System.out.println("[AsyncWriteToDbOperator] Write completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Override
    public String name() {
        return "AsyncWriteToDbOperator";
    }
}
