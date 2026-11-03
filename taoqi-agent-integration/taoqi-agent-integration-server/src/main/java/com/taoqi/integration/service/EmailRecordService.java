package com.taoqi.integration.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taoqi.integration.api.dto.DailySendStat;
import com.taoqi.integration.api.dto.EmailRecordQuery;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.TopRecipient;
import com.taoqi.integration.api.entity.EmailSendRecordEntity;
import com.taoqi.integration.api.enums.EmailSendStatus;

/**
 * 邮件发送记录的入库、状态更新与查询服务。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public interface EmailRecordService {

    /**
     * 创建一条发送记录并返回主键。
     *
     * @param status       初始状态
     * @param errorMessage 失败原因，状态为 SENDING 时传 null
     */
    Long create(EmailSendRequest request, EmailSendStatus status, String errorMessage);

    /**
     * 标记发送成功，记录 Message-ID、耗时与完成时间。
     */
    void markSuccess(Long recordId, String messageId, long costMillis);

    /**
     * 标记发送失败，记录失败原因、耗时与完成时间。
     */
    void markFailed(Long recordId, String errorMessage, long costMillis);

    /**
     * 按条件分页查询发送记录，直接返回 MyBatis-Plus 的分页结果。
     */
    IPage<EmailSendRecordEntity> query(EmailRecordQuery query);

    /**
     * 按主键查询发送记录。
     */
    EmailSendRecordEntity getById(Long id);

    /**
     * 查询最近 N 天的每日发送统计。
     */
    List<DailySendStat> dailyStats(int days);

    /**
     * 查询收件人发送次数排行。
     */
    List<TopRecipient> topRecipients(int limit);
}
