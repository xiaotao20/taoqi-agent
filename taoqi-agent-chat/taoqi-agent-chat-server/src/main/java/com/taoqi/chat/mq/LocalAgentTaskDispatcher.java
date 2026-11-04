package com.taoqi.chat.mq;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import com.taoqi.chat.task.AgentTaskExecutor;

/**
 * 本机线程池派发器：本地没有 RocketMQ 时使用，行为与 MQ 模式一致，只是消息不落盘。
 *
 * <p>仅用于单实例开发调试，进程重启会丢失在途任务。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Component
@ConditionalOnProperty(prefix = "taoqi.chat.agent-task", name = "dispatch-mode", havingValue = "local")
public class LocalAgentTaskDispatcher implements AgentTaskDispatcher {

    private static final Logger log = LoggerFactory.getLogger(LocalAgentTaskDispatcher.class);

    @Resource(name = "agentTaskLocalExecutor")
    private AsyncTaskExecutor executor;

    @Resource
    private AgentTaskExecutor agentTaskExecutor;

    @Override
    public void dispatch(Long taskId) {
        executor.execute(() -> {
            try {
                agentTaskExecutor.execute(taskId);
            }
            catch (RuntimeException e) {
                log.error("本地执行 AI 任务失败, taskId={}", taskId, e);
            }
        });
    }
}