package com.taoqi.chat.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import jakarta.annotation.Resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.taoqi.chat.api.dto.AgentTaskSubmitRequest;
import com.taoqi.chat.api.entity.AgentTaskDeadLetterEntity;
import com.taoqi.chat.api.entity.AgentTaskEntity;
import com.taoqi.chat.api.enums.AgentTaskStatus;
import com.taoqi.chat.api.enums.DeadLetterStatus;
import com.taoqi.chat.config.AgentTaskProperties;
import com.taoqi.chat.mapper.AgentTaskDeadLetterMapper;
import com.taoqi.chat.mapper.AgentTaskMapper;

/**
 * AI 任务死信服务：记录死信消息、维护任务终态，并支持人工重放。
 *
 * <p>死信落库以消息 ID 唯一约束保证幂等；重放采用“新建任务”的方式，
 * 原任务保留在 {@code DEAD} 终态作为历史，避免旧的死信消息干扰重放后的任务。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Service
public class AgentTaskDeadLetterService {

    private static final Logger log = LoggerFactory.getLogger(AgentTaskDeadLetterService.class);

    private static final int MAX_PAGE_SIZE = 50;

    private static final int ERROR_MESSAGE_LENGTH = 1000;

    @Resource
    private AgentTaskDeadLetterMapper agentTaskDeadLetterMapper;

    @Resource
    private AgentTaskMapper agentTaskMapper;

    @Resource
    private AgentTaskService agentTaskService;

    @Resource
    private AgentTaskProperties agentTaskProperties;

    /**
     * 记录一条死信消息，并把原任务推进到 {@code DEAD} 终态。
     *
     * <p>同一消息重复投递时由唯一索引拦下，只记录一次；原任务已是成功状态时不会降级。
     *
     * @param message 死信消息
     * @param taskId  从消息体解析出的任务主键，解析失败时允许为空
     */
    public void record(MessageExt message, Long taskId) {
        AgentTaskEntity task = taskId == null ? null : agentTaskMapper.selectById(taskId);
        LocalDateTime now = LocalDateTime.now();

        AgentTaskDeadLetterEntity letter = new AgentTaskDeadLetterEntity();
        letter.setTaskId(taskId);
        letter.setMsgId(message.getMsgId());
        letter.setConsumerGroup(agentTaskProperties.getConsumerGroup());
        letter.setTopic(agentTaskProperties.getTopic());
        letter.setPayload(new String(message.getBody(), StandardCharsets.UTF_8));
        letter.setReconsumeTimes(message.getReconsumeTimes());
        letter.setErrorMessage(task == null ? null : truncate(task.getErrorMessage()));
        // 无法关联任务的死信只留档，不能人工重放
        letter.setStatus(task == null ? DeadLetterStatus.IGNORED : DeadLetterStatus.PENDING);
        letter.setCreatedAt(now);
        letter.setUpdatedAt(now);

        try {
            agentTaskDeadLetterMapper.insert(letter);
        }
        catch (DuplicateKeyException e) {
            log.info("死信消息已记录，跳过, msgId={}", message.getMsgId());
        }

        if (task != null) {
            int updated = agentTaskMapper.update(null, Wrappers.<AgentTaskEntity>lambdaUpdate()
                    .eq(AgentTaskEntity::getId, taskId)
                    .ne(AgentTaskEntity::getStatus, AgentTaskStatus.SUCCESS)
                    .set(AgentTaskEntity::getStatus, AgentTaskStatus.DEAD)
                    .set(AgentTaskEntity::getUpdatedAt, now));
            if (updated > 0) {
                log.error("AI 任务重投耗尽进入死信队列, taskId={}, msgId={}, error={}",
                        taskId, message.getMsgId(), task.getErrorMessage());
            }
        }
        else {
            log.error("死信消息无法关联任务, msgId={}, payload={}", message.getMsgId(),
                    new String(message.getBody(), StandardCharsets.UTF_8));
        }
    }

    /**
     * 分页查询死信记录，最新的在前。
     */
    public IPage<AgentTaskDeadLetterEntity> page(long current, long size) {
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return agentTaskDeadLetterMapper.selectPage(new Page<>(safeCurrent, safeSize),
                Wrappers.<AgentTaskDeadLetterEntity>lambdaQuery()
                        .orderByDesc(AgentTaskDeadLetterEntity::getId));
    }

    /**
     * 人工重放：以原任务的类型和输入新建一个任务，并标记死信为已重放。
     *
     * @param deadLetterId 死信记录主键
     * @return 新建的任务
     */
    public AgentTaskEntity replay(Long deadLetterId) {
        AgentTaskDeadLetterEntity letter = agentTaskDeadLetterMapper.selectById(deadLetterId);
        if (letter == null) {
            throw new IllegalArgumentException("死信记录不存在");
        }
        if (letter.getStatus() != DeadLetterStatus.PENDING) {
            throw new IllegalStateException("该死信已处理或正在处理");
        }

        LocalDateTime now = LocalDateTime.now();
        int claimed = agentTaskDeadLetterMapper.update(null,
                Wrappers.<AgentTaskDeadLetterEntity>lambdaUpdate()
                        .eq(AgentTaskDeadLetterEntity::getId, deadLetterId)
                        .eq(AgentTaskDeadLetterEntity::getStatus, DeadLetterStatus.PENDING)
                        .set(AgentTaskDeadLetterEntity::getStatus, DeadLetterStatus.REPLAYING)
                        .set(AgentTaskDeadLetterEntity::getUpdatedAt, now));
        if (claimed == 0) {
            throw new IllegalStateException("该死信已被其它请求处理");
        }

        AgentTaskEntity original = letter.getTaskId() == null
                ? null : agentTaskMapper.selectById(letter.getTaskId());
        if (original == null) {
            restorePending(deadLetterId);
            throw new IllegalStateException("原任务不存在，无法重放");
        }

        try {
            AgentTaskEntity replayed = agentTaskService.submit(
                    new AgentTaskSubmitRequest(original.getTaskType().name(), original.getInputContent()));
            LocalDateTime replayedAt = LocalDateTime.now();
            agentTaskDeadLetterMapper.update(null,
                    Wrappers.<AgentTaskDeadLetterEntity>lambdaUpdate()
                            .eq(AgentTaskDeadLetterEntity::getId, deadLetterId)
                            .set(AgentTaskDeadLetterEntity::getStatus, DeadLetterStatus.REPLAYED)
                            .set(AgentTaskDeadLetterEntity::getReplayedTaskId, replayed.getId())
                            .set(AgentTaskDeadLetterEntity::getReplayedAt, replayedAt)
                            .set(AgentTaskDeadLetterEntity::getUpdatedAt, replayedAt));
            log.info("死信任务已重放, deadLetterId={}, oldTaskId={}, newTaskId={}",
                    deadLetterId, letter.getTaskId(), replayed.getId());
            return replayed;
        }
        catch (RuntimeException e) {
            restorePending(deadLetterId);
            throw e;
        }
    }

    private void restorePending(Long deadLetterId) {
        agentTaskDeadLetterMapper.update(null, Wrappers.<AgentTaskDeadLetterEntity>lambdaUpdate()
                .eq(AgentTaskDeadLetterEntity::getId, deadLetterId)
                .set(AgentTaskDeadLetterEntity::getStatus, DeadLetterStatus.PENDING)
                .set(AgentTaskDeadLetterEntity::getUpdatedAt, LocalDateTime.now()));
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= ERROR_MESSAGE_LENGTH ? value : value.substring(0, ERROR_MESSAGE_LENGTH);
    }
}