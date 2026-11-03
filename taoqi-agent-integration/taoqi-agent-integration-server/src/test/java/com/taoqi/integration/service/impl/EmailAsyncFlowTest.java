package com.taoqi.integration.service.impl;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import com.taoqi.integration.api.enums.EmailSendStatus;
import com.taoqi.integration.service.EmailRecordService;
import com.taoqi.integration.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * 异步发送链路的集成测试：提交后记录为发送中，线程池执行完成后状态更新为成功或失败。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@SpringBootTest
class EmailAsyncFlowTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailRecordService emailRecordService;

    @MockitoBean
    private JavaMailSender mailSender;

    private final EmailSendRequest request = new EmailSendRequest(
            List.of("receiver@example.com"), null, null, "异步测试", "内容", false);

    @Test
    @DisplayName("提交立即返回发送中，异步完成后状态变为成功")
    void submitsImmediatelyAndCompletesAsynchronously() {
        when(mailSender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));

        EmailSubmitResult result = emailService.submit(request);

        assertThat(result.status()).isEqualTo(EmailSendStatus.SENDING);
        assertThat(result.recordId()).isNotNull();

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var record = emailRecordService.getById(result.recordId());
            assertThat(record.getStatus()).isEqualTo(EmailSendStatus.SUCCESS);
            assertThat(record.getMessageId()).isNotBlank();
            assertThat(record.getSentAt()).isNotNull();
            assertThat(record.getCostMillis()).isNotNegative();
        });
    }

    @Test
    @DisplayName("异步发送失败时记录状态变为失败")
    void marksFailedWhenAsyncSendFails() {
        when(mailSender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        doThrow(new MailSendException("SMTP 连接失败")).when(mailSender).send(any(MimeMessage.class));

        EmailSubmitResult result = emailService.submit(request);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var record = emailRecordService.getById(result.recordId());
            assertThat(record.getStatus()).isEqualTo(EmailSendStatus.FAILED);
            assertThat(record.getErrorMessage()).contains("SMTP 连接失败");
            assertThat(record.getSentAt()).isNotNull();
        });
    }
}
