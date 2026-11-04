package com.taoqi.chat.mq;

import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.taoqi.chat.task.AgentTaskExecutor;

/**
 * AI 任务消费者：通过 {@code @RocketMQMessageListener} 声明消费组与主题。
 *
 * <p>消费失败时把异常抛给 starter，由 RocketMQ 按 {@code maxReconsumeTimes} 重投，
 * 超限后进入 {@code %DLQ%} 死信队列。traceId 从消息用户属性写回 MDC，保证异步日志可串联。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Component
@ConditionalOnProperty(prefix = "taoqi.chat.agent-task", name = "dispatch-mode",
        havingValue = "mq", matchIfMissing = true)
@RocketMQMessageListener(
        topic = "${taoqi.chat.agent-task.topic:taoqi-chat-agent-task}",
        consumerGroup = "${taoqi.chat.agent-task.consumer-group:taoqi-agent-chat-agent-task-consumer}",
        // 注解属性不支持占位符，重投次数写常量；超过后进入死信队列
        maxReconsumeTimes = 5,
        consumeThreadNumber = 2,
        consumeThreadMax = 4)
public class AgentTaskConsumer implements RocketMQListener<MessageExt> {

    private static final Logger log = LoggerFactory.getLogger(AgentTaskConsumer.class);

    private static final String TRACE_ID_PROPERTY = "traceId";

    @Resource
    private AgentTaskExecutor agentTaskExecutor;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(MessageExt message) {
        String traceId = message.getUserProperty(TRACE_ID_PROPERTY);
        boolean traceIdApplied = StringUtils.hasText(traceId);
        if (traceIdApplied) {
            MDC.put(TRACE_ID_PROPERTY, traceId);
        }
        try {
            AgentTaskMessage payload = objectMapper.readValue(message.getBody(), AgentTaskMessage.class);
            agentTaskExecutor.execute(payload.taskId());
        }
        catch (RuntimeException e) {
            log.error("AI 任务消费失败，等待 MQ 重投, msgId={}, reconsumeTimes={}",
                    message.getMsgId(), message.getReconsumeTimes(), e);
            throw e;
        }
        finally {
            if (traceIdApplied) {
                MDC.remove(TRACE_ID_PROPERTY);
            }
        }
    }
}