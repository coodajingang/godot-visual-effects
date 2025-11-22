package com.dus.pipeline.example.async;

import com.dus.pipeline.async.AsyncPipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 异步 Pipeline 示例
 * 展示使用异步算子、非阻塞执行、并发度控制等功能
 *
 * @author Dus
 * @version 1.0
 */
public class AsyncPipelineExample {

    public static void main(String[] args) throws Exception {
        System.out.println("========== Async Pipeline Example ==========\n");

        AsyncPipeline<List<String>, Void> pipeline = new AsyncPipeline<>(new AsyncDataSourceOperator());

        pipeline.addOperator(new AsyncTransformOperator())
                .addOperator(new AsyncEnrichOperator())
                .addOperator(new AsyncWriteToDbOperator());

        CompletableFuture<Void> future = pipeline.runAsync();

        System.out.println("Pipeline started, waiting for completion...\n");

        future.get();

        pipeline.printMetricsReport();

        pipeline.awaitTermination(5, TimeUnit.SECONDS);
        pipeline.shutdown();

        System.out.println("Pipeline execution completed!");
    }

    /**
     * 异步数据源算子示例
     */
    private static class AsyncDataSourceOperator extends com.dus.pipeline.async.AsyncSourceOperator<List<String>> {

        private int batchCount = 0;

        @Override
        public CompletableFuture<List<String>> nextBatchAsync() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    if (batchCount >= 3) {
                        return null;
                    }
                    Thread.sleep(5);
                    batchCount++;
                    List<String> batch = new ArrayList<>();
                    batch.add("item_" + (batchCount * 10 + 1));
                    batch.add("item_" + (batchCount * 10 + 2));
                    batch.add("item_" + (batchCount * 10 + 3));
                    System.out.println("[DataSource] Fetched batch " + batchCount);
                    return batch;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            });
        }

        @Override
        public String name() {
            return "AsyncDataSourceOperator";
        }
    }
}
