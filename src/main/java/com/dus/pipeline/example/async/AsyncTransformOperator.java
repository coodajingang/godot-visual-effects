package com.dus.pipeline.example.async;

import com.dus.pipeline.async.AsyncOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 异步转换算子示例
 * 将列表中的每个元素进行异步转换（例如转换为大写）
 *
 * @author Dus
 * @version 1.0
 */
public class AsyncTransformOperator extends AsyncOperator<List<String>, List<String>> {

    @Override
    public CompletableFuture<List<String>> processAsync(List<String> input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10);
                return input.stream()
                        .map(String::toUpperCase)
                        .collect(Collectors.toList());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public String name() {
        return "AsyncTransformOperator";
    }
}
