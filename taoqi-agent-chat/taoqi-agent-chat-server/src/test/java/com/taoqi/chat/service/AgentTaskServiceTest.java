package com.taoqi.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taoqi.chat.api.dto.AgentTaskSubmitRequest;
import com.taoqi.chat.api.entity.AgentTaskEntity;
import com.taoqi.chat.api.enums.AgentTaskStatus;
import com.taoqi.chat.api.enums.AgentTaskType;
import com.taoqi.chat.mapper.AgentTaskMapper;
import com.taoqi.chat.mq.AgentTaskDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code AgentTaskService} 的单元测试：验证落库、派发与分页参数处理。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@ExtendWith(MockitoExtension.class)
class AgentTaskServiceTest {

    @Mock
    private AgentTaskMapper agentTaskMapper;

    @Mock
    private AgentTaskDispatcher agentTaskDispatcher;

    @InjectMocks
    private AgentTaskService agentTaskService;

    @Test
    @DisplayName("提交任务时落库 PENDING 记录并派发主键")
    void submitsAndDispatches() {
        stubInsertedId(7L);

        AgentTaskEntity task = agentTaskService.submit(new AgentTaskSubmitRequest("SUMMARY", "  一段需要总结的文本  "));

        assertThat(task.getId()).isEqualTo(7L);
        assertThat(task.getStatus()).isEqualTo(AgentTaskStatus.PENDING);
        assertThat(task.getTaskType()).isEqualTo(AgentTaskType.SUMMARY);
        assertThat(task.getInputContent()).isEqualTo("一段需要总结的文本");
        assertThat(task.getRetryCount()).isZero();
        verify(agentTaskDispatcher).dispatch(7L);
    }

    @Test
    @DisplayName("未指定任务类型时默认文案生成")
    void defaultsToCopywriting() {
        stubInsertedId(8L);

        AgentTaskEntity task = agentTaskService.submit(new AgentTaskSubmitRequest(null, "hello"));

        assertThat(task.getTaskType()).isEqualTo(AgentTaskType.COPYWRITING);
    }

    @Test
    @DisplayName("消息投递失败时把任务置为 FAILED")
    void marksFailedWhenDispatchFails() {
        stubInsertedId(9L);
        doThrow(new IllegalStateException("broker down")).when(agentTaskDispatcher).dispatch(9L);

        AgentTaskEntity task = agentTaskService.submit(new AgentTaskSubmitRequest("IDEA", "hello"));

        assertThat(task.getStatus()).isEqualTo(AgentTaskStatus.FAILED);
        verify(agentTaskMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("分页参数越界时收敛到合法范围")
    void clampsPageParameters() {
        when(agentTaskMapper.selectPage(any(), any())).thenReturn(new Page<>());

        agentTaskService.page(0, 999);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Page<AgentTaskEntity>> captor = ArgumentCaptor.forClass(Page.class);
        verify(agentTaskMapper).selectPage(captor.capture(), any());
        assertThat(captor.getValue().getCurrent()).isEqualTo(1);
        assertThat(captor.getValue().getSize()).isEqualTo(50);
    }

    private void stubInsertedId(Long id) {
        when(agentTaskMapper.insert(any(AgentTaskEntity.class))).thenAnswer(invocation -> {
            AgentTaskEntity entity = invocation.getArgument(0);
            entity.setId(id);
            return 1;
        });
    }
}