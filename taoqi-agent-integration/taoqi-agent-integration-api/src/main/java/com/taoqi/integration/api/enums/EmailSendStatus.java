package com.taoqi.integration.api.enums;

/**
 * 邮件发送状态。
 *
 * <p>数据库中以枚举名（如 {@code SENDING}）存储，MyBatis 默认按名称映射。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public enum EmailSendStatus {

    /**
     * 已提交，正在发送。
     */
    SENDING("发送中"),

    /**
     * 发送成功。
     */
    SUCCESS("发送成功"),

    /**
     * 发送失败。
     */
    FAILED("发送失败");

    private final String description;

    EmailSendStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
