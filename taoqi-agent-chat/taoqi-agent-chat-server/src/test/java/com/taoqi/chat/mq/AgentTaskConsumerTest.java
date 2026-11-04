package com.taoqi.chat.mq;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import tools.jackson.databind.ObjectMapper;

import com.taoqi.chat.task.AgentTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * {@code AgentTaskConsumer} 的单元测试：验证反序列化、traceId 回填与异常向上抛出。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@ExtendWith(MockitoExtension.class)
class AgentTaskConsumerTest {

    @Mock
    private AgentTaskExecutor agentTaskExecutor;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AgentTaskConsumer consumer;

    @Test
    @DisplayName("消费消息后调用执行器，执行期间 MDC 带 traceId，结束后清理")
    void consumesAndRestoresTraceId() {
        MessageExt message = message("trace-1");
        when(objectMapper.readValue(any(byte[].class), eq(AgentTaskMessage.class)))
                .thenReturn(new AgentTaskMessage(9L));
        AtomicReference<String> traceInExecution = new AtomicReference<>();
        doAnswer(invocation -> {
            traceInExecution.set(MDC.get("traceId"));
            return null;
        }).when(agentTaskExecutor).execute(9L);

        consumer.onMessage(message);

        assertThat(traceInExecution.get()).isEqualTo("trace-1");
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    @DisplayName("执行失败时把异常抛给 starter 触发重投")
    void propagatesFailure() {
        MessageExt message = message("trace-2");
        when(objectMapper.readValue(any(byte[].class), eq(AgentTaskMessage.class)))
                .thenReturn(new AgentTaskMessage(9L));
        doThrow(new IllegalStateException("模拟失败")).when(agentTaskExecutor).execute(9L);

        assertThatThrownBy(() -> consumer.onMessage(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("模拟失败");
    }

    private MessageExt message(String traceId) {
        MessageExt message = new MessageExt();
        message.setBody("{\"taskId\":9}".getBytes(StandardCharsets.UTF_8));
        message.putUserProperty("traceId", traceId);
        return message;
    }
}