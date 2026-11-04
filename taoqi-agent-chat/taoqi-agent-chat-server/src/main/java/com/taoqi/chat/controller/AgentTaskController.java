package com.taoqi.chat.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taoqi.chat.api.dto.AgentTaskSubmitRequest;
import com.taoqi.chat.api.entity.AgentTaskDeadLetterEntity;
import com.taoqi.chat.api.entity.AgentTaskEntity;
import com.taoqi.chat.api.enums.AgentTaskStatus;
import com.taoqi.chat.service.AgentTaskDeadLetterService;
import com.taoqi.chat.service.AgentTaskService;
import com.taoqi.common.annotation.RateLimit;
import com.taoqi.common.result.R;

/**
 * AI 任务接口：提交任务、查询任务状态与分页列表。
 *
 * <p>提交接口立即返回，真正的生成由 RocketMQ 消费者异步完成，调用方通过查询接口轮询状态。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@RestController
@RequestMapping("/api/chat/agent-task")
public class AgentTaskController {

    private static final int NOT_FOUND_CODE = 404;

    private static final int CONFLICT_CODE = 409;

    private static final int DISPATCH_FAILED_CODE = 503;

    @Resource
    private AgentTaskService agentTaskService;

    @Resource
    private AgentTaskDeadLetterService agentTaskDeadLetterService;

    /**
     * 提交 AI 任务，每分钟最多 10 次。
     */
    @RateLimit(limit = 10, windowSeconds = 60)
    @PostMapping
    public R<AgentTaskEntity> submit(@Valid @RequestBody AgentTaskSubmitRequest request) {
        AgentTaskEntity task = agentTaskService.submit(request);
        if (task.getStatus() == AgentTaskStatus.FAILED) {
            return R.fail(DISPATCH_FAILED_CODE, "任务已创建，但消息投递失败，请检查 RocketMQ 是否可用");
        }
        return R.data(task);
    }

    /**
     * 查询单个任务。
     */
    @GetMapping("/{id}")
    public R<AgentTaskEntity> get(@PathVariable("id") Long id) {
        AgentTaskEntity task = agentTaskService.find(id);
        if (task == null) {
            return R.fail(NOT_FOUND_CODE, "任务不存在");
        }
        return R.data(task);
    }

    /**
     * 分页查询任务列表，最新提交的在前。
     */
    @GetMapping
    public R<IPage<AgentTaskEntity>> page(@RequestParam(name = "current", defaultValue = "1") long current,
            @RequestParam(name = "size", defaultValue = "10") long size) {
        return R.data(agentTaskService.page(current, size));
    }

    /**
     * 分页查询死信记录，最新进入死信的在前。
     */
    @GetMapping("/dead-letter")
    public R<IPage<AgentTaskDeadLetterEntity>> deadLetters(
            @RequestParam(name = "current", defaultValue = "1") long current,
            @RequestParam(name = "size", defaultValue = "10") long size) {
        return R.data(agentTaskDeadLetterService.page(current, size));
    }

    /**
     * 人工重放死信：以原任务类型和输入新建任务并重新入队，每分钟最多 5 次。
     */
    @RateLimit(limit = 5, windowSeconds = 60)
    @PostMapping("/dead-letter/{id}/replay")
    public R<AgentTaskEntity> replay(@PathVariable("id") Long id) {
        try {
            return R.data(agentTaskDeadLetterService.replay(id));
        }
        catch (IllegalArgumentException e) {
            return R.fail(NOT_FOUND_CODE, e.getMessage());
        }
        catch (IllegalStateException e) {
            return R.fail(CONFLICT_CODE, e.getMessage());
        }
    }
}