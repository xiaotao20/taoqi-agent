package com.taoqi.chat.api.enums;

/**
 * AI 任务状态。
 *
 * <p>数据库中以枚举名（如 {@code PENDING}）存储，MyBatis 默认按名称映射。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
public enum AgentTaskStatus {

    /**
     * 已提交，等待消费。
     */
    PENDING("排队中"),

    /**
     * 消费中，正在生成结果。
     */
    RUNNING("处理中"),

    /**
     * 执行成功。
     */
    SUCCESS("已完成"),

    /**
     * 执行失败，可能等待 MQ 重投。
     */
    FAILED("失败"),

    /**
     * 重投次数耗尽，消息已进入死信队列。
     */
    DEAD("已进入死信");

    private final String description;

    AgentTaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}