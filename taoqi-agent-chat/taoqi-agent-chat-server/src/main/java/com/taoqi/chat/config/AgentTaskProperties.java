package com.taoqi.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 任务配置：消息投递方式、topic / 消费组与模拟执行参数。
 *
 * <p>RocketMQ 的 name-server 与生产者组由 starter 的 {@code rocketmq.*} 配置提供。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@ConfigurationProperties(prefix = "taoqi.chat.agent-task")
public class AgentTaskProperties {

    /**
     * 任务派发方式：{@code mq} 走 RocketMQ，{@code local} 走本机线程池（本地无 MQ 时使用）。
     */
    private String dispatchMode = "mq";

    /**
     * 模拟模型调用的耗时，单位毫秒；接入真实模型后该配置失效。
     */
    private long simulateDelayMillis = 3000;

    /**
     * AI 任务主题。
     */
    private String topic = "taoqi-chat-agent-task";

    /**
     * 消费者组。
     */
    private String consumerGroup = "taoqi-agent-chat-agent-task-consumer";

    public String getDispatchMode() {
        return dispatchMode;
    }

    public void setDispatchMode(String dispatchMode) {
        this.dispatchMode = dispatchMode;
    }

    public long getSimulateDelayMillis() {
        return simulateDelayMillis;
    }

    public void setSimulateDelayMillis(long simulateDelayMillis) {
        this.simulateDelayMillis = simulateDelayMillis;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }
}