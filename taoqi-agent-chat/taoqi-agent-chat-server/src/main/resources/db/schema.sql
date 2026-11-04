-- AI 任务表，适用于本地 MySQL 8+
-- 应用启动时会自动执行本脚本（CREATE ... IF NOT EXISTS），也可手动执行：
--   mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS `taoqi-agent-chat`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `taoqi-agent-chat`;

CREATE TABLE IF NOT EXISTS `agent_task` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_type`     VARCHAR(32)  NOT NULL COMMENT '任务类型：SUMMARY / COPYWRITING / IDEA',
    `input_content` TEXT         NOT NULL COMMENT '提交的原始输入',
    `status`        VARCHAR(20)  NOT NULL COMMENT '任务状态：PENDING / RUNNING / SUCCESS / FAILED',
    `result`        TEXT                  DEFAULT NULL COMMENT '生成结果',
    `error_message` VARCHAR(1000)         DEFAULT NULL COMMENT '最近一次失败原因',
    `retry_count`   INT          NOT NULL DEFAULT 0 COMMENT '执行失败次数',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `finished_at`   DATETIME              DEFAULT NULL COMMENT '执行完成时间（成功或失败）',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 异步任务';

CREATE TABLE IF NOT EXISTS `agent_task_dead_letter` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`          BIGINT                DEFAULT NULL COMMENT '原任务主键，消息体无法解析时为空',
    `msg_id`           VARCHAR(128) NOT NULL COMMENT '死信消息 ID，唯一',
    `consumer_group`   VARCHAR(128) NOT NULL COMMENT '原消费者组',
    `topic`            VARCHAR(128) NOT NULL COMMENT '原消息主题',
    `payload`          TEXT                  DEFAULT NULL COMMENT '原始消息体',
    `reconsume_times`  INT          NOT NULL DEFAULT 0 COMMENT '死信消息重试次数',
    `error_message`    VARCHAR(1000)         DEFAULT NULL COMMENT '任务失败原因',
    `status`           VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING / REPLAYING / REPLAYED / IGNORED',
    `replayed_task_id` BIGINT                DEFAULT NULL COMMENT '重放后新建的任务主键',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `replayed_at`      DATETIME              DEFAULT NULL COMMENT '重放时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_msg_id` (`msg_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 任务死信记录';