package com.taoqi.integration.api.dto;

import java.time.LocalDateTime;

import com.taoqi.integration.api.enums.EmailSendStatus;

/**
 * 邮件发送记录的分页查询条件。
 *
 * @param to        收件人精确匹配，为空时不筛选
 * @param subject   主题模糊匹配，为空时不筛选
 * @param status    发送状态，为空时不筛选
 * @param startTime 创建时间下限（含）
 * @param endTime   创建时间上限（含）
 * @param page      页码，从 1 开始
 * @param size      每页条数，最大 200
 * @author xiaotao
 * @date 2026-11-03
 */
public record EmailRecordQuery(String to, String subject, EmailSendStatus status,
        LocalDateTime startTime, LocalDateTime endTime, int page, int size) {

    public EmailRecordQuery {
        page = Math.max(1, page);
        size = Math.min(200, Math.max(1, size));
    }
}
