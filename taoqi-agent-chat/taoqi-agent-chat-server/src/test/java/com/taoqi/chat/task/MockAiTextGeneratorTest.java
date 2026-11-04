package com.taoqi.chat.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taoqi.chat.api.enums.AgentTaskType;
import com.taoqi.chat.config.AgentTaskProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@code MockAiTextGenerator} 的单元测试：验证三种模板与失败模拟开关。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@ExtendWith(MockitoExtension.class)
class MockAiTextGeneratorTest {

    @Mock
    private AgentTaskProperties agentTaskProperties;

    @InjectMocks
    private MockAiTextGenerator generator;

    @Test
    @DisplayName("文本总结结果包含原文摘要")
    void generatesSummary() {
        when(agentTaskProperties.getSimulateDelayMillis()).thenReturn(0L);

        String result = generator.generate(AgentTaskType.SUMMARY, "这是一段需要总结的内容");

        assertThat(result).contains("模拟摘要");
        assertThat(result).contains("这是一段需要总结的内容");
    }

    @Test
    @DisplayName("输入包含「模拟失败」时抛出异常")
    void failsOnKeyword() {
        when(agentTaskProperties.getSimulateDelayMillis()).thenReturn(0L);

        assertThatThrownBy(() -> generator.generate(AgentTaskType.IDEA, "模拟失败"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("模拟");
    }
}