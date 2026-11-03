package com.taoqi.chat.controller;

import com.taoqi.chat.facade.ChatFacade;
import com.taoqi.integration.api.dto.EmailSendResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code ChatController} 的单元测试：验证请求被委托给 facade。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
class ChatControllerTest {

    private final ChatFacade chatFacade = mock(ChatFacade.class);
    private final ChatController controller = new ChatController(chatFacade);

    @Test
    @DisplayName("controller 将请求委托给 facade")
    void delegatesToFacade() {
        when(chatFacade.sendTestEmail("someone@qq.com")).thenReturn(EmailSendResult.ok("message-id"));

        EmailSendResult result = controller.sendTestEmail("someone@qq.com");

        assertThat(result.success()).isTrue();
        verify(chatFacade).sendTestEmail("someone@qq.com");
    }
}
