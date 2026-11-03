package com.taoqi.chat.controller;

import com.taoqi.chat.facade.EmailClientFacade;
import com.taoqi.common.annotation.RateLimit;
import com.taoqi.common.result.R;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天模块的测试接口：通过 {@code EmailClientFacade} 触发一封测试邮件。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@RestController
public class ChatController {

    @Resource
    private EmailClientFacade emailClientFacade;

    /**
     * 测试接口：发送测试邮件，不传 to 时发送到 taoqi.chat.test-email-to 配置的地址。
     *
     * <p>使用 {@code @RateLimit} 限制每个来源每分钟最多调用 5 次，防止误刷导致重复发信。
     */
    @RateLimit(limit = 5, windowSeconds = 60)
    @GetMapping("/api/chat/email/test")
    public R<EmailSubmitResult> sendTestEmail(@RequestParam(name = "to", required = false) String to) {
        return R.data(emailClientFacade.sendTestEmail(to));
    }

    /**
     * 连通性测试接口，返回统一的 {@code R} 结构；受 {@code @RateLimit} 限制：每个来源 30 秒内最多调用 2 次。
     */
    @RateLimit(limit = 2, windowSeconds = 30)
    @GetMapping("/api/chat/ping")
    public R<String> ping() {
        return R.data("pong");
    }
}
