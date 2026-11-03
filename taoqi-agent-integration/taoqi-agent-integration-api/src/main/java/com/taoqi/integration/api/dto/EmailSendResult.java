package com.taoqi.integration.api.dto;

/**
 * 邮件发送结果。
 *
 * @param success      是否发送成功
 * @param messageId    邮件 Message-ID，发送成功时返回
 * @param errorMessage 失败原因，发送成功时为 null
 */
public record EmailSendResult(boolean success, String messageId, String errorMessage) {

    public static EmailSendResult ok(String messageId) {
        return new EmailSendResult(true, messageId, null);
    }

    public static EmailSendResult fail(String errorMessage) {
        return new EmailSendResult(false, null, errorMessage);
    }
}
