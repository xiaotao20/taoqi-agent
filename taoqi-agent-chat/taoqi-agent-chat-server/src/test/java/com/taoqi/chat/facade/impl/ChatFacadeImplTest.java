package com.taoqi.chat.facade.impl;

import com.taoqi.chat.config.ChatProperties;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSendResult;
import com.taoqi.integration.client.EmailFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code ChatFacadeImpl} 的单元测试：验证默认收件人与指定收件人逻辑。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
class ChatFacadeImplTest {

    private final EmailFeignClient emailFeignClient = mock(EmailFeignClient.class);
    private final ChatFacadeImpl chatFacade = new ChatFacadeImpl(
            emailFeignClient, new ChatProperties("default@qq.com"));

    @Test
    @DisplayName("未传 to 时使用配置的默认收件人")
    void usesConfiguredRecipientWhenToMissing() {
        when(emailFeignClient.send(any())).thenReturn(EmailSendResult.ok("message-id"));

        EmailSendResult result = chatFacade.sendTestEmail(null);

        assertThat(result.success()).isTrue();
        assertThat(captureRequest().to()).containsExactly("default@qq.com");
    }

    @Test
    @DisplayName("传入 to 时使用指定收件人")
    void usesRequestedRecipient() {
        when(emailFeignClient.send(any())).thenReturn(EmailSendResult.ok("message-id"));

        chatFacade.sendTestEmail("other@qq.com");

        assertThat(captureRequest().to()).containsExactly("other@qq.com");
    }

    private EmailSendRequest captureRequest() {
        ArgumentCaptor<EmailSendRequest> captor = ArgumentCaptor.forClass(EmailSendRequest.class);
        verify(emailFeignClient).send(captor.capture());
        return captor.getValue();
    }
}
