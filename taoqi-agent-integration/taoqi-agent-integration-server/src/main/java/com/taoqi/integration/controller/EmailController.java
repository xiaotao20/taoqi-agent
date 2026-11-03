package com.taoqi.integration.controller;

import com.taoqi.integration.api.EmailApi;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSendResult;
import com.taoqi.integration.service.EmailService;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮件服务的 REST 接口实现，将请求委托给 {@code EmailService}。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@RestController
public class EmailController implements EmailApi {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public EmailSendResult send(EmailSendRequest request) {
        return emailService.send(request);
    }
}
