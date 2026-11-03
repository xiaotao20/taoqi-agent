package com.taoqi.chat.facade;

import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;

/**
 * 邮件服务的门面：封装对邮件服务 Feign 客户端的调用，向上返回业务数据。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public interface EmailClientFacade {

    /**
     * 按给定的请求发送邮件，返回邮件服务的提交结果。
     */
    EmailSubmitResult send(EmailSendRequest request);

    /**
     * 发送测试邮件。
     *
     * @param to 收件人地址，为空时使用配置的默认收件人
     */
    EmailSubmitResult sendTestEmail(String to);
}
