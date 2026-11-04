package com.taoqi.chat.api.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.taoqi.chat.api.enums.DeadLetterStatus;

/**
 * 死信记录实体，对应表 {@code agent_task_dead_letter}。
 *
 * <p>每条死信保存原始消息、失败原因和处理状态，便于排查与人工重放；
 * {@code replayedTaskId} 指向重放后新建的任务，原任务保留在终态作为历史。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@TableName("agent_task_dead_letter")
public class AgentTaskDeadLetterEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原任务主键；消息体无法解析时为空。
     */
    private Long taskId;

    /**
     * 死信消息 ID，唯一，用于幂等落库。
     */
    private String msgId;

    /**
     * 原消费者组。
     */
    private String consumerGroup;

    /**
     * 原消息主题。
     */
    private String topic;

    /**
     * 原始消息体。
     */
    private String payload;

    /**
     * 死信消息的重投次数。
     */
    private Integer reconsumeTimes;

    /**
     * 任务失败原因。
     */
    private String errorMessage;

    /**
     * 死信处理状态，见 {@link DeadLetterStatus}。
     */
    private DeadLetterStatus status;

    /**
     * 重放后新建的任务主键。
     */
    private Long replayedTaskId;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 重放时间。
     */
    private LocalDateTime replayedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getReconsumeTimes() {
        return reconsumeTimes;
    }

    public void setReconsumeTimes(Integer reconsumeTimes) {
        this.reconsumeTimes = reconsumeTimes;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public DeadLetterStatus getStatus() {
        return status;
    }

    public void setStatus(DeadLetterStatus status) {
        this.status = status;
    }

    public Long getReplayedTaskId() {
        return replayedTaskId;
    }

    public void setReplayedTaskId(Long replayedTaskId) {
        this.replayedTaskId = replayedTaskId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getReplayedAt() {
        return replayedAt;
    }

    public void setReplayedAt(LocalDateTime replayedAt) {
        this.replayedAt = replayedAt;
    }
}