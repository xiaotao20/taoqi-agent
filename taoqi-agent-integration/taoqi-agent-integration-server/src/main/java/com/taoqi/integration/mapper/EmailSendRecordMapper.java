package com.taoqi.integration.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoqi.integration.api.dto.DailySendStat;
import com.taoqi.integration.api.dto.TopRecipient;
import com.taoqi.integration.api.entity.EmailSendRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 邮件发送记录 Mapper。
 *
 * <p>简单 CRUD 与条件分页由 {@code BaseMapper} 和 {@code LambdaQueryWrapper} 完成；
 * 只有聚合类复杂 SQL 写在 {@code resources/mapper/EmailSendRecordMapper.xml} 中。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Mapper
public interface EmailSendRecordMapper extends BaseMapper<EmailSendRecordEntity> {

    /**
     * 按天统计最近 N 天的发送总数、成功数、失败数与平均耗时。
     */
    List<DailySendStat> selectDailyStats(@Param("days") int days);

    /**
     * 统计收件人发送次数排行。
     *
     * <p>收件人以逗号分隔存储，SQL 中拆分每个地址后聚合，最多支持单条记录 10 个收件人。
     */
    List<TopRecipient> selectTopRecipients(@Param("limit") int limit);
}
