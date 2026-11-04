package com.taoqi.chat.mq;

/**
 * AI 任务派发器：把已落库的任务交给执行方。
 *
 * <p>{@code mq} 模式发消息给 RocketMQ，{@code local} 模式提交到本机线程池；
 * 由 {@code taoqi.chat.agent-task.dispatch-mode} 决定装配哪个实现。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
public interface AgentTaskDispatcher {

    /**
     * 派发任务。
     *
     * @param taskId 任务主键
     */
    void dispatch(Long taskId);
}