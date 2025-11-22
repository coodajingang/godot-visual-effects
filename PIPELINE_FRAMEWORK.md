# Pipeline 框架 - 生命周期、异步、Metrics 和批次拆分增强

## 概述

本文档描述了 Pipeline 框架的完整增强，包括以下核心功能：

1. **Pipeline 生命周期管理** - INIT → RUNNING → STOPPING → STOPPED/FAILED
2. **算子级 Metrics & 耗时统计** - 每个算子的性能监控
3. **批次拆分器（BatchSplitter）策略** - 灵活的批次处理策略
4. **异步算子支持** - 完整的异步/非阻塞处理支持

## 项目结构

```
src/main/java/com/dus/pipeline/
├── core/
│   ├── Operator.java              # 基础算子接口
│   ├── AbstractOperator.java       # 抽象算子（模板方法模式）
│   ├── SourceOperator.java         # 数据源算子
│   ├── SinkOperator.java           # 数据写入算子
│   ├── Pipeline.java               # 主流程类（已增强）
│   └── PipelineStatus.java         # Pipeline 状态枚举 [新增]
│
├── metrics/                        # [新增包]
│   ├── OperatorMetrics.java        # 算子指标类
│   ├── MetricsCollector.java       # 指标收集器接口
│   └── DefaultMetricsCollector.java # 默认实现
│
├── splitter/                       # [新增包]
│   ├── BatchSplitter.java          # 批次拆分器接口
│   ├── NoBatchSplitter.java        # 不拆分实现
│   ├── FixedSizeBatchSplitter.java # 按固定大小拆分
│   └── PredicateBatchSplitter.java # 按条件拆分
│
├── async/                          # [新增包]
│   ├── AsyncOperator.java          # 异步算子基类
│   ├── AsyncSourceOperator.java    # 异步数据源
│   ├── AsyncSinkOperator.java      # 异步写入
│   └── AsyncPipeline.java          # 异步流程类
│
└── example/
    ├── {现有示例}
    ├── sync/                       # [新增]
    │   ├── SyncPipelineExample.java
    │   ├── BatchSplitterExample.java
    │   └── MetricsReportExample.java
    └── async/                      # [新增]
        ├── AsyncPipelineExample.java
        ├── AsyncTransformOperator.java
        ├── AsyncEnrichOperator.java
        └── AsyncWriteToDbOperator.java
```

## 核心功能详解

### 1. Pipeline 生命周期管理

#### PipelineStatus 枚举

```java
public enum PipelineStatus {
    INIT,      // 初始化状态
    RUNNING,   // 运行中
    STOPPING,  // 停止中
    STOPPED,   // 已停止
    FAILED     // 失败
}
```

#### Pipeline 新增方法

```java
// 生命周期管理
void shutdown();                              // 优雅关闭
void shutdownNow();                           // 强制关闭
boolean awaitTermination(long timeout, TimeUnit unit);
PipelineStatus getStatus();
ExecutorService getExecutor();

// Metrics 相关
void setMetricsCollector(MetricsCollector);
Map<String, OperatorMetrics> getMetrics();
void printMetricsReport();

// 批次拆分
void setBatchSplitter(BatchSplitter<I>);
BatchSplitter<I> getBatchSplitter();
```

### 2. Metrics & 耗时统计

#### OperatorMetrics 类

记录单个算子的统计数据：

```java
OperatorMetrics {
    invokeCount       // 调用次数
    successCount      // 成功次数
    failureCount      // 失败次数
    totalDurationNanos // 总耗时（纳秒）
    minDurationNanos  // 最小耗时
    maxDurationNanos  // 最大耗时
    avgDurationNanos  // 平均耗时
}
```

#### MetricsCollector 接口

```java
interface MetricsCollector {
    void recordStart(String operatorName);
    void recordSuccess(String operatorName, long durationNanos);
    void recordFailure(String operatorName, long durationNanos, Throwable ex);
    OperatorMetrics getMetrics(String operatorName);
    Map<String, OperatorMetrics> getAllMetrics();
    void reset();
}
```

#### DefaultMetricsCollector 实现

- 使用 `ConcurrentHashMap` 保证线程安全
- 自动创建和管理每个算子的 metrics

### 3. 批次拆分器（BatchSplitter）

#### BatchSplitter 接口

```java
interface BatchSplitter<T> {
    boolean shouldSplit(T batch);    // 是否需要拆分
    List<T> split(T batch);           // 拆分批次
    String name();
}
```

#### 内置实现

1. **NoBatchSplitter** - 不拆分
2. **FixedSizeBatchSplitter** - 按固定大小拆分
3. **PredicateBatchSplitter** - 按条件拆分

### 4. 异步算子支持

#### AsyncOperator 基类

```java
abstract class AsyncOperator<I, O> implements Operator<I, O> {
    public abstract CompletableFuture<O> processAsync(I input);
    protected Executor getExecutor() { /* 默认 ForkJoinPool */ }
    @Override
    public O process(I input) { /* 通过 join() 实现同步包装 */ }
}
```

#### AsyncPipeline 类

- 支持异步算子链的调度
- 返回 `CompletableFuture<Void>` 用于非阻塞操作
- 支持 metrics 收集和生命周期管理

#### 辅助类

- `AsyncSourceOperator<O>` - 异步数据源
- `AsyncSinkOperator<I>` - 异步写入

## 使用示例

### 示例 1：同步 Pipeline + Metrics

```java
Pipeline<List<String>, Void> pipeline = new Pipeline<>(new MySourceOperator());

pipeline.addOperator(new TransformOperator())
        .addOperator(new EnrichOperator())
        .addOperator(new WriteToDbOperator());

pipeline.setMetricsCollector(new DefaultMetricsCollector());
pipeline.run();

pipeline.printMetricsReport();
pipeline.shutdown();
```

### 示例 2：使用 BatchSplitter

```java
pipeline.setBatchSplitter(new FixedSizeBatchSplitter<>(100));
// 或
pipeline.setBatchSplitter(new PredicateBatchSplitter<>(item -> classify(item)));
pipeline.run();
```

### 示例 3：异步 Pipeline

```java
AsyncPipeline<List<String>, Void> pipeline = 
    new AsyncPipeline<>(new AsyncDataSourceOperator());

pipeline.addOperator(new AsyncTransformOperator())
        .addOperator(new AsyncEnrichOperator())
        .addOperator(new AsyncWriteToDbOperator());

CompletableFuture<Void> future = pipeline.runAsync();
// 非阻塞操作
future.get();
pipeline.printMetricsReport();
```

## 关键特性

✅ **完整的生命周期管理** - INIT → RUNNING → STOPPING → STOPPED/FAILED
✅ **性能可观测性** - 每个算子的耗时、成功/失败统计
✅ **灵活的批次拆分** - 支持固定大小、条件拆分等策略
✅ **异步非阻塞支持** - 基于 CompletableFuture 的异步处理
✅ **线程安全** - 所有 metrics 操作都是线程安全的
✅ **易用的 API** - 链式调用、默认实现、丰富的示例

## 性能报告示例

```
========== Pipeline Metrics Report ==========
Pipeline Status: STOPPED
Total Batches Processed: 2
Total Duration: 45.23 ms

Operator Metrics:
---
TransformOperator: invoke=2, success=2, failure=0, avg=5.12ms, min=4ms, max=6ms
EnrichOperator: invoke=2, success=2, failure=0, avg=12.34ms, min=11ms, max=13ms
WriteToDbOperator: invoke=2, success=2, failure=0, avg=28.56ms, min=27ms, max=30ms
============================================
```

## 技术栈

- **Java 8+** - 利用 CompletableFuture、Lambda、Stream API
- **并发工具** - ExecutorService、ConcurrentHashMap、AtomicLong
- **设计模式** - 模板方法、策略模式、装饰器模式

## 文件清单

### 核心模块
- `core/PipelineStatus.java` - Pipeline 状态
- `core/Pipeline.java` - 主流程（已增强）

### Metrics 模块
- `metrics/OperatorMetrics.java` - 指标类
- `metrics/MetricsCollector.java` - 收集器接口
- `metrics/DefaultMetricsCollector.java` - 默认实现

### Splitter 模块
- `splitter/BatchSplitter.java` - 拆分器接口
- `splitter/NoBatchSplitter.java` - 无拆分
- `splitter/FixedSizeBatchSplitter.java` - 固定大小拆分
- `splitter/PredicateBatchSplitter.java` - 条件拆分

### Async 模块
- `async/AsyncOperator.java` - 异步算子基类
- `async/AsyncSourceOperator.java` - 异步数据源
- `async/AsyncSinkOperator.java` - 异步写入
- `async/AsyncPipeline.java` - 异步流程

### 示例
- `example/sync/SyncPipelineExample.java`
- `example/sync/BatchSplitterExample.java`
- `example/sync/MetricsReportExample.java`
- `example/async/AsyncPipelineExample.java`
- `example/async/AsyncTransformOperator.java`
- `example/async/AsyncEnrichOperator.java`
- `example/async/AsyncWriteToDbOperator.java`

## 线程安全性

- **DefaultMetricsCollector** - 使用 ConcurrentHashMap，所有操作都是线程安全的
- **OperatorMetrics** - 使用 synchronized 方法保护计数操作
- **Pipeline** - 使用 AtomicLong 管理批次计数

## 异常处理

- Pipeline 中的异常会被记录到 metrics
- 异步操作中的异常会通过 CompletableFuture 异常链传播
- 失败状态会被记录到 PipelineStatus
