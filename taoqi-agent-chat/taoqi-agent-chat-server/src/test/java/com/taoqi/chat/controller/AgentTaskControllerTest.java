package com.taoqi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taoqi.chat.api.dto.AgentTaskSubmitRequest;
import com.taoqi.chat.api.entity.AgentTaskEntity;
import com.taoqi.chat.api.enums.AgentTaskStatus;
import com.taoqi.chat.service.AgentTaskDeadLetterService;
import com.taoqi.chat.service.AgentTaskService;
import com.taoqi.common.result.R;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code AgentTaskController} 的单元测试：验证委托与 {@code R} 包装。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@ExtendWith(MockitoExtension.class)
class AgentTaskControllerTest {

    @Mock
    private AgentTaskService agentTaskService;

    @Mock
    private AgentTaskDeadLetterService agentTaskDeadLetterService;

    @InjectMocks
    private AgentTaskController controller;

    @Test
    @DisplayName("提交成功时返回任务数据")
    void submitsAndWrapsResult() {
        AgentTaskEntity task = task(AgentTaskStatus.PENDING);
        when(agentTaskService.submit(new AgentTaskSubmitRequest("SUMMARY", "hello"))).thenReturn(task);

        R<AgentTaskEntity> result = controller.submit(new AgentTaskSubmitRequest("SUMMARY", "hello"));

        assertThat(result.success()).isTrue();
        assertThat(result.data().getId()).isEqualTo(1L);
        verify(agentTaskService).submit(new AgentTaskSubmitRequest("SUMMARY", "hello"));
    }

    @Test
    @DisplayName("消息投递失败时返回业务码 503")
    void reportsDispatchFailure() {
        when(agentTaskService.submit(new AgentTaskSubmitRequest("SUMMARY", "hello")))
                .thenReturn(task(AgentTaskStatus.FAILED));

        R<AgentTaskEntity> result = controller.submit(new AgentTaskSubmitRequest("SUMMARY", "hello"));

        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo(503);
    }

    @Test
    @DisplayName("任务不存在时返回业务码 404")
    void reportsNotFound() {
        when(agentTaskService.find(9L)).thenReturn(null);

        R<AgentTaskEntity> result = controller.get(9L);

        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo(404);
        assertThat(result.message()).contains("任务不存在");
    }

    @Test
    @DisplayName("分页查询返回统一结果")
    void pagesTasks() {
        when(agentTaskService.page(1, 10)).thenReturn(new Page<>(1, 10));

        R<?> result = controller.page(1, 10);

        assertThat(result.success()).isTrue();
        verify(agentTaskService).page(1, 10);
    }

    @Test
    @DisplayName("死信分页查询返回统一结果")
    void pagesDeadLetters() {
        when(agentTaskDeadLetterService.page(1, 5)).thenReturn(new Page<>(1, 5));

        R<?> result = controller.deadLetters(1, 5);

        assertThat(result.success()).isTrue();
        verify(agentTaskDeadLetterService).page(1, 5);
    }

    @Test
    @DisplayName("重放死信后返回新任务")
    void replaysDeadLetter() {
        AgentTaskEntity replayed = task(AgentTaskStatus.PENDING);
        when(agentTaskDeadLetterService.replay(3L)).thenReturn(replayed);

        R<AgentTaskEntity> result = controller.replay(3L);

        assertThat(result.success()).isTrue();
        assertThat(result.data().getId()).isEqualTo(1L);
        verify(agentTaskDeadLetterService).replay(3L);
    }

    @Test
    @DisplayName("死信已被处理时返回业务码 409")
    void reportsReplayConflict() {
        when(agentTaskDeadLetterService.replay(3L))
                .thenThrow(new IllegalStateException("该死信已处理或正在处理"));

        R<AgentTaskEntity> result = controller.replay(3L);

        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo(409);
    }

    @Test
    @DisplayName("死信不存在时返回业务码 404")
    void reportsDeadLetterNotFound() {
        when(agentTaskDeadLetterService.replay(3L))
                .thenThrow(new IllegalArgumentException("死信记录不存在"));

        R<AgentTaskEntity> result = controller.replay(3L);

        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo(404);
    }

    private AgentTaskEntity task(AgentTaskStatus status) {
        AgentTaskEntity task = new AgentTaskEntity();
        task.setId(1L);
        task.setStatus(status);
        return task;
    }
}