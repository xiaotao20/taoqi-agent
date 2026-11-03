package com.taoqi.integration.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.taoqi.integration.api.dto.DailySendStat;
import com.taoqi.integration.api.dto.EmailRecordQuery;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.TopRecipient;
import com.taoqi.integration.api.entity.EmailSendRecordEntity;
import com.taoqi.integration.api.enums.EmailSendStatus;
import com.taoqi.integration.mapper.EmailSendRecordMapper;
import com.taoqi.integration.service.EmailRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 邮件发送记录服务实现。
 *
 * <p>写入、状态更新、主键查询、条件分页使用 MyBatis-Plus 的 {@code IService} 与条件构造器；
 * 每日统计、收件人排行等复杂聚合 SQL 走 Mapper XML。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Service
public class EmailRecordServiceImpl extends ServiceImpl<EmailSendRecordMapper, EmailSendRecordEntity>
        implements EmailRecordService {

    private static final int MAX_STAT_DAYS = 90;

    private static final int MAX_TOP_LIMIT = 50;

    @Override
    public Long create(EmailSendRequest request, EmailSendStatus status, String errorMessage) {
        EmailSendRecordEntity entity = new EmailSendRecordEntity();
        entity.setToAddresses(joinAddresses(request.to()));
        entity.setCcAddresses(joinAddresses(request.cc()));
        entity.setBccAddresses(joinAddresses(request.bcc()));
        entity.setSubject(request.subject());
        entity.setContent(request.content());
        entity.setHtml(request.htmlEnabled());
        entity.setStatus(status);
        entity.setErrorMessage(errorMessage);
        entity.setCreatedAt(LocalDateTime.now());
        if (status != EmailSendStatus.SENDING) {
            entity.setSentAt(LocalDateTime.now());
        }
        save(entity);
        return entity.getId();
    }

    @Override
    public void markSuccess(Long recordId, String messageId, long costMillis) {
        update(Wrappers.<EmailSendRecordEntity>lambdaUpdate()
                .set(EmailSendRecordEntity::getStatus, EmailSendStatus.SUCCESS)
                .set(EmailSendRecordEntity::getMessageId, messageId)
                .set(EmailSendRecordEntity::getCostMillis, costMillis)
                .set(EmailSendRecordEntity::getSentAt, LocalDateTime.now())
                .eq(EmailSendRecordEntity::getId, recordId));
    }

    @Override
    public void markFailed(Long recordId, String errorMessage, long costMillis) {
        update(Wrappers.<EmailSendRecordEntity>lambdaUpdate()
                .set(EmailSendRecordEntity::getStatus, EmailSendStatus.FAILED)
                .set(EmailSendRecordEntity::getErrorMessage, errorMessage)
                .set(EmailSendRecordEntity::getCostMillis, costMillis)
                .set(EmailSendRecordEntity::getSentAt, LocalDateTime.now())
                .eq(EmailSendRecordEntity::getId, recordId));
    }

    @Override
    public IPage<EmailSendRecordEntity> query(EmailRecordQuery query) {
        LambdaQueryWrapper<EmailSendRecordEntity> wrapper = Wrappers.<EmailSendRecordEntity>lambdaQuery()
                // 收件人以逗号分隔存储，用 FIND_IN_SET 做精确匹配
                .apply(StringUtils.hasText(query.to()), "FIND_IN_SET({0}, to_addresses)", query.to())
                .like(StringUtils.hasText(query.subject()), EmailSendRecordEntity::getSubject, query.subject())
                .eq(query.status() != null, EmailSendRecordEntity::getStatus, query.status())
                .ge(query.startTime() != null, EmailSendRecordEntity::getCreatedAt, query.startTime())
                .le(query.endTime() != null, EmailSendRecordEntity::getCreatedAt, query.endTime())
                .orderByDesc(EmailSendRecordEntity::getCreatedAt)
                .orderByDesc(EmailSendRecordEntity::getId);

        return page(new Page<>(query.page(), query.size()), wrapper);
    }

    @Override
    public EmailSendRecordEntity getById(Long id) {
        return super.getById(id);
    }

    @Override
    public List<DailySendStat> dailyStats(int days) {
        return baseMapper.selectDailyStats(Math.min(MAX_STAT_DAYS, Math.max(1, days)));
    }

    @Override
    public List<TopRecipient> topRecipients(int limit) {
        return baseMapper.selectTopRecipients(Math.min(MAX_TOP_LIMIT, Math.max(1, limit)));
    }

    private String joinAddresses(List<String> addresses) {
        return addresses == null || addresses.isEmpty() ? null : String.join(",", addresses);
    }
}
