package com.taoqi.chat.controller;

import com.taoqi.chat.facade.ChatFacade;
import com.taoqi.integration.api.dto.EmailSendResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天模块的测试接口：通过 {@code ChatFacade} 触发一封测试邮件。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@RestController
public class ChatController {

    private final ChatFacade chatFacade;

    public ChatController(ChatFacade chatFacade) {
        this.chatFacade = chatFacade;
    }

    /**
     * 测试接口：发送测试邮件，不传 to 时发送到 taoqi.chat.test-email-to 配置的地址。
     */
    @GetMapping("/api/chat/email/test")
    public EmailSendResult sendTestEmail(@RequestParam(name = "to", required = false) String to) {
        return chatFacade.sendTestEmail(to);
    }
}
