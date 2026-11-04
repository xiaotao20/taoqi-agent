package com.taoqi.chat.mq;

import java.nio.charset.StandardCharsets;

import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import com.taoqi.chat.service.AgentTaskDeadLetterService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code AgentTaskDeadLetterConsumer} 的单元测试：验证消息解析与死信服务委托。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@ExtendWith(MockitoExtension.class)
class AgentTaskDeadLetterConsumerTest {

    @Mock
    private AgentTaskDeadLetterService agentTaskDeadLetterService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AgentTaskDeadLetterConsumer consumer;

    @Test
    @DisplayName("解析出任务主键后交给死信服务")
    void delegatesParsedTaskId() {
        MessageExt message = message();
        when(objectMapper.readValue(any(byte[].class), eq(AgentTaskMessage.class)))
                .thenReturn(new AgentTaskMessage(9L));

        consumer.onMessage(message);

        verify(agentTaskDeadLetterService).record(message, 9L);
    }

    @Test
    @DisplayName("消息体无法解析时以空任务主键留档")
    void recordsUnparseableMessage() {
        MessageExt message = message();
        when(objectMapper.readValue(any(byte[].class), eq(AgentTaskMessage.class)))
                .thenThrow(new IllegalArgumentException("bad json"));

        consumer.onMessage(message);

        verify(agentTaskDeadLetterService).record(message, null);
    }

    private MessageExt message() {
        MessageExt message = new MessageExt();
        message.setMsgId("msg-1");
        message.setBody("{\"taskId\":9}".getBytes(StandardCharsets.UTF_8));
        return message;
    }
}