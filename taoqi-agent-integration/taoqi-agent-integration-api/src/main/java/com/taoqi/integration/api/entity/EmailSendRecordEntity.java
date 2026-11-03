package com.taoqi.integration.api.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.taoqi.integration.api.enums.EmailSendStatus;

/**
 * 邮件发送记录实体，对应表 {@code email_send_record}。
 *
 * <p>主键使用数据库自增；字段按下划线规则映射到表列，简单查询与分页由 MyBatis-Plus 完成。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@TableName("email_send_record")
public class EmailSendRecordEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 收件人，多个地址用逗号分隔。
     */
    private String toAddresses;

    /**
     * 抄送人，多个地址用逗号分隔。
     */
    private String ccAddresses;

    /**
     * 密送人，多个地址用逗号分隔。
     */
    private String bccAddresses;

    /**
     * 邮件主题。
     */
    private String subject;

    /**
     * 邮件正文。
     */
    private String content;

    /**
     * 正文是否为 HTML。
     */
    private Boolean html;

    /**
     * 发送状态，见 {@link EmailSendStatus}。
     */
    private EmailSendStatus status;

    /**
     * 邮件 Message-ID。
     */
    private String messageId;

    /**
     * 发送失败原因。
     */
    private String errorMessage;

    /**
     * 发送耗时，单位毫秒。
     */
    private Long costMillis;

    /**
     * 记录创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 发送完成时间（成功或失败），发送中为 null。
     */
    private LocalDateTime sentAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToAddresses() {
        return toAddresses;
    }

    public void setToAddresses(String toAddresses) {
        this.toAddresses = toAddresses;
    }

    public String getCcAddresses() {
        return ccAddresses;
    }

    public void setCcAddresses(String ccAddresses) {
        this.ccAddresses = ccAddresses;
    }

    public String getBccAddresses() {
        return bccAddresses;
    }

    public void setBccAddresses(String bccAddresses) {
        this.bccAddresses = bccAddresses;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getHtml() {
        return html;
    }

    public void setHtml(Boolean html) {
        this.html = html;
    }

    public EmailSendStatus getStatus() {
        return status;
    }

    public void setStatus(EmailSendStatus status) {
        this.status = status;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getCostMillis() {
        return costMillis;
    }

    public void setCostMillis(Long costMillis) {
        this.costMillis = costMillis;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
