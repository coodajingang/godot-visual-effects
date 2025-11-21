package com.dus.pipeline.splitter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * BatchSplitter 集成测试
 * 验证不同批次拆分器的综合功能
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("BatchSplitter 集成测试")
class BatchSplitterTest {
    
    @Test
    @DisplayName("FixedSizeBatchSplitter 基础功能验证")
    void testFixedSizeBatchSplitterBasicFunctionality() {
        // Given
        BatchSplitter<String> splitter = new FixedSizeBatchSplitter<>(3);
        List<String> data = Arrays.asList("a", "b", "c", "d", "e", "f", "g");
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).containsExactly("a", "b", "c");
        assertThat(result.get(1)).containsExactly("d", "e", "f");
        assertThat(result.get(2)).containsExactly("g");
    }
    
    @Test
    @DisplayName("PredicateBatchSplitter 基础功能验证")
    void testPredicateBatchSplitterBasicFunctionality() {
        // Given
        BatchSplitter<String> splitter = new PredicateBatchSplitter<>(s -> s.startsWith("A"));
        List<String> data = Arrays.asList("Apple", "Banana", "Apricot", "Cherry", "Avocado");
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsExactly("Apple", "Apricot", "Avocado");
        assertThat(result.get(1)).containsExactly("Banana", "Cherry");
    }
    
    @Test
    @DisplayName("组合使用两种拆分器")
    void testCombinedSplitterUsage() {
        // Given - 先按固定大小拆分，再按条件拆分
        FixedSizeBatchSplitter<Integer> fixedSplitter = new FixedSizeBatchSplitter<>(4);
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // When - 第一步：固定大小拆分
        List<List<Integer>> fixedBatches = fixedSplitter.split(data);
        
        // Then
        assertThat(fixedBatches).hasSize(3);
        assertThat(fixedBatches.get(0)).containsExactly(1, 2, 3, 4);
        assertThat(fixedBatches.get(1)).containsExactly(5, 6, 7, 8);
        assertThat(fixedBatches.get(2)).containsExactly(9, 10);
        
        // When - 第二步：对每个批次进行条件拆分
        PredicateBatchSplitter<Integer> evenSplitter = new PredicateBatchSplitter<>(i -> i % 2 == 0);
        
        // Then
        List<List<Integer>> evenNumbers = evenSplitter.split(fixedBatches.get(0));
        assertThat(evenNumbers.get(0)).containsExactly(2, 4); // 偶数
        assertThat(evenNumbers.get(1)).containsExactly(1, 3); // 奇数
    }
    
    @Test
    @DisplayName("边界情况：空数据和null数据")
    void testBoundaryCasesEmptyAndNullData() {
        // Given
        FixedSizeBatchSplitter<String> fixedSplitter = new FixedSizeBatchSplitter<>(5);
        PredicateBatchSplitter<String> predicateSplitter = new PredicateBatchSplitter<>(s -> true);
        
        // When & Then - 空列表
        List<List<String>> emptyFixed = fixedSplitter.split(Arrays.asList());
        List<List<String>> emptyPredicate = predicateSplitter.split(Arrays.asList());
        assertThat(emptyFixed).isEmpty();
        assertThat(emptyPredicate).isEmpty();
        
        // When & Then - null输入
        List<List<String>> nullFixed = fixedSplitter.split(null);
        List<List<String>> nullPredicate = predicateSplitter.split(null);
        assertThat(nullFixed).isEmpty();
        assertThat(nullPredicate).isEmpty();
    }
    
    @Test
    @DisplayName("性能测试：大数据集拆分")
    void testPerformanceLargeDataset() {
        // Given
        int dataSize = 100_000;
        int batchSize = 1000;
        FixedSizeBatchSplitter<Integer> splitter = new FixedSizeBatchSplitter<>(batchSize);
        
        List<Integer> largeData = java.util.stream.IntStream.range(0, dataSize)
            .boxed()
            .toList();
        
        // When
        long startTime = System.currentTimeMillis();
        List<List<Integer>> result = splitter.split(largeData);
        long endTime = System.currentTimeMillis();
        
        // Then
        assertThat(result).hasSize(100); // 100_000 / 1000 = 100
        assertThat(result.stream().mapToInt(List::size).sum()).isEqualTo(dataSize);
        
        // 性能断言：处理10万条数据应该在合理时间内完成（比如1秒内）
        assertThat(endTime - startTime).isLessThan(1000);
    }
    
    @Test
    @DisplayName("类型安全性验证")
    void testTypeSafety() {
        // Given - 不同类型的拆分器
        FixedSizeBatchSplitter<String> stringSplitter = new FixedSizeBatchSplitter<>(2);
        FixedSizeBatchSplitter<Integer> intSplitter = new FixedSizeBatchSplitter<>(3);
        PredicateBatchSplitter<Double> doubleSplitter = new PredicateBatchSplitter<>(d -> d > 0.5);
        
        // When & Then - 字符串拆分
        List<String> strings = Arrays.asList("x", "y", "z", "w");
        List<List<String>> stringResult = stringSplitter.split(strings);
        assertThat(stringResult.get(0)).isInstanceOf(List.class);
        assertThat(stringResult.get(0).get(0)).isInstanceOf(String.class);
        
        // When & Then - 整数拆分
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        List<List<Integer>> intResult = intSplitter.split(integers);
        assertThat(intResult.get(0)).isInstanceOf(List.class);
        assertThat(intResult.get(0).get(0)).isInstanceOf(Integer.class);
        
        // When & Then - 双精度浮点数拆分
        List<Double> doubles = Arrays.asList(0.1, 0.6, 0.3, 0.8);
        List<List<Double>> doubleResult = doubleSplitter.split(doubles);
        assertThat(doubleResult.get(0)).isInstanceOf(List.class);
        assertThat(doubleResult.get(0).get(0)).isInstanceOf(Double.class);
    }
    
    @Test
    @DisplayName("数据完整性验证")
    void testDataIntegrityVerification() {
        // Given
        List<String> originalData = Arrays.asList(
            "item1", "item2", "item3", "item4", "item5", "item6", "item7"
        );
        
        // When - 使用固定大小拆分
        FixedSizeBatchSplitter<String> fixedSplitter = new FixedSizeBatchSplitter<>(3);
        List<List<String>> fixedResult = fixedSplitter.split(originalData);
        
        // Then - 验证数据完整性
        List<String> reconstructedFixed = fixedResult.stream()
            .flatMap(List::stream)
            .toList();
        assertThat(reconstructedFixed).isEqualTo(originalData);
        
        // When - 使用条件拆分
        PredicateBatchSplitter<String> predicateSplitter = new PredicateBatchSplitter<>(s -> s.contains("2") || s.contains("5"));
        List<List<String>> predicateResult = predicateSplitter.split(originalData);
        
        // Then - 验证数据完整性
        List<String> reconstructedPredicate = predicateResult.stream()
            .flatMap(List::stream)
            .toList();
        assertThat(reconstructedPredicate).isEqualTo(originalData);
    }
    
    @Test
    @DisplayName("复杂条件组合拆分")
    void testComplexConditionSplitting() {
        // Given - 复杂数据结构
        List<String> logEntries = Arrays.asList(
            "INFO: Application started",
            "ERROR: Database connection failed",
            "WARN: Low memory",
            "INFO: Processing data",
            "ERROR: File not found",
            "DEBUG: Variable value",
            "ERROR: Network timeout",
            "INFO: Application stopped"
        );
        
        // When - 按日志级别拆分
        PredicateBatchSplitter<String> errorSplitter = new PredicateBatchSplitter<>(s -> s.startsWith("ERROR"));
        List<List<String>> errorResult = errorSplitter.split(logEntries);
        
        // Then
        assertThat(errorResult).hasSize(2);
        List<String> errors = errorResult.get(0);
        List<String> nonErrors = errorResult.get(1);
        
        assertThat(errors).containsExactly(
            "ERROR: Database connection failed",
            "ERROR: File not found",
            "ERROR: Network timeout"
        );
        
        assertThat(nonErrors).containsExactly(
            "INFO: Application started",
            "WARN: Low memory",
            "INFO: Processing data",
            "DEBUG: Variable value",
            "INFO: Application stopped"
        );
        
        // When - 对非错误日志进一步按固定大小拆分
        FixedSizeBatchSplitter<String> fixedSplitter = new FixedSizeBatchSplitter<>(2);
        List<List<String>> nonErrorBatches = fixedSplitter.split(nonErrors);
        
        // Then
        assertThat(nonErrorBatches).hasSize(3);
        assertThat(nonErrorBatches.get(0)).containsExactly("INFO: Application started", "WARN: Low memory");
        assertThat(nonErrorBatches.get(1)).containsExactly("INFO: Processing data", "DEBUG: Variable value");
        assertThat(nonErrorBatches.get(2)).containsExactly("INFO: Application stopped");
    }
}