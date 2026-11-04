package com.taoqi.chat.service;

import java.time.LocalDateTime;

import jakarta.annotation.Resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.taoqi.chat.api.dto.AgentTaskSubmitRequest;
import com.taoqi.chat.api.entity.AgentTaskEntity;
import com.taoqi.chat.api.enums.AgentTaskStatus;
import com.taoqi.chat.api.enums.AgentTaskType;
import com.taoqi.chat.mapper.AgentTaskMapper;
import com.taoqi.chat.mq.AgentTaskDispatcher;

/**
 * AI 任务服务：负责任务落库、派发与查询。
 *
 * <p>提交时先写一条 {@code PENDING} 记录再发消息，消息投递失败则把记录置为 {@code FAILED}，
 * 避免任务停留在永远没人消费的排队状态。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Service
public class AgentTaskService {

    private static final Logger log = LoggerFactory.getLogger(AgentTaskService.class);

    private static final int MAX_PAGE_SIZE = 50;

    @Resource
    private AgentTaskMapper agentTaskMapper;

    @Resource
    private AgentTaskDispatcher agentTaskDispatcher;

    /**
     * 提交任务：落库后派发给 MQ（或本地线程池）。
     *
     * @param request 提交请求
     * @return 已创建的任务，派发失败时状态为 {@code FAILED}
     */
    public AgentTaskEntity submit(AgentTaskSubmitRequest request) {
        LocalDateTime now = LocalDateTime.now();
        AgentTaskEntity task = new AgentTaskEntity();
        task.setTaskType(resolveType(request.taskType()));
        task.setInputContent(request.input().trim());
        task.setStatus(AgentTaskStatus.PENDING);
        task.setRetryCount(0);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        agentTaskMapper.insert(task);

        try {
            agentTaskDispatcher.dispatch(task.getId());
        }
        catch (RuntimeException e) {
            log.error("AI 任务派发失败, taskId={}", task.getId(), e);
            markDispatchFailed(task.getId());
            task.setStatus(AgentTaskStatus.FAILED);
        }
        log.info("AI 任务已提交, taskId={}, type={}, status={}",
                task.getId(), task.getTaskType(), task.getStatus());
        return task;
    }

    /**
     * 按主键查询任务。
     *
     * @param id 任务主键
     * @return 任务，不存在时返回 null
     */
    public AgentTaskEntity find(Long id) {
        return agentTaskMapper.selectById(id);
    }

    /**
     * 分页查询任务，按主键倒序，最新的在前。
     *
     * @param current 页码，从 1 开始
     * @param size    每页条数，最大 50
     * @return 分页结果
     */
    public IPage<AgentTaskEntity> page(long current, long size) {
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return agentTaskMapper.selectPage(new Page<>(safeCurrent, safeSize),
                Wrappers.<AgentTaskEntity>lambdaQuery().orderByDesc(AgentTaskEntity::getId));
    }

    /**
     * 派发失败时把任务置为失败，避免停在 PENDING。
     */
    private void markDispatchFailed(Long taskId) {
        LocalDateTime now = LocalDateTime.now();
        agentTaskMapper.update(null, Wrappers.<AgentTaskEntity>lambdaUpdate()
                .eq(AgentTaskEntity::getId, taskId)
                .set(AgentTaskEntity::getStatus, AgentTaskStatus.FAILED)
                .set(AgentTaskEntity::getErrorMessage, "AI 任务消息投递失败")
                .set(AgentTaskEntity::getFinishedAt, now)
                .set(AgentTaskEntity::getUpdatedAt, now));
    }

    private AgentTaskType resolveType(String taskType) {
        return StringUtils.hasText(taskType) ? AgentTaskType.valueOf(taskType) : AgentTaskType.COPYWRITING;
    }
}