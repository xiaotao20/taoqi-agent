package com.taoqi.integration.mapper;

import java.time.LocalDateTime;

import com.taoqi.integration.api.entity.EmailSendRecordEntity;
import com.taoqi.integration.api.enums.EmailSendStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 基于 H2 内存库验证实体与 MyBatis-Plus 的基础映射（复杂 SQL 依赖 MySQL，由本地数据库验证）。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@SpringBootTest
class EmailSendRecordMapperTest {

    @Autowired
    private EmailSendRecordMapper emailSendRecordMapper;

    @Test
    void insertsAndReadsRecord() {
        EmailSendRecordEntity record = new EmailSendRecordEntity();
        record.setToAddresses("receiver@example.com");
        record.setSubject("Mapper 测试");
        record.setContent("内容");
        record.setHtml(false);
        record.setStatus(EmailSendStatus.SUCCESS);
        record.setMessageId("<test@localhost>");
        record.setCostMillis(12L);
        record.setCreatedAt(LocalDateTime.now());
        record.setSentAt(LocalDateTime.now());

        emailSendRecordMapper.insert(record);

        assertThat(record.getId()).isNotNull();
        EmailSendRecordEntity loaded = emailSendRecordMapper.selectById(record.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getToAddresses()).isEqualTo("receiver@example.com");
        assertThat(loaded.getSubject()).isEqualTo("Mapper 测试");
        assertThat(loaded.getStatus()).isEqualTo(EmailSendStatus.SUCCESS);
        assertThat(loaded.getSentAt()).isNotNull();
        assertThat(loaded.getCostMillis()).isEqualTo(12L);
        assertThat(loaded.getCreatedAt()).isNotNull();
    }
}
