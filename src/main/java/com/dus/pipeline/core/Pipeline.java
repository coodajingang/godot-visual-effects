package com.dus.pipeline.core;

import com.dus.pipeline.hook.AfterPipelineHook;
import com.dus.pipeline.hook.BeforePipelineHook;
import com.dus.pipeline.exception.HookExecutionException;
import com.dus.pipeline.exception.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline 主类，支持生命周期钩子
 */
public class Pipeline<I, O> implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);
    
    private Source<I> source;
    private List<Operator> operators = new ArrayList<>();
    private List<BeforePipelineHook> beforeHooks = new ArrayList<>();
    private List<AfterPipelineHook> afterHooks = new ArrayList<>();
    private PipelineContext context;
    private PipelineStatus pipelineStatus = PipelineStatus.INITIALIZED;
    
    public Pipeline(Source<I> source) {
        this.source = source;
    }
    
    /**
     * 添加操作符
     */
    public Pipeline<I, O> addOperator(Operator<?, ?> operator) {
        operators.add(operator);
        return this;
    }
    
    /**
     * 添加 Pipeline 开始前的钩子
     */
    public Pipeline<I, O> addBeforeHook(BeforePipelineHook hook) {
        beforeHooks.add(hook);
        return this;
    }
    
    /**
     * 添加多个开始前钩子
     */
    public Pipeline<I, O> addBeforeHooks(BeforePipelineHook... hooks) {
        for (BeforePipelineHook hook : hooks) {
            beforeHooks.add(hook);
        }
        return this;
    }
    
    /**
     * 添加 Pipeline 结束后的钩子
     */
    public Pipeline<I, O> addAfterHook(AfterPipelineHook hook) {
        afterHooks.add(hook);
        return this;
    }
    
    /**
     * 添加多个结束后钩子
     */
    public Pipeline<I, O> addAfterHooks(AfterPipelineHook... hooks) {
        for (AfterPipelineHook hook : hooks) {
            afterHooks.add(hook);
        }
        return this;
    }
    
    /**
     * 设置 Pipeline 上下文（可选，若不设置则自动创建）
     */
    public Pipeline<I, O> withContext(PipelineContext context) {
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
     * 获取 Pipeline 状态
     */
    public PipelineStatus getPipelineStatus() {
        return pipelineStatus;
    }
    
    /**
     * 改造后的 run() 方法，集成钩子
     */
    @Override
    public void run() {
        try {
            executePipeline();
        } catch (Exception e) {
            if (e instanceof PipelineException) {
                throw (PipelineException) e;
            } else {
                throw new PipelineException("Pipeline execution failed", e);
            }
        }
    }
    
    /**
     * 执行 Pipeline 的核心逻辑
     */
    private void executePipeline() throws Exception {
        // 初始化上下文
        if (context == null) {
            context = new PipelineContext();
        }
        context.setStartTime(System.currentTimeMillis());
        
        try {
            // 1. 执行所有 BeforePipelineHook
            logger.info("Executing {} before-pipeline hooks...", beforeHooks.size());
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
            
            // 2. 执行主 Pipeline 逻辑
            logger.info("Pipeline started with source [{}]", source.name());
            pipelineStatus = PipelineStatus.RUNNING;
            
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
            
            pipelineStatus = PipelineStatus.STOPPED;
            logger.info("Pipeline completed successfully. Processed {} batches with {} total records", 
                       context.getBatchCount(), context.getTotalRecordCount());
            
            // 3. 执行所有 AfterPipelineHook（成功情况）
            logger.info("Executing {} after-pipeline hooks...", afterHooks.size());
            for (AfterPipelineHook hook : afterHooks) {
                try {
                    hook.afterPipeline(context);
                    logger.info("AfterPipelineHook [{}] completed", hook.name());
                } catch (Exception e) {
                    logger.warn("AfterPipelineHook [{}] failed after successful pipeline", hook.name(), e);
                    // 不影响已完成的 Pipeline
                }
            }
            
        } catch (Exception e) {
            pipelineStatus = PipelineStatus.FAILED;
            logger.error("Pipeline failed after {} ms", context.getElapsedTimeMs(), e);
            
            // 3. 执行所有 AfterPipelineHook（失败情况）
            logger.info("Executing after-pipeline hooks due to failure...");
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