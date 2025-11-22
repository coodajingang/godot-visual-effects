package com.dus.pipeline.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 测试数据工厂类
 * 用于生成各种类型的测试数据
 * 
 * @author Dus
 * @version 1.0
 */
public class TestDataFactory {
    
    private static final Random random = new Random();
    
    /**
     * 创建字符串列表数据
     * 
     * @param size 数据大小
     * @return 字符串列表
     */
    public static List<String> createStringList(int size) {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add("test_item_" + i + "_" + System.currentTimeMillis() % 10000);
        }
        return data;
    }
    
    /**
     * 创建整数列表数据
     * 
     * @param size 数据大小
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return 整数列表
     */
    public static List<Integer> createIntegerList(int size, int minValue, int maxValue) {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(random.nextInt(maxValue - minValue + 1) + minValue);
        }
        return data;
    }
    
    /**
     * 创建Map列表数据（模拟JSON对象）
     * 
     * @param size 数据大小
     * @return Map列表
     */
    public static List<Map<String, Object>> createMapList(int size) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i + 1);
            item.put("name", "item_" + i);
            item.put("value", random.nextDouble() * 100);
            item.put("active", random.nextBoolean());
            item.put("timestamp", System.currentTimeMillis());
            data.add(item);
        }
        return data;
    }
    
    /**
     * 创建用户数据
     * 
     * @param count 用户数量
     * @return 用户数据列表
     */
    public static List<Map<String, Object>> createUserData(int count) {
        List<Map<String, Object>> users = new ArrayList<>();
        String[] firstNames = {"John", "Jane", "Bob", "Alice", "Charlie", "Diana", "Eve", "Frank"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
        
        for (int i = 0; i < count; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", i + 1);
            user.put("firstName", firstNames[random.nextInt(firstNames.length)]);
            user.put("lastName", lastNames[random.nextInt(lastNames.length)]);
            user.put("email", "user" + (i + 1) + "@example.com");
            user.put("age", random.nextInt(50) + 18); // 18-67岁
            user.put("salary", random.nextInt(100000) + 30000); // 30k-130k
            user.put("active", random.nextBoolean());
            user.put("department", "Dept_" + (random.nextInt(5) + 1));
            users.add(user);
        }
        return users;
    }
    
    /**
     * 创建日志数据
     * 
     * @param count 日志条数
     * @return 日志数据列表
     */
    public static List<Map<String, Object>> createLogData(int count) {
        List<Map<String, Object>> logs = new ArrayList<>();
        String[] levels = {"INFO", "WARN", "ERROR", "DEBUG"};
        String[] messages = {
            "Application started successfully",
            "Database connection established",
            "Processing user request",
            "Cache miss for key",
            "Invalid input parameter",
            "Memory usage high",
            "Network timeout occurred",
            "File not found",
            "Authentication failed",
            "Data validation error"
        };
        
        for (int i = 0; i < count; i++) {
            Map<String, Object> log = new HashMap<>();
            log.put("id", i + 1);
            log.put("timestamp", System.currentTimeMillis() - random.nextInt(86400000)); // 过去24小时内
            log.put("level", levels[random.nextInt(levels.length)]);
            log.put("message", messages[random.nextInt(messages.length)]);
            log.put("thread", "Thread-" + (random.nextInt(10) + 1));
            log.put("logger", "com.example.Service" + (random.nextInt(5) + 1));
            logs.add(log);
        }
        return logs;
    }
    
    /**
     * 创建订单数据
     * 
     * @param count 订单数量
     * @return 订单数据列表
     */
    public static List<Map<String, Object>> createOrderData(int count) {
        List<Map<String, Object>> orders = new ArrayList<>();
        String[] statuses = {"PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"};
        String[] products = {"Laptop", "Phone", "Tablet", "Monitor", "Keyboard", "Mouse", "Headphones", "Camera"};
        
        for (int i = 0; i < count; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("orderId", "ORD-" + String.format("%06d", i + 1));
            order.put("customerId", random.nextInt(1000) + 1);
            order.put("product", products[random.nextInt(products.length)]);
            order.put("quantity", random.nextInt(5) + 1);
            order.put("price", random.nextDouble() * 1000 + 50);
            order.put("status", statuses[random.nextInt(statuses.length)]);
            order.put("orderDate", System.currentTimeMillis() - random.nextInt(30 * 86400000L)); // 过去30天内
            order.put("shippingAddress", "Address " + (random.nextInt(100) + 1));
            orders.add(order);
        }
        return orders;
    }
    
    /**
     * 创建性能测试数据
     * 
     * @param size 数据大小
     * @param complexity 复杂度（1-5）
     * @return 复杂数据列表
     */
    public static List<Map<String, Object>> createPerformanceTestData(int size, int complexity) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 0; i < size; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("name", "perf_test_" + i);
            
            // 根据复杂度添加不同数量的字段
            for (int j = 0; j < complexity * 5; j++) {
                item.put("field_" + j, random.nextDouble() * 1000);
                item.put("text_" + j, "sample_text_" + j + "_" + random.nextInt(1000));
                item.put("flag_" + j, random.nextBoolean());
            }
            
            // 添加嵌套结构
            Map<String, Object> nested = new HashMap<>();
            for (int k = 0; k < complexity; k++) {
                nested.put("nested_" + k, "nested_value_" + k);
            }
            item.put("nested", nested);
            
            data.add(item);
        }
        
        return data;
    }
    
    /**
     * 创建错误测试数据
     * 
     * @param size 数据大小
     * @param errorRate 错误率（0.0-1.0）
     * @return 包含错误的数据列表
     */
    public static List<Map<String, Object>> createErrorTestData(int size, double errorRate) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 0; i < size; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i + 1);
            
            if (random.nextDouble() < errorRate) {
                // 创建错误数据
                item.put("name", null); // null值
                item.put("value", "invalid_number"); // 无效数字
                item.put("active", "not_boolean"); // 无效布尔值
                item.put("error", true);
            } else {
                // 创建正常数据
                item.put("name", "valid_item_" + i);
                item.put("value", random.nextInt(100));
                item.put("active", random.nextBoolean());
                item.put("error", false);
            }
            
            data.add(item);
        }
        
        return data;
    }
}