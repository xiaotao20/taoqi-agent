package com.taoqi.chat.facade.impl;

import com.taoqi.chat.config.ChatProperties;
import com.taoqi.chat.facade.ChatFacade;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSendResult;
import com.taoqi.integration.client.EmailFeignClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * {@code ChatFacade} 的实现：确定收件人并通过 Feign 调用邮件服务。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Service
public class ChatFacadeImpl implements ChatFacade {

    private final EmailFeignClient emailFeignClient;
    private final ChatProperties chatProperties;

    public ChatFacadeImpl(EmailFeignClient emailFeignClient, ChatProperties chatProperties) {
        this.emailFeignClient = emailFeignClient;
        this.chatProperties = chatProperties;
    }

    @Override
    public EmailSendResult sendTestEmail(String to) {
        String receiver = StringUtils.hasText(to) ? to : chatProperties.testEmailTo();
        EmailSendRequest request = new EmailSendRequest(
                List.of(receiver),
                null,
                null,
                "taoqi-agent-chat Feign 测试邮件",
                "这是一封由 taoqi-agent-chat 通过 Feign 调用 taoqi-agent-integration 邮件服务发送的测试邮件。",
                false);
        return emailFeignClient.send(request);
    }
}
