package com.taoqi.chat.task;

import java.time.LocalDateTime;

import jakarta.annotation.Resource;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.taoqi.chat.api.entity.AgentTaskEntity;
import com.taoqi.chat.api.enums.AgentTaskStatus;
import com.taoqi.chat.config.AgentTaskProperties;
import com.taoqi.chat.mapper.AgentTaskMapper;

/**
 * AI 任务执行组件：按主键推进任务状态，调用生成器生成结果并写回。
 *
 * <p>状态流转通过条件更新完成，重复消费或并发消费时只有一次能把任务从
 * {@code PENDING/FAILED} 推进到 {@code RUNNING}，其余直接跳过，保证幂等。
 * 执行异常时先落 {@code FAILED} 并累加失败次数，再把异常抛给 MQ 触发重投。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Component
public class AgentTaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(AgentTaskExecutor.class);

    private static final int ERROR_MESSAGE_LENGTH = 1000;

    @Resource
    private AgentTaskMapper agentTaskMapper;

    @Resource
    private MockAiTextGenerator generator;

    /**
     * 执行任务。
     *
     * @param taskId 任务主键
     */
    public void execute(Long taskId) {
        AgentTaskEntity task = agentTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("AI 任务不存在，跳过, taskId={}", taskId);
            return;
        }

        int started = agentTaskMapper.update(null, startWrapper(taskId));
        if (started == 0) {
            log.info("AI 任务已被处理或正在处理，跳过, taskId={}, status={}", taskId, task.getStatus());
            return;
        }

        long startMillis = System.currentTimeMillis();
        try {
            String result = generator.generate(task.getTaskType(), task.getInputContent());
            LocalDateTime finishedAt = LocalDateTime.now();
            agentTaskMapper.update(null, successWrapper(taskId, result, finishedAt));
            log.info("AI 任务执行成功, taskId={}, type={}, cost={}ms",
                    taskId, task.getTaskType(), System.currentTimeMillis() - startMillis);
        }
        catch (RuntimeException e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            LocalDateTime finishedAt = LocalDateTime.now();
            agentTaskMapper.update(null, failureWrapper(taskId, reason, finishedAt));
            log.error("AI 任务执行失败, taskId={}, cost={}ms", taskId, System.currentTimeMillis() - startMillis, e);
            throw new AgentTaskExecutionException("AI 任务执行失败, taskId=" + taskId, e);
        }
    }

    private LambdaUpdateWrapper<AgentTaskEntity> startWrapper(Long taskId) {
        return Wrappers.<AgentTaskEntity>lambdaUpdate()
                .eq(AgentTaskEntity::getId, taskId)
                .in(AgentTaskEntity::getStatus, AgentTaskStatus.PENDING, AgentTaskStatus.FAILED)
                .set(AgentTaskEntity::getStatus, AgentTaskStatus.RUNNING)
                .set(AgentTaskEntity::getUpdatedAt, LocalDateTime.now());
    }

    private LambdaUpdateWrapper<AgentTaskEntity> successWrapper(Long taskId, String result, LocalDateTime finishedAt) {
        return Wrappers.<AgentTaskEntity>lambdaUpdate()
                .eq(AgentTaskEntity::getId, taskId)
                .eq(AgentTaskEntity::getStatus, AgentTaskStatus.RUNNING)
                .set(AgentTaskEntity::getStatus, AgentTaskStatus.SUCCESS)
                .set(AgentTaskEntity::getResult, result)
                .set(AgentTaskEntity::getErrorMessage, null)
                .set(AgentTaskEntity::getFinishedAt, finishedAt)
                .set(AgentTaskEntity::getUpdatedAt, finishedAt);
    }

    private LambdaUpdateWrapper<AgentTaskEntity> failureWrapper(Long taskId, String reason, LocalDateTime finishedAt) {
        String message = reason.length() <= ERROR_MESSAGE_LENGTH
                ? reason : reason.substring(0, ERROR_MESSAGE_LENGTH);
        return Wrappers.<AgentTaskEntity>lambdaUpdate()
                .eq(AgentTaskEntity::getId, taskId)
                .eq(AgentTaskEntity::getStatus, AgentTaskStatus.RUNNING)
                .set(AgentTaskEntity::getStatus, AgentTaskStatus.FAILED)
                .set(AgentTaskEntity::getErrorMessage, message)
                .set(AgentTaskEntity::getFinishedAt, finishedAt)
                .set(AgentTaskEntity::getUpdatedAt, finishedAt)
                .setSql("retry_count = retry_count + 1");
    }
}