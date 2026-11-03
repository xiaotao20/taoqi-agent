package com.taoqi.integration.task;

import java.nio.charset.StandardCharsets;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.config.MailProperties;
import com.taoqi.integration.service.EmailRecordService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 邮件发送执行组件：完成 SMTP 发送并把结果写回发送记录。
 *
 * <p>本身不加 {@code @Async}，由 {@code EmailSendListener} 在独立线程池中调用；
 * 这样事件监听负责调度，本类专注执行，也便于单元测试直接调用。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Component
public class EmailSendTask {

    private static final Logger log = LoggerFactory.getLogger(EmailSendTask.class);

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private MailProperties mailProperties;

    @Resource
    private EmailRecordService emailRecordService;

    /**
     * 发送邮件并更新记录状态。
     *
     * @param recordId 提交阶段创建的发送记录主键
     */
    public void send(Long recordId, EmailSendRequest request) {
        long startMillis = System.currentTimeMillis();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            String from = mailProperties.from();
            if (StringUtils.hasText(mailProperties.personal())) {
                helper.setFrom(new InternetAddress(from, mailProperties.personal(), StandardCharsets.UTF_8.name()));
            } else {
                helper.setFrom(from);
            }
            helper.setTo(request.to().toArray(String[]::new));
            setCcAndBcc(helper, request);
            helper.setSubject(request.subject());
            helper.setText(request.content(), request.htmlEnabled());
            // 提前落定 Content-Type、Message-ID 等头部，发送后可直接记录 Message-ID
            message.saveChanges();
            mailSender.send(message);

            long costMillis = System.currentTimeMillis() - startMillis;
            String messageId = message.getMessageID();
            emailRecordService.markSuccess(recordId, messageId, costMillis);
            log.info("邮件发送成功, recordId={}, to={}, messageId={}, cost={}ms",
                    recordId, request.to(), messageId, costMillis);
        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            long costMillis = System.currentTimeMillis() - startMillis;
            String reason = StringUtils.hasText(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
            emailRecordService.markFailed(recordId, reason, costMillis);
            log.error("邮件发送失败, recordId={}, to={}", recordId, request.to(), e);
        } catch (RuntimeException e) {
            long costMillis = System.currentTimeMillis() - startMillis;
            emailRecordService.markFailed(recordId, e.getMessage(), costMillis);
            log.error("邮件发送异常, recordId={}, to={}", recordId, request.to(), e);
        }
    }

    private void setCcAndBcc(MimeMessageHelper helper, EmailSendRequest request) throws MessagingException {
        if (request.cc() != null && !request.cc().isEmpty()) {
            helper.setCc(request.cc().toArray(String[]::new));
        }
        if (request.bcc() != null && !request.bcc().isEmpty()) {
            helper.setBcc(request.bcc().toArray(String[]::new));
        }
    }
}
