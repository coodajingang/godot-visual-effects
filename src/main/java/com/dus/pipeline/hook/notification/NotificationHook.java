package com.dus.pipeline.hook.notification;

import com.dus.pipeline.core.PipelineContext;
import com.dus.pipeline.hook.AfterPipelineHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 邮件通知钩子接口
 */
public interface EmailService {
    
    /**
     * 发送邮件
     * @param recipient 收件人邮箱
     * @param subject 邮件主题
     * @param message 邮件内容
     * @throws Exception 如果发送失败
     */
    void send(String recipient, String subject, String message) throws Exception;
}

/**
 * 通知钩子
 */
public class NotificationHook implements AfterPipelineHook {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationHook.class);
    
    private EmailService emailService;
    private String recipientEmail;
    
    public NotificationHook(EmailService emailService, String recipientEmail) {
        this.emailService = emailService;
        this.recipientEmail = recipientEmail;
    }
    
    @Override
    public void initialize() throws Exception {
        // 验证参数
        if (emailService == null) {
            throw new IllegalArgumentException("EmailService cannot be null");
        }
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email cannot be null or empty");
        }
    }
    
    @Override
    public void afterPipeline(PipelineContext context) throws Exception {
        long durationMs = context.getElapsedTimeMs();
        String message = String.format(
            "Pipeline completed successfully\n" +
            "Run ID: %s\n" +
            "Runtime: %d ms\n" +
            "Batches: %d\n" +
            "Total records: %d",
            context.getRunId(), durationMs, context.getBatchCount(), context.getTotalRecordCount()
        );
        
        emailService.send(recipientEmail, "Pipeline Success", message);
        logger.info("Success notification sent to {}", recipientEmail);
    }
    
    @Override
    public void onPipelineFailure(PipelineContext context, Exception exception) throws Exception {
        String message = String.format(
            "Pipeline failed after %d ms\n" +
            "Run ID: %s\n" +
            "Error: %s\n" +
            "Batches processed: %d\n" +
            "Records processed: %d",
            context.getElapsedTimeMs(),
            context.getRunId(),
            exception.getMessage(),
            context.getBatchCount(),
            context.getTotalRecordCount()
        );
        
        emailService.send(recipientEmail, "Pipeline Failed", message);
        logger.info("Failure notification sent to {}", recipientEmail);
    }
    
    @Override
    public void onPipelineInterrupted(PipelineContext context) throws Exception {
        String message = String.format(
            "Pipeline was interrupted\n" +
            "Run ID: %s\n" +
            "Runtime: %d ms\n" +
            "Batches processed: %d\n" +
            "Records processed: %d",
            context.getRunId(),
            context.getElapsedTimeMs(),
            context.getBatchCount(),
            context.getTotalRecordCount()
        );
        
        emailService.send(recipientEmail, "Pipeline Interrupted", message);
        logger.info("Interruption notification sent to {}", recipientEmail);
    }
    
    @Override
    public String name() {
        return "NotificationHook";
    }
}