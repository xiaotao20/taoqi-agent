package com.taoqi.integration.event;

import com.taoqi.integration.api.dto.EmailSendRequest;

/**
 * 邮件发送事件：提交阶段落库后发布，由监听器异步执行真正的 SMTP 发送。
 *
 * @param recordId 发送记录主键
 * @param request  邮件发送请求
 * @author xiaotao
 * @date 2026-11-03
 */
public record EmailSendEvent(Long recordId, EmailSendRequest request) {
}