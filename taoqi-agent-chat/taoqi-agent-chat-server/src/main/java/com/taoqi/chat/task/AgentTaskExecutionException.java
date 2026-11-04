package com.taoqi.chat.task;

/**
 * AI 任务执行异常：消费者抛出后由 RocketMQ 触发重投。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
public class AgentTaskExecutionException extends RuntimeException {

    public AgentTaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}