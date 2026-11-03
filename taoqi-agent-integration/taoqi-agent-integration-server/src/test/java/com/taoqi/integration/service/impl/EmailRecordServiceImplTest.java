package com.taoqi.integration.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taoqi.integration.api.dto.EmailRecordQuery;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.entity.EmailSendRecordEntity;
import com.taoqi.integration.api.enums.EmailSendStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 基于 H2 内存库验证记录的创建、状态流转与条件分页。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@SpringBootTest
class EmailRecordServiceImplTest {

    @Autowired
    private EmailRecordServiceImpl emailRecordService;

    @Test
    void createsSendingRecordThenMarksSuccess() {
        EmailSendRequest request = new EmailSendRequest(
                List.of("async@example.com"), null, null, "异步发送测试", "内容", false);

        Long recordId = emailRecordService.create(request, EmailSendStatus.SENDING, null);
        EmailSendRecordEntity created = emailRecordService.getById(recordId);
        assertThat(created.getStatus()).isEqualTo(EmailSendStatus.SENDING);
        assertThat(created.getSentAt()).isNull();

        emailRecordService.markSuccess(recordId, "<async@localhost>", 12L);

        EmailSendRecordEntity updated = emailRecordService.getById(recordId);
        assertThat(updated.getStatus()).isEqualTo(EmailSendStatus.SUCCESS);
        assertThat(updated.getMessageId()).isEqualTo("<async@localhost>");
        assertThat(updated.getCostMillis()).isEqualTo(12L);
        assertThat(updated.getSentAt()).isNotNull();
    }

    @Test
    void queriesByStatusWithMybatisPlus() {
        EmailSendRequest request = new EmailSendRequest(
                List.of("page@example.com"), null, null, "分页状态查询", "内容", false);
        Long recordId = emailRecordService.create(request, EmailSendStatus.SENDING, null);
        emailRecordService.markFailed(recordId, "模拟失败", 3L);

        IPage<EmailSendRecordEntity> page = emailRecordService.query(
                new EmailRecordQuery(null, "分页状态查询", EmailSendStatus.FAILED, null, null, 1, 10));

        assertThat(page.getTotal()).isGreaterThanOrEqualTo(1);
        assertThat(page.getRecords()).anySatisfy(record -> {
            assertThat(record.getSubject()).isEqualTo("分页状态查询");
            assertThat(record.getStatus()).isEqualTo(EmailSendStatus.FAILED);
            assertThat(record.getErrorMessage()).isEqualTo("模拟失败");
        });
        assertThat(page.getCurrent()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(10);
    }
}
