package com.taoqi.integration.api.dto;

/**
 * 收件人发送次数排行。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public class TopRecipient {

    /**
     * 收件人地址。
     */
    private String recipient;

    /**
     * 发送次数。
     */
    private Long sendCount;

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public Long getSendCount() {
        return sendCount;
    }

    public void setSendCount(Long sendCount) {
        this.sendCount = sendCount;
    }
}
