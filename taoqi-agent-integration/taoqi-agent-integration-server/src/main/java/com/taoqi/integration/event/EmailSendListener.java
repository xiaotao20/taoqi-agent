package com.taoqi.integration.event;

import com.taoqi.integration.task.EmailSendTask;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 邮件发送事件监听器：在独立线程池中消费 {@code EmailSendEvent}，调用执行组件完成发送。
 *
 * <p>新增审计、指标、重试等需求时，只需再加监听器，发布方无需修改。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Component
public class EmailSendListener {

    private static final Logger log = LoggerFactory.getLogger(EmailSendListener.class);

    @Resource
    private EmailSendTask emailSendTask;

    @Async("emailSendExecutor")
    @EventListener
    public void onEmailSend(EmailSendEvent event) {
        log.info("收到邮件发送事件, recordId={}, to={}", event.recordId(), event.request().to());
        emailSendTask.send(event.recordId(), event.request());
    }
}