package com.taoqi.chat.mq;

/**
 * AI 任务消息体：只携带任务主键，消费端按主键读库，避免大文本进入消息队列。
 *
 * @param taskId 任务主键
 * @author xiaotao
 * @date 2026-11-04
 */
public record AgentTaskMessage(Long taskId) {
}