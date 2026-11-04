package com.taoqi.chat.mq;

import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.taoqi.chat.service.AgentTaskDeadLetterService;

/**
 * 死信消费者：订阅原消费组的 {@code %DLQ%} 主题，把死信落库并标记任务终态。
 *
 * <p>落库使用消息 ID 唯一约束保证幂等；消息体无法解析时也会留档，状态置为 {@code IGNORED}。
 * 死信消费者自身失败会按 {@code maxReconsumeTimes} 重投，避免直接丢弃。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Component
@ConditionalOnProperty(prefix = "taoqi.chat.agent-task", name = "dispatch-mode",
        havingValue = "mq", matchIfMissing = true)
@RocketMQMessageListener(
        topic = "${taoqi.chat.agent-task.dlq-topic}",
        consumerGroup = "${taoqi.chat.agent-task.dlq-consumer-group}",
        maxReconsumeTimes = 3,
        consumeThreadNumber = 1,
        consumeThreadMax = 2)
public class AgentTaskDeadLetterConsumer implements RocketMQListener<MessageExt> {

    private static final Logger log = LoggerFactory.getLogger(AgentTaskDeadLetterConsumer.class);

    @Resource
    private AgentTaskDeadLetterService agentTaskDeadLetterService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(MessageExt message) {
        Long taskId = null;
        try {
            AgentTaskMessage payload = objectMapper.readValue(message.getBody(), AgentTaskMessage.class);
            taskId = payload.taskId();
        }
        catch (RuntimeException e) {
            log.error("死信消息体解析失败，仅留档, msgId={}", message.getMsgId(), e);
        }
        agentTaskDeadLetterService.record(message, taskId);
    }
}