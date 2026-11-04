package com.taoqi.chat.mq;

import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.taoqi.chat.config.AgentTaskProperties;

/**
 * 基于 RocketMQ 的任务派发器：把任务主键作为消息发送到配置的 topic。
 *
 * <p>traceId 通过消息用户属性透传，消费端写回 MDC，异步执行日志仍能串到原请求。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Component
@ConditionalOnProperty(prefix = "taoqi.chat.agent-task", name = "dispatch-mode",
        havingValue = "mq", matchIfMissing = true)
public class MqAgentTaskDispatcher implements AgentTaskDispatcher {

    private static final Logger log = LoggerFactory.getLogger(MqAgentTaskDispatcher.class);

    private static final String TRACE_ID_PROPERTY = "traceId";

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private AgentTaskProperties agentTaskProperties;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void dispatch(Long taskId) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(new AgentTaskMessage(taskId));
            Message message = new Message(agentTaskProperties.getTopic(), body);
            String traceId = MDC.get(TRACE_ID_PROPERTY);
            if (StringUtils.hasText(traceId)) {
                message.putUserProperty(TRACE_ID_PROPERTY, traceId);
            }

            SendResult result = rocketMQTemplate.getProducer().send(message);
            log.info("AI 任务消息已发送, taskId={}, msgId={}, traceId={}", taskId, result.getMsgId(), traceId);
        }
        catch (Exception e) {
            throw new IllegalStateException("AI 任务消息发送失败, taskId=" + taskId, e);
        }
    }
}