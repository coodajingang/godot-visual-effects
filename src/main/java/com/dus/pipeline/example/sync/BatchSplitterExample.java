package com.dus.pipeline.example.sync;

import com.dus.pipeline.core.Pipeline;
import com.dus.pipeline.example.MySourceOperator;
import com.dus.pipeline.example.TransformOperator;
import com.dus.pipeline.example.WriteToDbOperator;
import com.dus.pipeline.splitter.FixedSizeBatchSplitter;
import com.dus.pipeline.splitter.PredicateBatchSplitter;

import java.util.ArrayList;
import java.util.List;

/**
 * 批次拆分器示例
 * 演示如何使用不同的批次拆分策略
 *
 * @author Dus
 * @version 1.0
 */
public class BatchSplitterExample {

    public static void main(String[] args) throws Exception {
        System.out.println("========== Batch Splitter Example ==========\n");

        System.out.println("--- Example 1: FixedSizeBatchSplitter ---\n");
        demonstrateFixedSizeBatchSplitter();

        System.out.println("\n--- Example 2: PredicateBatchSplitter ---\n");
        demonstratePredicateBatchSplitter();
    }

    private static void demonstrateFixedSizeBatchSplitter() throws Exception {
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new MySourceOperator());

        pipeline.addOperator(new TransformOperator())
                .addOperator(new WriteToDbOperator());

        pipeline.setBatchSplitter(new FixedSizeBatchSplitter<>(2));

        System.out.println("Configured pipeline with FixedSizeBatchSplitter(size=2)");
        System.out.println("Original batch size is 3, will be split into sub-batches of 2\n");

        pipeline.run();
    }

    private static void demonstratePredicateBatchSplitter() throws Exception {
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new DataSourceWithCategories());

        pipeline.addOperator(new TransformOperator())
                .addOperator(new WriteToDbOperator());

        pipeline.setBatchSplitter(new PredicateBatchSplitter<>(item -> {
            if (item.startsWith("A_")) {
                return "category_A";
            } else if (item.startsWith("B_")) {
                return "category_B";
            } else {
                return "category_C";
            }
        }));

        System.out.println("Configured pipeline with PredicateBatchSplitter");
        System.out.println("Batch will be split by category prefix (A_, B_, C_)\n");

        pipeline.run();
    }

    /**
     * 返回带有类别前缀的数据源
     */
    private static class DataSourceWithCategories extends com.dus.pipeline.core.SourceOperator<List<String>> {

        private int batchCount = 0;

        @Override
        protected List<String> doNextBatch() throws Exception {
            if (batchCount >= 2) {
                return null;
            }
            batchCount++;

            List<String> batch = new ArrayList<>();
            batch.add("A_item_1");
            batch.add("A_item_2");
            batch.add("B_item_1");
            batch.add("B_item_2");
            batch.add("C_item_1");

            return batch;
        }

        @Override
        public String name() {
            return "DataSourceWithCategories";
        }
    }
}
