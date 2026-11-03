package com.taoqi.integration.task;

import java.util.List;
import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.config.MailProperties;
import com.taoqi.integration.service.EmailRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code EmailSendTask} 的单元测试：验证邮件组装与发送结果回写（直接调用方法，不经过异步线程池）。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ExtendWith(MockitoExtension.class)
class EmailSendTaskTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MailProperties mailProperties;

    @Mock
    private EmailRecordService emailRecordService;

    @InjectMocks
    private EmailSendTask emailSendTask;

    @BeforeEach
    void setUp() {
        when(mailProperties.from()).thenReturn("sender@qq.com");
        when(mailProperties.personal()).thenReturn("陶气助手");
        when(mailSender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
    }

    @Test
    @DisplayName("发送纯文本邮件成功后更新记录")
    void sendsTextEmailAndMarksSuccess() throws Exception {
        EmailSendRequest request = new EmailSendRequest(
                List.of("receiver@example.com"), null, null, "测试主题", "测试内容", false);

        emailSendTask.send(1L, request);

        MimeMessage message = captureSentMessage();
        assertThat(message.getSubject()).isEqualTo("测试主题");
        assertThat(message.getContentType()).startsWith("text/plain");
        assertThat(message.getContent()).isEqualTo("测试内容");

        InternetAddress from = (InternetAddress) message.getFrom()[0];
        assertThat(from.getAddress()).isEqualTo("sender@qq.com");
        assertThat(from.getPersonal()).isEqualTo("陶气助手");

        verify(emailRecordService).markSuccess(eq(1L), anyString(), anyLong());
    }

    @Test
    @DisplayName("发送 HTML 邮件并携带抄送、密送")
    void sendsHtmlEmailWithCcAndBcc() throws Exception {
        EmailSendRequest request = new EmailSendRequest(
                List.of("receiver@example.com"),
                List.of("cc@example.com"),
                List.of("bcc@example.com"),
                "HTML 主题",
                "<h1>你好</h1>",
                true);

        emailSendTask.send(2L, request);

        MimeMessage message = captureSentMessage();
        assertThat(message.getContentType()).startsWith("text/html");
        assertThat(message.getAllRecipients()).hasSize(3);
        verify(emailRecordService).markSuccess(eq(2L), anyString(), anyLong());
    }

    @Test
    @DisplayName("SMTP 异常时把记录标记为失败")
    void marksFailedWhenSendThrows() {
        doThrow(new MailSendException("SMTP 连接失败")).when(mailSender).send(any(MimeMessage.class));
        EmailSendRequest request = new EmailSendRequest(
                List.of("receiver@example.com"), null, null, "主题", "内容", false);

        emailSendTask.send(3L, request);

        verify(emailRecordService).markFailed(eq(3L), contains("SMTP 连接失败"), anyLong());
    }

    private MimeMessage captureSentMessage() {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }
}