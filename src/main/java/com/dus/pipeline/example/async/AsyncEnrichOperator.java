package com.dus.pipeline.example.async;

import com.dus.pipeline.async.AsyncOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 异步数据富化算子示例
 * 异步调用外部 API 进行数据富化
 *
 * @author Dus
 * @version 1.0
 */
public class AsyncEnrichOperator extends AsyncOperator<List<String>, List<Map<String, String>>> {

    @Override
    public CompletableFuture<List<Map<String, String>>> processAsync(List<String> input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(20);
                List<Map<String, String>> enriched = new ArrayList<>();
                for (String item : input) {
                    Map<String, String> data = new HashMap<>();
                    data.put("original", item);
                    data.put("enriched", item + "_enriched");
                    data.put("timestamp", String.valueOf(System.currentTimeMillis()));
                    enriched.add(data);
                }
                return enriched;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public String name() {
        return "AsyncEnrichOperator";
    }
}
