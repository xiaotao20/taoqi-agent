package com.taoqi.chat.api.enums;

/**
 * 死信记录状态。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
public enum DeadLetterStatus {

    /**
     * 待处理，可以人工重放。
     */
    PENDING("待处理"),

    /**
     * 正在重放，防止并发重复操作。
     */
    REPLAYING("重放中"),

    /**
     * 已重放为新任务。
     */
    REPLAYED("已重放"),

    /**
     * 无需处理或无法处理（如消息体无法解析）。
     */
    IGNORED("已忽略");

    private final String description;

    DeadLetterStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}