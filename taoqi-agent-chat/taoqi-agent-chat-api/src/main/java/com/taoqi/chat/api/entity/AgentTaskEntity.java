package com.taoqi.chat.api.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.taoqi.chat.api.enums.AgentTaskStatus;
import com.taoqi.chat.api.enums.AgentTaskType;

/**
 * AI 任务实体，对应表 {@code agent_task}。
 *
 * <p>提交接口落一条 {@code PENDING} 记录后发消息，消费者按主键推进状态；
 * {@code retryCount} 记录执行失败次数，便于在页面上观察 MQ 重投。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@TableName("agent_task")
public class AgentTaskEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务类型，见 {@link AgentTaskType}。
     */
    private AgentTaskType taskType;

    /**
     * 提交的原始输入。
     */
    private String inputContent;

    /**
     * 任务状态，见 {@link AgentTaskStatus}。
     */
    private AgentTaskStatus status;

    /**
     * 生成结果，成功后有值。
     */
    private String result;

    /**
     * 最近一次失败原因，成功时清空。
     */
    private String errorMessage;

    /**
     * 执行失败次数，每失败一次加一。
     */
    private Integer retryCount;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 执行完成时间（成功或失败），未完成时为 null。
     */
    private LocalDateTime finishedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AgentTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(AgentTaskType taskType) {
        this.taskType = taskType;
    }

    public String getInputContent() {
        return inputContent;
    }

    public void setInputContent(String inputContent) {
        this.inputContent = inputContent;
    }

    public AgentTaskStatus getStatus() {
        return status;
    }

    public void setStatus(AgentTaskStatus status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
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

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}