package com.taoqi.integration.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSendResult;
import com.taoqi.integration.config.MailProperties;
import com.taoqi.integration.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 邮件发送服务实现：通过 {@code JavaMailSender} 使用 QQ SMTP 发送邮件。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public EmailServiceImpl(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public EmailSendResult send(EmailSendRequest request) {
        String from = mailProperties.from();
        if (!StringUtils.hasText(from)) {
            return EmailSendResult.fail("未配置发件人，请设置环境变量 QQ_MAIL_USERNAME 或 taoqi.mail.from");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            if (StringUtils.hasText(mailProperties.personal())) {
                helper.setFrom(new InternetAddress(from, mailProperties.personal(), StandardCharsets.UTF_8.name()));
            } else {
                helper.setFrom(from);
            }
            helper.setTo(request.to().toArray(String[]::new));
            setCcAndBcc(helper, request.cc(), request.bcc());
            helper.setSubject(request.subject());
            helper.setText(request.content(), request.htmlEnabled());
            // 提前落定 Content-Type、Message-ID 等头部，发送后可直接返回 Message-ID
            message.saveChanges();
            mailSender.send(message);

            String messageId = message.getMessageID();
            log.info("邮件发送成功, to={}, subject={}, messageId={}", request.to(), request.subject(), messageId);
            return EmailSendResult.ok(messageId);
        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            log.error("邮件发送失败, to={}, subject={}", request.to(), request.subject(), e);
            String reason = StringUtils.hasText(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
            return EmailSendResult.fail(reason);
        }
    }

    private void setCcAndBcc(MimeMessageHelper helper, List<String> cc, List<String> bcc) throws MessagingException {
        if (cc != null && !cc.isEmpty()) {
            helper.setCc(cc.toArray(String[]::new));
        }
        if (bcc != null && !bcc.isEmpty()) {
            helper.setBcc(bcc.toArray(String[]::new));
        }
    }
}
