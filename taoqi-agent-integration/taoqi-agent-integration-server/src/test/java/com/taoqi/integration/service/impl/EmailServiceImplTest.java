package com.taoqi.integration.service.impl;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSendResult;
import com.taoqi.integration.config.MailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@code EmailServiceImpl} 的单元测试：用模拟的 {@code JavaMailSender} 验证邮件组装与异常处理。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
class EmailServiceImplTest {

    private JavaMailSender mailSender;
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        emailService = new EmailServiceImpl(mailSender, new MailProperties("sender@qq.com", "陶气助手"));
    }

    @Test
    @DisplayName("发送纯文本邮件")
    void sendTextEmail() throws Exception {
        EmailSendResult result = emailService.send(new EmailSendRequest(
                List.of("receiver@example.com"), null, null, "测试主题", "测试内容", false));

        assertThat(result.success()).isTrue();
        assertThat(result.messageId()).isNotBlank();

        MimeMessage message = captureSentMessage();
        assertThat(message.getSubject()).isEqualTo("测试主题");
        assertThat(message.getContentType()).startsWith("text/plain");
        assertThat(message.getContent()).isEqualTo("测试内容");

        InternetAddress from = (InternetAddress) message.getFrom()[0];
        assertThat(from.getAddress()).isEqualTo("sender@qq.com");
        assertThat(from.getPersonal()).isEqualTo("陶气助手");

        assertThat(message.getAllRecipients()).hasSize(1);
        assertThat(((InternetAddress) message.getAllRecipients()[0]).getAddress())
                .isEqualTo("receiver@example.com");
    }

    @Test
    @DisplayName("发送 HTML 邮件并携带抄送、密送")
    void sendHtmlEmailWithCcAndBcc() throws Exception {
        EmailSendResult result = emailService.send(new EmailSendRequest(
                List.of("receiver@example.com"),
                List.of("cc@example.com"),
                List.of("bcc@example.com"),
                "HTML 主题",
                "<h1>你好</h1>",
                true));

        assertThat(result.success()).isTrue();

        MimeMessage message = captureSentMessage();
        assertThat(message.getContentType()).startsWith("text/html");
        assertThat(message.getAllRecipients()).hasSize(3);
    }

    @Test
    @DisplayName("SMTP 发送异常时返回失败结果")
    void sendFailureReturnsFailedResult() {
        doThrow(new MailSendException("SMTP 连接失败")).when(mailSender).send(any(MimeMessage.class));

        EmailSendResult result = emailService.send(new EmailSendRequest(
                List.of("receiver@example.com"), null, null, "主题", "内容", false));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("SMTP 连接失败");
    }

    @Test
    @DisplayName("未配置发件人时直接返回失败，不访问 SMTP")
    void missingFromReturnsFailedResult() {
        EmailServiceImpl serviceWithoutFrom = new EmailServiceImpl(mailSender, new MailProperties(null, null));

        EmailSendResult result = serviceWithoutFrom.send(new EmailSendRequest(
                List.of("receiver@example.com"), null, null, "主题", "内容", false));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("未配置发件人");
        verifyNoInteractions(mailSender);
    }

    private MimeMessage captureSentMessage() {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }
}
