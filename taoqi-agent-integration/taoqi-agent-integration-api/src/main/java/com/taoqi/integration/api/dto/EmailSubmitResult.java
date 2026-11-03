package com.taoqi.integration.api.dto;

import com.taoqi.integration.api.enums.EmailSendStatus;

/**
 * 邮件提交结果。
 *
 * <p>邮件服务采用异步发送：接口收到请求后先落库一条状态为 {@code SENDING} 的记录并立即返回，
 * 调用方可以用 {@code recordId} 查询发送记录获取最终状态。
 *
 * @param recordId 发送记录主键
 * @param status   提交时的状态
 * @param message  提示信息
 * @author xiaotao
 * @date 2026-11-03
 */
public record EmailSubmitResult(Long recordId, EmailSendStatus status, String message) {

    /**
     * 已受理，邮件进入异步发送流程。
     */
    public static EmailSubmitResult accepted(Long recordId) {
        return new EmailSubmitResult(recordId, EmailSendStatus.SENDING, "邮件已提交，正在发送");
    }

    /**
     * 提交阶段即失败（如未配置发件人），不会进入异步发送。
     */
    public static EmailSubmitResult rejected(Long recordId, String message) {
        return new EmailSubmitResult(recordId, EmailSendStatus.FAILED, message);
    }
}
