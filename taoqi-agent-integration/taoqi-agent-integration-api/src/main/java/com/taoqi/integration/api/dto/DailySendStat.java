package com.taoqi.integration.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 按天聚合的邮件发送统计。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public class DailySendStat {

    /**
     * 统计日期。
     */
    private LocalDate statDate;

    /**
     * 当天发送总数。
     */
    private Long totalCount;

    /**
     * 当天成功数。
     */
    private Long successCount;

    /**
     * 当天失败数。
     */
    private Long failureCount;

    /**
     * 当天仍在发送中的数量。
     */
    private Long sendingCount;

    /**
     * 当天平均发送耗时，单位毫秒。
     */
    private BigDecimal avgCostMillis;

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Long failureCount) {
        this.failureCount = failureCount;
    }

    public Long getSendingCount() {
        return sendingCount;
    }

    public void setSendingCount(Long sendingCount) {
        this.sendingCount = sendingCount;
    }

    public BigDecimal getAvgCostMillis() {
        return avgCostMillis;
    }

    public void setAvgCostMillis(BigDecimal avgCostMillis) {
        this.avgCostMillis = avgCostMillis;
    }
}
