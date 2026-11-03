package com.taoqi.integration.controller;

import com.taoqi.common.result.R;
import com.taoqi.integration.api.EmailApi;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import com.taoqi.integration.service.EmailService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮件服务的 REST 接口实现：接收请求后立即返回，实际发送由 {@code EmailSendTask} 异步完成。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@RestController
public class EmailController implements EmailApi {

    @Resource
    private EmailService emailService;

    @Override
    public R<EmailSubmitResult> send(EmailSendRequest request) {
        return R.data(emailService.submit(request));
    }
}
