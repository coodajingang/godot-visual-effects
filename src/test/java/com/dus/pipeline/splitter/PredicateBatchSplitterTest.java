package com.dus.pipeline.splitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * PredicateBatchSplitter 单元测试
 * 验证条件批次拆分器的功能
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("PredicateBatchSplitter 测试")
class PredicateBatchSplitterTest {
    
    private PredicateBatchSplitter<String> splitter;
    
    @Test
    @DisplayName("按条件拆分：例如按类型分组")
    void testConditionalSplitting() {
        // Given
        Predicate<String> isEven = s -> s.endsWith("_even");
        splitter = new PredicateBatchSplitter<>(isEven);
        
        List<String> data = Arrays.asList(
            "item_1_odd", "item_2_even", "item_3_odd", "item_4_even", "item_5_odd"
        );
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(2);
        
        // 验证匹配条件的批次
        List<String> evenItems = result.get(0);
        assertThat(evenItems).containsExactly("item_2_even", "item_4_even");
        
        // 验证不匹配条件的批次
        List<String> oddItems = result.get(1);
        assertThat(oddItems).containsExactly("item_1_odd", "item_3_odd", "item_5_odd");
    }
    
    @Test
    @DisplayName("条件不匹配时原样返回")
    void testNoMatchReturnsOriginal() {
        // Given
        Predicate<String> startsWithZ = s -> s.startsWith("Z");
        splitter = new PredicateBatchSplitter<>(startsWithZ);
        
        List<String> data = Arrays.asList("apple", "banana", "cherry");
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(data);
    }
    
    @Test
    @DisplayName("全部匹配条件")
    void testAllMatchCondition() {
        // Given
        Predicate<String> isNotEmpty = s -> !s.isEmpty();
        splitter = new PredicateBatchSplitter<>(isNotEmpty);
        
        List<String> data = Arrays.asList("a", "b", "c", "d");
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(data);
    }
    
    @Test
    @DisplayName("空列表处理")
    void testEmptyList() {
        // Given
        Predicate<String> alwaysTrue = s -> true;
        splitter = new PredicateBatchSplitter<>(alwaysTrue);
        
        List<String> data = Collections.emptyList();
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("null 输入处理")
    void testNullInput() {
        // Given
        Predicate<String> alwaysTrue = s -> true;
        splitter = new PredicateBatchSplitter<>(alwaysTrue);
        
        // When
        List<List<String>> result = splitter.split(null);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("复杂条件拆分：数字范围")
    void testComplexConditionNumberRange() {
        // Given
        Predicate<Integer> isLarge = i -> i > 50;
        PredicateBatchSplitter<Integer> intSplitter = new PredicateBatchSplitter<>(isLarge);
        
        List<Integer> data = Arrays.asList(10, 20, 60, 30, 80, 40, 90, 50);
        
        // When
        List<List<Integer>> result = intSplitter.split(data);
        
        // Then
        assertThat(result).hasSize(2);
        
        // 验证大数字
        List<Integer> largeNumbers = result.get(0);
        assertThat(largeNumbers).containsExactly(60, 80, 90);
        
        // 验证小数字
        List<Integer> smallNumbers = result.get(1);
        assertThat(smallNumbers).containsExactly(10, 20, 30, 40, 50);
    }
    
    @Test
    @DisplayName("字符串长度条件")
    void testStringLengthCondition() {
        // Given
        Predicate<String> isLong = s -> s.length() > 5;
        splitter = new PredicateBatchSplitter<>(isLong);
        
        List<String> data = Arrays.asList("cat", "elephant", "dog", "giraffe", "bird");
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(2);
        
        List<String> longWords = result.get(0);
        assertThat(longWords).containsExactly("elephant", "giraffe");
        
        List<String> shortWords = result.get(1);
        assertThat(shortWords).containsExactly("cat", "dog", "bird");
    }
    
    @Test
    @DisplayName("复合条件：多个条件组合")
    void testComplexConditions() {
        // Given
        Predicate<String> startsWithAAndEndsWithZ = s -> s.startsWith("A") && s.endsWith("Z");
        splitter = new PredicateBatchSplitter<>(startsWithAAndEndsWithZ);
        
        List<String> data = Arrays.asList(
            "Apple", "ApplicationZ", "Banana", "AmazingZ", "Cherry", "AwesomeZ"
        );
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(2);
        
        List<String> matched = result.get(0);
        assertThat(matched).containsExactly("ApplicationZ", "AmazingZ", "AwesomeZ");
        
        List<String> unmatched = result.get(1);
        assertThat(unmatched).containsExactly("Apple", "Banana", "Cherry");
    }
    
    @Test
    @DisplayName("null 元素处理")
    void testNullElements() {
        // Given
        Predicate<String> isNotNull = s -> s != null;
        splitter = new PredicateBatchSplitter<>(isNotNull);
        
        List<String> data = Arrays.asList("a", null, "b", null, "c");
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(2);
        
        List<String> notNull = result.get(0);
        assertThat(notNull).containsExactly("a", "b", "c");
        
        List<String> nullElements = result.get(1);
        assertThat(nullElements).containsExactly(null, null);
    }
    
    @Test
    @DisplayName("构造函数参数验证")
    void testConstructorValidation() {
        // When & Then
        assertThatThrownBy(() -> new PredicateBatchSplitter<>(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Predicate cannot be null");
        
        // 正常构造不应该抛出异常
        Predicate<String> alwaysTrue = s -> true;
        assertThatCode(() -> new PredicateBatchSplitter<>(alwaysTrue))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("getPredicate() 方法")
    void testGetPredicate() {
        // Given
        Predicate<String> isEven = s -> s.endsWith("_even");
        splitter = new PredicateBatchSplitter<>(isEven);
        
        // When
        Predicate<String> result = splitter.getPredicate();
        
        // Then
        assertThat(result).isSameAs(isEven);
        assertThat(result.test("test_even")).isTrue();
        assertThat(result.test("test_odd")).isFalse();
    }
    
    @Test
    @DisplayName("单一匹配项")
    void testSingleMatch() {
        // Given
        Predicate<String> isTarget = s -> s.equals("target");
        splitter = new PredicateBatchSplitter<>(isTarget);
        
        List<String> data = Arrays.asList("a", "target", "b", "c");
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(2);
        
        List<String> matched = result.get(0);
        assertThat(matched).containsExactly("target");
        
        List<String> unmatched = result.get(1);
        assertThat(unmatched).containsExactly("a", "b", "c");
    }
    
    @Test
    @DisplayName("lambda 表达式条件")
    void testLambdaExpressionCondition() {
        // Given
        splitter = new PredicateBatchSplitter<>(s -> s.contains("error"));
        
        List<String> data = Arrays.asList(
            "info: started", "error: connection failed", "info: processing", 
            "warning: low memory", "error: timeout", "info: completed"
        );
        
        // When
        List<List<String>> result = splitter.split(data);
        
        // Then
        assertThat(result).hasSize(2);
        
        List<String> errors = result.get(0);
        assertThat(errors).containsExactly("error: connection failed", "error: timeout");
        
        List<String> nonErrors = result.get(1);
        assertThat(nonErrors).containsExactly("info: started", "info: processing", "warning: low memory", "info: completed");
    }
    
    @Test
    @DisplayName("方法引用条件")
    void testMethodReferenceCondition() {
        // Given
        PredicateBatchSplitter<String> stringSplitter = new PredicateBatchSplitter<>(String::isEmpty);
        
        List<String> data = Arrays.asList("hello", "", "world", "", "test");
        
        // When
        List<List<String>> result = stringSplitter.split(data);
        
        // Then
        assertThat(result).hasSize(2);
        
        List<String> empty = result.get(0);
        assertThat(empty).containsExactly("", "");
        
        List<String> nonEmpty = result.get(1);
        assertThat(nonEmpty).containsExactly("hello", "world", "test");
    }
}