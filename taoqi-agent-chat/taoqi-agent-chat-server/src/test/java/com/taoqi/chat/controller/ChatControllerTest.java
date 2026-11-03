package com.taoqi.chat.controller;

import com.taoqi.chat.facade.EmailClientFacade;
import com.taoqi.common.result.R;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import com.taoqi.integration.api.enums.EmailSendStatus;
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
 * {@code ChatController} 的单元测试：验证请求被委托给 facade，并以 {@code R} 包装返回。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private EmailClientFacade emailClientFacade;

    @InjectMocks
    private ChatController controller;

    @Test
    @DisplayName("controller 将请求委托给 facade")
    void delegatesToFacade() {
        when(emailClientFacade.sendTestEmail("someone@qq.com"))
                .thenReturn(EmailSubmitResult.accepted(1L));

        R<EmailSubmitResult> result = controller.sendTestEmail("someone@qq.com");

        assertThat(result.success()).isTrue();
        assertThat(result.data().recordId()).isEqualTo(1L);
        assertThat(result.data().status()).isEqualTo(EmailSendStatus.SENDING);
        verify(emailClientFacade).sendTestEmail("someone@qq.com");
    }

    @Test
    @DisplayName("ping 返回统一结果结构")
    void pingReturnsWrappedResult() {
        var result = controller.ping();

        assertThat(result.success()).isTrue();
        assertThat(result.code()).isEqualTo(200);
        assertThat(result.data()).isEqualTo("pong");
    }
}