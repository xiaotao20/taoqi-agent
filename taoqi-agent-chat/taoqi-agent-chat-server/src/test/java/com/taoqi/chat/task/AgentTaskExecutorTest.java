package com.taoqi.chat.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taoqi.chat.api.entity.AgentTaskEntity;
import com.taoqi.chat.api.enums.AgentTaskStatus;
import com.taoqi.chat.api.enums.AgentTaskType;
import com.taoqi.chat.mapper.AgentTaskMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@code AgentTaskExecutor} 的单元测试：验证状态机、幂等跳过与失败重投。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@ExtendWith(MockitoExtension.class)
class AgentTaskExecutorTest {

    @Mock
    private AgentTaskMapper agentTaskMapper;

    @Mock
    private MockAiTextGenerator generator;

    @InjectMocks
    private AgentTaskExecutor agentTaskExecutor;

    @Test
    @DisplayName("正常执行时从 PENDING 推进到 SUCCESS")
    void executesAndMarksSuccess() {
        when(agentTaskMapper.selectById(1L)).thenReturn(task(AgentTaskStatus.PENDING));
        when(agentTaskMapper.update(isNull(), any())).thenReturn(1, 1);
        when(generator.generate(AgentTaskType.COPYWRITING, "hello")).thenReturn("done");

        agentTaskExecutor.execute(1L);

        verify(agentTaskMapper, times(2)).update(isNull(), any());
        verify(generator).generate(AgentTaskType.COPYWRITING, "hello");
    }

    @Test
    @DisplayName("任务已被其它消费者推进时跳过执行")
    void skipsWhenAlreadyHandled() {
        when(agentTaskMapper.selectById(1L)).thenReturn(task(AgentTaskStatus.SUCCESS));
        when(agentTaskMapper.update(isNull(), any())).thenReturn(0);

        agentTaskExecutor.execute(1L);

        verify(generator, never()).generate(any(), any());
    }

    @Test
    @DisplayName("任务不存在时直接跳过")
    void skipsWhenTaskMissing() {
        when(agentTaskMapper.selectById(1L)).thenReturn(null);

        agentTaskExecutor.execute(1L);

        verifyNoInteractions(generator);
        verify(agentTaskMapper, never()).update(isNull(), any());
    }

    @Test
    @DisplayName("执行失败时落 FAILED 并抛出异常触发 MQ 重投")
    void marksFailedAndRethrows() {
        when(agentTaskMapper.selectById(1L)).thenReturn(task(AgentTaskStatus.PENDING));
        when(agentTaskMapper.update(isNull(), any())).thenReturn(1, 1);
        when(generator.generate(AgentTaskType.COPYWRITING, "hello"))
                .thenThrow(new IllegalStateException("模型调用失败"));

        assertThatThrownBy(() -> agentTaskExecutor.execute(1L))
                .isInstanceOf(AgentTaskExecutionException.class)
                .hasMessageContaining("taskId=1");

        verify(agentTaskMapper, times(2)).update(isNull(), any());
    }

    private AgentTaskEntity task(AgentTaskStatus status) {
        AgentTaskEntity task = new AgentTaskEntity();
        task.setId(1L);
        task.setTaskType(AgentTaskType.COPYWRITING);
        task.setInputContent("hello");
        task.setStatus(status);
        task.setRetryCount(0);
        return task;
    }
}