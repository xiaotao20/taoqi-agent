package com.taoqi.integration.service.impl;

import java.util.List;

import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import com.taoqi.integration.api.enums.EmailSendStatus;
import com.taoqi.integration.config.MailProperties;
import com.taoqi.integration.event.EmailSendEvent;
import com.taoqi.integration.service.EmailRecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@code EmailServiceImpl} 的单元测试：验证提交阶段创建记录并发布发送事件。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private EmailRecordService emailRecordService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MailProperties mailProperties;

    @InjectMocks
    private EmailServiceImpl emailService;

    private final EmailSendRequest request = new EmailSendRequest(
            List.of("receiver@example.com"), null, null, "主题", "内容", false);

    @Test
    @DisplayName("配置齐全时先落一条发送中的记录，再发布发送事件")
    void submitsRecordAndPublishesEvent() {
        when(mailProperties.getFrom()).thenReturn("sender@qq.com");
        when(emailRecordService.create(any(), eq(EmailSendStatus.SENDING), isNull())).thenReturn(100L);

        EmailSubmitResult result = emailService.submit(request);

        assertThat(result.status()).isEqualTo(EmailSendStatus.SENDING);
        assertThat(result.recordId()).isEqualTo(100L);
        assertThat(result.message()).contains("正在发送");

        ArgumentCaptor<EmailSendEvent> captor = ArgumentCaptor.forClass(EmailSendEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().recordId()).isEqualTo(100L);
        assertThat(captor.getValue().request()).isEqualTo(request);
    }

    @Test
    @DisplayName("未配置发件人时直接落一条失败记录，不发布发送事件")
    void missingFromRecordsFailure() {
        when(emailRecordService.create(any(), eq(EmailSendStatus.FAILED), anyString())).thenReturn(101L);

        EmailSubmitResult result = emailService.submit(request);

        assertThat(result.status()).isEqualTo(EmailSendStatus.FAILED);
        assertThat(result.recordId()).isEqualTo(101L);
        assertThat(result.message()).contains("未配置发件人");
        verifyNoInteractions(eventPublisher);
    }
}