package com.dus.pipeline.core;

import com.dus.pipeline.hook.AfterPipelineHook;
import com.dus.pipeline.hook.BeforePipelineHook;
import com.dus.pipeline.exception.HookExecutionException;
import com.dus.pipeline.exception.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 异步 Pipeline 类，支持生命周期钩子
 */
public class AsyncPipeline<I, O> {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncPipeline.class);
    
    private Source<I> source;
    private List<Operator> operators = new ArrayList<>();
    private List<BeforePipelineHook> beforeHooks = new ArrayList<>();
    private List<AfterPipelineHook> afterHooks = new ArrayList<>();
    private PipelineContext context;
    private Executor executor;
    
    public AsyncPipeline(Source<I> source) {
        this.source = source;
    }
    
    public AsyncPipeline(Source<I> source, Executor executor) {
        this.source = source;
        this.executor = executor;
    }
    
    /**
     * 添加操作符
     */
    public AsyncPipeline<I, O> addOperator(Operator<?, ?> operator) {
        operators.add(operator);
        return this;
    }
    
    /**
     * 添加 Pipeline 开始前的钩子
     */
    public AsyncPipeline<I, O> addBeforeHook(BeforePipelineHook hook) {
        beforeHooks.add(hook);
        return this;
    }
    
    /**
     * 添加多个开始前钩子
     */
    public AsyncPipeline<I, O> addBeforeHooks(BeforePipelineHook... hooks) {
        for (BeforePipelineHook hook : hooks) {
            beforeHooks.add(hook);
        }
        return this;
    }
    
    /**
     * 添加 Pipeline 结束后的钩子
     */
    public AsyncPipeline<I, O> addAfterHook(AfterPipelineHook hook) {
        afterHooks.add(hook);
        return this;
    }
    
    /**
     * 添加多个结束后钩子
     */
    public AsyncPipeline<I, O> addAfterHooks(AfterPipelineHook... hooks) {
        for (AfterPipelineHook hook : hooks) {
            afterHooks.add(hook);
        }
        return this;
    }
    
    /**
     * 设置 Pipeline 上下文（可选，若不设置则自动创建）
     */
    public AsyncPipeline<I, O> withContext(PipelineContext context) {
        this.context = context;
        return this;
    }
    
    /**
     * 获取 Pipeline 上下文
     */
    public PipelineContext getContext() {
        return this.context;
    }
    
    /**
     * 异步执行 Pipeline，集成钩子
     */
    public CompletableFuture<Void> runAsync() {
        Executor exec = executor != null ? executor : CompletableFuture.defaultExecutor();
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                executeAsyncPipeline();
                return null;
            } catch (Exception e) {
                if (e instanceof PipelineException) {
                    throw new CompletionException((PipelineException) e);
                } else {
                    throw new CompletionException(new PipelineException("Async pipeline execution failed", e));
                }
            }
        }, exec);
    }
    
    /**
     * 执行异步 Pipeline 的核心逻辑
     */
    private void executeAsyncPipeline() throws Exception {
        // 初始化上下文
        if (context == null) {
            context = new PipelineContext();
        }
        context.setStartTime(System.currentTimeMillis());
        
        try {
            // 执行前置钩子
            logger.info("Executing {} before-pipeline hooks for async pipeline...", beforeHooks.size());
            for (BeforePipelineHook hook : beforeHooks) {
                try {
                    hook.initialize();
                    hook.beforePipeline(context);
                    logger.info("BeforePipelineHook [{}] completed", hook.name());
                } catch (Exception e) {
                    logger.error("BeforePipelineHook [{}] failed", hook.name(), e);
                    throw new HookExecutionException(hook.name(), "Before pipeline hook execution failed", e);
                }
            }
            
            // 异步管道主逻辑
            logger.info("Async pipeline started with source [{}]", source.name());
            
            while (true) {
                I batch = source.nextBatch();
                
                if (batch == null || isEmpty(batch)) {
                    break;
                }
                
                context.incrementBatchCount();
                context.addToTotalRecordCount(getRecordCount(batch));
                
                Object data = batch;
                for (Operator op : operators) {
                    try {
                        data = op.process(data);
                    } catch (Exception e) {
                        logger.error("Operator [{}] failed", op.name(), e);
                        throw new PipelineException("Operator [" + op.name() + "] processing failed", e);
                    }
                }
            }
            
            logger.info("Async pipeline completed successfully. Processed {} batches with {} total records", 
                       context.getBatchCount(), context.getTotalRecordCount());
            
            // 执行后置钩子
            logger.info("Executing {} after-pipeline hooks for async pipeline...", afterHooks.size());
            for (AfterPipelineHook hook : afterHooks) {
                try {
                    hook.afterPipeline(context);
                    logger.info("AfterPipelineHook [{}] completed", hook.name());
                } catch (Exception e) {
                    logger.warn("AfterPipelineHook [{}] failed after successful async pipeline", hook.name(), e);
                    // 不影响已完成的 Pipeline
                }
            }
            
        } catch (Exception e) {
            logger.error("Async pipeline failed after {} ms", context.getElapsedTimeMs(), e);
            
            // 执行失败后的钩子
            logger.info("Executing after-pipeline hooks due to async pipeline failure...");
            for (AfterPipelineHook hook : afterHooks) {
                try {
                    hook.onPipelineFailure(context, e);
                    logger.info("AfterPipelineHook [{}] completed failure handling", hook.name());
                } catch (Exception hookEx) {
                    logger.warn("AfterPipelineHook [{}] failed during failure handling", hook.name(), hookEx);
                }
            }
            
            throw e;
        }
    }
    
    /**
     * 检查批次是否为空
     */
    private boolean isEmpty(I batch) {
        if (batch == null) {
            return true;
        }
        
        // 对于集合类型
        if (batch instanceof Iterable) {
            return !((Iterable<?>) batch).iterator().hasNext();
        }
        
        // 对于数组
        if (batch.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(batch) == 0;
        }
        
        // 默认认为非空
        return false;
    }
    
    /**
     * 获取批次中的记录数
     */
    private long getRecordCount(I batch) {
        if (batch == null) {
            return 0;
        }
        
        // 对于集合类型
        if (batch instanceof Iterable) {
            long count = 0;
            for (Object item : (Iterable<?>) batch) {
                count++;
            }
            return count;
        }
        
        // 对于数组
        if (batch.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(batch);
        }
        
        // 默认返回 1（单个对象）
        return 1;
    }
}