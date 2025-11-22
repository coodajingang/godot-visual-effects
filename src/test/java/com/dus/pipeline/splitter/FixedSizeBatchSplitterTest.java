package com.dus.pipeline.splitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * FixedSizeBatchSplitter 单元测试
 * 验证固定大小批次拆分器的功能
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("FixedSizeBatchSplitter 测试")
class FixedSizeBatchSplitterTest {
    
    private FixedSizeBatchSplitter<String> splitter;
    
    @Test
    @DisplayName("正常拆分：100 条数据按 30 条拆分")
    void testNormalSplitting() {
        // Given
        splitter = new FixedSizeBatchSplitter<>(30);
        List<String> data = createTestData(100);
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(4); // 100 / 30 = 3余10，所以是4批
        assertThat(result.get(0)).hasSize(30);
        assertThat(result.get(1)).hasSize(30);
        assertThat(result.get(2)).hasSize(30);
        assertThat(result.get(3)).hasSize(10);
        
        // 验证数据完整性
        List<String> flattened = result.stream()
            .flatMap(List::stream)
            .toList();
        assertThat(flattened).isEqualTo(data);
    }
    
    @Test
    @DisplayName("不足一个完整批次的情况")
    void testPartialBatch() {
        // Given
        splitter = new FixedSizeBatchSplitter<>(50);
        List<String> data = createTestData(25);
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).hasSize(25);
        assertThat(result.get(0)).isEqualTo(data);
    }
    
    @Test
    @DisplayName("空列表处理")
    void testEmptyList() {
        // Given
        splitter = new FixedSizeBatchSplitter<>(10);
        List<String> data = Collections.emptyList();
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("单条数据处理")
    void testSingleItem() {
        // Given
        splitter = new FixedSizeBatchSplitter<>(10);
        List<String> data = Arrays.asList("single_item");
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).hasSize(1);
        assertThat(result.get(0).get(0)).isEqualTo("single_item");
    }
    
    @Test
    @DisplayName("批次大小等于数据大小")
    void testBatchSizeEqualsDataSize() {
        // Given
        splitter = new FixedSizeBatchSplitter<>(20);
        List<String> data = createTestData(20);
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).hasSize(20);
        assertThat(result.get(0)).isEqualTo(data);
    }
    
    @Test
    @DisplayName("批次大小为1")
    void testBatchSizeOne() {
        // Given
        splitter = new FixedSizeBatchSplitter<>(1);
        List<String> data = Arrays.asList("a", "b", "c", "d", "e");
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(5);
        for (int i = 0; i < 5; i++) {
            assertThat(result.get(i)).hasSize(1);
            assertThat(result.get(i).get(0)).isEqualTo(data.get(i));
        }
    }
    
    @Test
    @DisplayName("批次大小大于数据大小")
    void testBatchSizeLargerThanData() {
        // Given
        splitter = new FixedSizeBatchSplitter<>(100);
        List<String> data = createTestData(10);
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).hasSize(10);
        assertThat(result.get(0)).isEqualTo(data);
    }
    
    @Test
    @DisplayName("精确整除的情况")
    void testExactDivision() {
        // Given
        splitter = new FixedSizeBatchSplitter<>(25);
        List<String> data = createTestData(100);
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(4);
        for (List<String> batch : result) {
            assertThat(batch).hasSize(25);
        }
    }
    
    @Test
    @DisplayName("null 输入处理")
    void testNullInput() {
        // Given
        splitter = new FixedSizeBatchSplitter<>(10);
        
        // When
        List<List<String>> result = splitter.split(null);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("构造函数参数验证")
    void testConstructorValidation() {
        // When & Then
        assertThatThrownBy(() -> new FixedSizeBatchSplitter<>(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Batch size must be positive");
        
        assertThatThrownBy(() -> new FixedSizeBatchSplitter<>(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Batch size must be positive");
        
        assertThatCode(() -> new FixedSizeBatchSplitter<>(1))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("getBatchSize() 方法")
    void testGetBatchSize() {
        // Given
        int batchSize = 15;
        splitter = new FixedSizeBatchSplitter<>(batchSize);
        
        // When
        int result = splitter.getBatchSize();
        
        // Then
        assertThat(result).isEqualTo(batchSize);
    }
    
    @Test
    @DisplayName("大批量数据拆分")
    void testLargeDataSet() {
        // Given
        splitter = new FixedSizeBatchSplitter<>(1000);
        List<String> data = createTestData(10000);
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(10);
        for (List<String> batch : result) {
            assertThat(batch).hasSize(1000);
        }
        
        // 验证数据完整性
        int totalItems = result.stream()
            .mapToInt(List::size)
            .sum();
        assertThat(totalItems).isEqualTo(10000);
    }
    
    @Test
    @DisplayName("不同数据类型的拆分")
    void testDifferentDataTypes() {
        // Given
        FixedSizeBatchSplitter<Integer> intSplitter = new FixedSizeBatchSplitter<>(3);
        List<Integer> intData = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        
        // When
        List<List<Integer>> result = intSplitter.split(intData);
        
        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).containsExactly(1, 2, 3);
        assertThat(result.get(1)).containsExactly(4, 5, 6);
        assertThat(result.get(2)).containsExactly(7);
    }
    
    @Test
    @DisplayName("边界情况：最小批次大小")
    void testMinimumBatchSize() {
        // Given
        splitter = new FixedSizeBatchSplitter<>(1);
        List<String> data = Arrays.asList("x", "y", "z");
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).containsExactly("x");
        assertThat(result.get(1)).containsExactly("y");
        assertThat(result.get(2)).containsExactly("z");
    }
    
    /**
     * 创建测试数据
     */
    private List<String> createTestData(int size) {
        return java.util.stream.IntStream.range(0, size)
            .mapToObj(i -> "item_" + i)
            .toList();
    }
}