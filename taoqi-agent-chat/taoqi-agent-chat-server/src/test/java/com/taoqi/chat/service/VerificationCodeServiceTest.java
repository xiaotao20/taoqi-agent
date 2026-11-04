package com.taoqi.chat.service;

import com.taoqi.chat.cache.VerificationCodeStore;
import com.taoqi.chat.facade.EmailClientFacade;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import com.taoqi.integration.api.enums.EmailSendStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code VerificationCodeService} 的单元测试：验证验证码生成、缓存与 HTML 邮件组装。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

    @Mock
    private VerificationCodeStore verificationCodeStore;

    @Mock
    private EmailClientFacade emailClientFacade;

    @InjectMocks
    private VerificationCodeService verificationCodeService;

    @Test
    @DisplayName("生成 6 位验证码写入缓存，并发送包含验证码的 HTML 邮件")
    void sendsHtmlEmailAndCachesCode() {
        when(emailClientFacade.send(any())).thenReturn(EmailSubmitResult.accepted(1L));

        EmailSubmitResult result = verificationCodeService.send("user@example.com");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(verificationCodeStore).put(eq("user@example.com"), codeCaptor.capture());
        String code = codeCaptor.getValue();
        assertThat(code).matches("\\d{6}");

        ArgumentCaptor<EmailSendRequest> requestCaptor = ArgumentCaptor.forClass(EmailSendRequest.class);
        verify(emailClientFacade).send(requestCaptor.capture());
        EmailSendRequest request = requestCaptor.getValue();

        assertThat(request.to()).containsExactly("user@example.com");
        assertThat(request.htmlEnabled()).isTrue();
        assertThat(request.subject()).contains("verification code");
        assertThat(request.content()).contains(code);
        assertThat(request.content()).contains("expires in 5 minutes");
        assertThat(request.content()).contains("taoqi-agent");

        assertThat(result.status()).isEqualTo(EmailSendStatus.SENDING);
    }

    @Test
    @DisplayName("校验验证码时直接委托给存储")
    void verifiesThroughStore() {
        when(verificationCodeStore.verify("user@example.com", "123456")).thenReturn(true);

        assertThat(verificationCodeService.verify("user@example.com", "123456")).isTrue();

        verify(verificationCodeStore).verify("user@example.com", "123456");
    }
}