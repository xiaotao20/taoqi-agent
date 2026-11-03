package com.taoqi.integration.event;

import java.util.List;

import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.task.EmailSendTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * {@code EmailSendListener} 的单元测试：验证事件被转交给执行组件。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ExtendWith(MockitoExtension.class)
class EmailSendListenerTest {

    @Mock
    private EmailSendTask emailSendTask;

    @InjectMocks
    private EmailSendListener emailSendListener;

    @Test
    @DisplayName("监听器把事件中的记录 ID 与请求转交给发送任务")
    void delegatesEventToTask() {
        EmailSendRequest request = new EmailSendRequest(
                List.of("receiver@example.com"), null, null, "主题", "内容", false);

        emailSendListener.onEmailSend(new EmailSendEvent(5L, request));

        verify(emailSendTask).send(5L, request);
    }
}