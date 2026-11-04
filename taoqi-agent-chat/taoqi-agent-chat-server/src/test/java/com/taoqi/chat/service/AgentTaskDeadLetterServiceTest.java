package com.taoqi.chat.service;

import java.nio.charset.StandardCharsets;

import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import com.taoqi.chat.api.dto.AgentTaskSubmitRequest;
import com.taoqi.chat.api.entity.AgentTaskDeadLetterEntity;
import com.taoqi.chat.api.entity.AgentTaskEntity;
import com.taoqi.chat.api.enums.AgentTaskStatus;
import com.taoqi.chat.api.enums.AgentTaskType;
import com.taoqi.chat.api.enums.DeadLetterStatus;
import com.taoqi.chat.config.AgentTaskProperties;
import com.taoqi.chat.mapper.AgentTaskDeadLetterMapper;
import com.taoqi.chat.mapper.AgentTaskMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code AgentTaskDeadLetterService} 的单元测试：验证死信落库、任务终态与人工重放。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@ExtendWith(MockitoExtension.class)
class AgentTaskDeadLetterServiceTest {

    @Mock
    private AgentTaskDeadLetterMapper agentTaskDeadLetterMapper;

    @Mock
    private AgentTaskMapper agentTaskMapper;

    @Mock
    private AgentTaskService agentTaskService;

    @Mock
    private AgentTaskProperties agentTaskProperties;

    @InjectMocks
    private AgentTaskDeadLetterService service;

    @Test
    @DisplayName("记录死信并把原任务推进到 DEAD")
    void recordsDeadLetterAndMarksTaskDead() {
        when(agentTaskMapper.selectById(1L)).thenReturn(task(1L, AgentTaskStatus.FAILED));
        when(agentTaskProperties.getConsumerGroup()).thenReturn("taoqi-agent-chat-agent-task-consumer");
        when(agentTaskProperties.getTopic()).thenReturn("taoqi-chat-agent-task");
        when(agentTaskDeadLetterMapper.insert(any(AgentTaskDeadLetterEntity.class))).thenReturn(1);
        when(agentTaskMapper.update(isNull(), any())).thenReturn(1);

        service.record(message("msg-1"), 1L);

        ArgumentCaptor<AgentTaskDeadLetterEntity> captor =
                ArgumentCaptor.forClass(AgentTaskDeadLetterEntity.class);
        verify(agentTaskDeadLetterMapper).insert(captor.capture());
        AgentTaskDeadLetterEntity letter = captor.getValue();
        assertThat(letter.getTaskId()).isEqualTo(1L);
        assertThat(letter.getMsgId()).isEqualTo("msg-1");
        assertThat(letter.getConsumerGroup()).isEqualTo("taoqi-agent-chat-agent-task-consumer");
        assertThat(letter.getTopic()).isEqualTo("taoqi-chat-agent-task");
        assertThat(letter.getPayload()).isEqualTo("{\"taskId\":1}");
        assertThat(letter.getStatus()).isEqualTo(DeadLetterStatus.PENDING);
        verify(agentTaskMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("重复死信消息不重复落库")
    void ignoresDuplicateMessage() {
        when(agentTaskMapper.selectById(1L)).thenReturn(task(1L, AgentTaskStatus.FAILED));
        when(agentTaskProperties.getConsumerGroup()).thenReturn("group");
        when(agentTaskProperties.getTopic()).thenReturn("topic");
        when(agentTaskDeadLetterMapper.insert(any(AgentTaskDeadLetterEntity.class)))
                .thenThrow(new DuplicateKeyException("uk_msg_id"));
        when(agentTaskMapper.update(isNull(), any())).thenReturn(0);

        service.record(message("msg-1"), 1L);

        verify(agentTaskDeadLetterMapper).insert(any(AgentTaskDeadLetterEntity.class));
        verify(agentTaskMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("无法关联任务的死信标记为 IGNORED")
    void marksOrphanDeadLetterIgnored() {
        when(agentTaskMapper.selectById(2L)).thenReturn(null);
        when(agentTaskProperties.getConsumerGroup()).thenReturn("group");
        when(agentTaskProperties.getTopic()).thenReturn("topic");
        when(agentTaskDeadLetterMapper.insert(any(AgentTaskDeadLetterEntity.class))).thenReturn(1);

        service.record(message("msg-2"), 2L);

        ArgumentCaptor<AgentTaskDeadLetterEntity> captor =
                ArgumentCaptor.forClass(AgentTaskDeadLetterEntity.class);
        verify(agentTaskDeadLetterMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(DeadLetterStatus.IGNORED);
        verify(agentTaskMapper, never()).update(isNull(), any());
    }

    @Test
    @DisplayName("重放死信时新建任务并标记已重放")
    void replaysAsNewTask() {
        AgentTaskDeadLetterEntity letter = letter(3L, 9L, DeadLetterStatus.PENDING);
        when(agentTaskDeadLetterMapper.selectById(3L)).thenReturn(letter);
        when(agentTaskDeadLetterMapper.update(isNull(), any())).thenReturn(1, 1);
        AgentTaskEntity original = task(9L, AgentTaskStatus.DEAD);
        when(agentTaskMapper.selectById(9L)).thenReturn(original);
        AgentTaskEntity replayed = task(11L, AgentTaskStatus.PENDING);
        when(agentTaskService.submit(any(AgentTaskSubmitRequest.class))).thenReturn(replayed);

        AgentTaskEntity result = service.replay(3L);

        assertThat(result.getId()).isEqualTo(11L);
        ArgumentCaptor<AgentTaskSubmitRequest> requestCaptor =
                ArgumentCaptor.forClass(AgentTaskSubmitRequest.class);
        verify(agentTaskService).submit(requestCaptor.capture());
        assertThat(requestCaptor.getValue().taskType()).isEqualTo("COPYWRITING");
        assertThat(requestCaptor.getValue().input()).isEqualTo("hello");
        verify(agentTaskDeadLetterMapper, times(2)).update(isNull(), any());
    }

    @Test
    @DisplayName("已处理的死信不能重复重放")
    void rejectsReplayWhenAlreadyHandled() {
        when(agentTaskDeadLetterMapper.selectById(3L))
                .thenReturn(letter(3L, 9L, DeadLetterStatus.REPLAYED));

        assertThatThrownBy(() -> service.replay(3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已处理");
        verify(agentTaskDeadLetterMapper, never()).update(isNull(), any());
    }

    @Test
    @DisplayName("原任务不存在时把死信恢复为待处理")
    void restoresPendingWhenTaskMissing() {
        when(agentTaskDeadLetterMapper.selectById(3L))
                .thenReturn(letter(3L, 9L, DeadLetterStatus.PENDING));
        when(agentTaskDeadLetterMapper.update(isNull(), any())).thenReturn(1, 1);
        when(agentTaskMapper.selectById(9L)).thenReturn(null);

        assertThatThrownBy(() -> service.replay(3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("原任务不存在");
        verify(agentTaskDeadLetterMapper, times(2)).update(isNull(), any());
    }

    private AgentTaskEntity task(Long id, AgentTaskStatus status) {
        AgentTaskEntity task = new AgentTaskEntity();
        task.setId(id);
        task.setTaskType(AgentTaskType.COPYWRITING);
        task.setInputContent("hello");
        task.setStatus(status);
        task.setErrorMessage("模拟失败");
        return task;
    }

    private AgentTaskDeadLetterEntity letter(Long id, Long taskId, DeadLetterStatus status) {
        AgentTaskDeadLetterEntity letter = new AgentTaskDeadLetterEntity();
        letter.setId(id);
        letter.setTaskId(taskId);
        letter.setStatus(status);
        return letter;
    }

    private MessageExt message(String msgId) {
        MessageExt message = new MessageExt();
        message.setMsgId(msgId);
        message.setReconsumeTimes(5);
        message.setBody("{\"taskId\":1}".getBytes(StandardCharsets.UTF_8));
        return message;
    }
}