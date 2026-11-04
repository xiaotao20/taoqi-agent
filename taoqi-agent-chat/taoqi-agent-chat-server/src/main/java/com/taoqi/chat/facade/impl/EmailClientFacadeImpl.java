package com.taoqi.chat.facade.impl;

import java.util.List;

import com.taoqi.chat.config.ChatProperties;
import com.taoqi.chat.facade.EmailClientFacade;
import com.taoqi.common.result.R;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import com.taoqi.integration.client.EmailFeignClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * {@code EmailClientFacade} 的实现：通过 Feign 调用邮件服务，并取出 {@code R} 中的业务数据。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Service
public class EmailClientFacadeImpl implements EmailClientFacade {

    @Resource
    private EmailFeignClient emailFeignClient;

    @Resource
    private ChatProperties chatProperties;

    @Override
    public EmailSubmitResult send(EmailSendRequest request) {
        R<EmailSubmitResult> response = emailFeignClient.send(request);
        if (response == null || response.data() == null) {
            return EmailSubmitResult.rejected(null, response == null ? "邮件服务无响应" : response.message());
        }
        return response.data();
    }

    @Override
    public EmailSubmitResult sendTestEmail(String to) {
        String receiver = StringUtils.hasText(to) ? to : chatProperties.getTestEmailTo();
        EmailSendRequest request = new EmailSendRequest(
                List.of(receiver),
                null,
                null,
                "taoqi-agent-chat Feign 测试邮件",
                "这是一封由 taoqi-agent-chat 通过 Feign 调用 taoqi-agent-integration 邮件服务发送的测试邮件。",
                false);
        return send(request);
    }
}
