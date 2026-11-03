package com.taoqi.chat.controller;

import com.taoqi.chat.dto.VerificationCodeRequest;
import com.taoqi.chat.service.VerificationCodeService;
import com.taoqi.common.result.R;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code VerificationCodeController} 的单元测试：验证委托与 {@code R} 包装。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ExtendWith(MockitoExtension.class)
class VerificationCodeControllerTest {

    @Mock
    private VerificationCodeService verificationCodeService;

    @InjectMocks
    private VerificationCodeController controller;

    @Test
    @DisplayName("发送验证码并返回统一结果")
    void sendsCodeAndWrapsResult() {
        when(verificationCodeService.send("user@example.com"))
                .thenReturn(EmailSubmitResult.accepted(9L));

        R<EmailSubmitResult> result = controller.send(new VerificationCodeRequest("user@example.com"));

        assertThat(result.success()).isTrue();
        assertThat(result.data().recordId()).isEqualTo(9L);
        verify(verificationCodeService).send("user@example.com");
    }
}