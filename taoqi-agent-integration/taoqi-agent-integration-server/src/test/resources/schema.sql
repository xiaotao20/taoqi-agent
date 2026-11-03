-- 单元测试用的 H2 表结构，字段与 MySQL 的 email_send_record 保持一致
CREATE TABLE IF NOT EXISTS email_send_record (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    to_addresses  VARCHAR(1000) NOT NULL,
    cc_addresses  VARCHAR(1000),
    bcc_addresses VARCHAR(1000),
    subject       VARCHAR(500)  NOT NULL,
    content       CLOB,
    html          BOOLEAN       NOT NULL DEFAULT FALSE,
    status        VARCHAR(20)   NOT NULL,
    message_id    VARCHAR(255),
    error_message VARCHAR(1000),
    cost_millis   BIGINT,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at       TIMESTAMP
);
