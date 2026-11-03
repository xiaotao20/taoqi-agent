package com.taoqi.integration.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taoqi.common.result.R;
import com.taoqi.integration.api.dto.DailySendStat;
import com.taoqi.integration.api.dto.EmailRecordQuery;
import com.taoqi.integration.api.dto.TopRecipient;
import com.taoqi.integration.api.entity.EmailSendRecordEntity;
import com.taoqi.integration.api.enums.EmailSendStatus;
import com.taoqi.integration.service.EmailRecordService;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮件发送记录查询接口，演示 MyBatis-Plus 的内置查询与自定义 XML SQL。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@RestController
@RequestMapping("/api/email/records")
public class EmailRecordController {

    @Resource
    private EmailRecordService emailRecordService;

    /**
     * 分页查询发送记录，返回 MyBatis-Plus 的分页对象。
     */
    @GetMapping
    public R<IPage<EmailSendRecordEntity>> query(
            @RequestParam(name = "to", required = false) String to,
            @RequestParam(name = "subject", required = false) String subject,
            @RequestParam(name = "status", required = false) EmailSendStatus status,
            @RequestParam(name = "startTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return R.data(emailRecordService.query(
                new EmailRecordQuery(to, subject, status, startTime, endTime, page, size)));
    }

    /**
     * 按主键查询单条发送记录，演示 MyBatis-Plus 的 {@code getById}。
     */
    @GetMapping("/{id}")
    public R<EmailSendRecordEntity> getById(@PathVariable(name = "id") Long id) {
        EmailSendRecordEntity record = emailRecordService.getById(id);
        return record == null ? R.fail(404, "记录不存在") : R.data(record);
    }

    /**
     * 最近 N 天的每日发送统计。
     */
    @GetMapping("/daily-stats")
    public R<List<DailySendStat>> dailyStats(@RequestParam(name = "days", defaultValue = "7") int days) {
        return R.data(emailRecordService.dailyStats(days));
    }

    /**
     * 收件人发送次数排行。
     */
    @GetMapping("/top-recipients")
    public R<List<TopRecipient>> topRecipients(@RequestParam(name = "limit", defaultValue = "5") int limit) {
        return R.data(emailRecordService.topRecipients(limit));
    }
}
