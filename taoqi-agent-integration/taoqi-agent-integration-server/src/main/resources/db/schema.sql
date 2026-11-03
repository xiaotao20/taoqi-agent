-- 邮件发送记录表，适用于本地 MySQL 8+
-- 应用启动时会自动执行本脚本（CREATE ... IF NOT EXISTS），也可手动执行：
--   mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS `taoqi-agent`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `taoqi-agent`;

CREATE TABLE IF NOT EXISTS `email_send_record` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `to_addresses`  VARCHAR(1000) NOT NULL COMMENT '收件人，多个用逗号分隔',
    `cc_addresses`  VARCHAR(1000)          DEFAULT NULL COMMENT '抄送人，多个用逗号分隔',
    `bcc_addresses` VARCHAR(1000)          DEFAULT NULL COMMENT '密送人，多个用逗号分隔',
    `subject`       VARCHAR(500)  NOT NULL COMMENT '邮件主题',
    `content`       TEXT COMMENT '邮件正文',
    `html`          TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '正文是否为 HTML',
    `status`        VARCHAR(20)   NOT NULL COMMENT '发送状态：SENDING / SUCCESS / FAILED',
    `message_id`    VARCHAR(255)           DEFAULT NULL COMMENT '邮件 Message-ID，发送成功后写入',
    `error_message` VARCHAR(1000)          DEFAULT NULL COMMENT '失败原因',
    `cost_millis`   BIGINT                 DEFAULT NULL COMMENT '发送耗时（毫秒），完成后写入',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `sent_at`       DATETIME               DEFAULT NULL COMMENT '发送完成时间（成功或失败）',
    PRIMARY KEY (`id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status` (`status`),
    KEY `idx_to_addresses` (`to_addresses`(255))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '邮件发送记录';
