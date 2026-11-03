package com.taoqi.integration.service.impl;

import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import com.taoqi.integration.api.enums.EmailSendStatus;
import com.taoqi.integration.config.MailProperties;
import com.taoqi.integration.event.EmailSendEvent;
import com.taoqi.integration.service.EmailRecordService;
import com.taoqi.integration.service.EmailService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 邮件发送服务实现：负责校验、创建发送记录并触发异步发送任务。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static final String MISSING_FROM_MESSAGE = "未配置发件人，请设置环境变量 QQ_MAIL_USERNAME 或 taoqi.mail.from";

    @Resource
    private MailProperties mailProperties;

    @Resource
    private EmailRecordService emailRecordService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    public EmailSubmitResult submit(EmailSendRequest request) {
        if (!StringUtils.hasText(mailProperties.from())) {
            Long failedRecordId = emailRecordService.create(request, EmailSendStatus.FAILED, MISSING_FROM_MESSAGE);
            return EmailSubmitResult.rejected(failedRecordId, MISSING_FROM_MESSAGE);
        }

        Long recordId = emailRecordService.create(request, EmailSendStatus.SENDING, null);
        // 发布事件，由 EmailSendListener 在线程池中异步执行发送
        eventPublisher.publishEvent(new EmailSendEvent(recordId, request));
        log.info("邮件发送事件已发布, recordId={}, to={}", recordId, request.to());
        return EmailSubmitResult.accepted(recordId);
    }
}
