package com.taoqi.chat.controller;

import com.taoqi.chat.dto.VerificationCodeRequest;
import com.taoqi.chat.dto.VerificationCodeVerifyRequest;
import com.taoqi.chat.service.VerificationCodeService;
import com.taoqi.common.annotation.RateLimit;
import com.taoqi.common.result.R;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮箱验证码接口：生成 6 位验证码写入存储（默认 Redis，5 分钟过期），并支持校验。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@RestController
@RequestMapping("/api/chat/verification-code")
public class VerificationCodeController {

    @Resource
    private VerificationCodeService verificationCodeService;

    /**
     * 发送邮箱验证码，每个来源每分钟最多调用 3 次。
     */
    @RateLimit(limit = 3, windowSeconds = 60)
    @PostMapping
    public R<EmailSubmitResult> send(@Valid @RequestBody VerificationCodeRequest request) {
        return R.data(verificationCodeService.send(request.email()));
    }

    /**
     * 校验邮箱验证码，每个来源每分钟最多尝试 5 次；验证码错误或已过期时返回业务码 400。
     */
    @RateLimit(limit = 5, windowSeconds = 60)
    @PostMapping("/verify")
    public R<Void> verify(@Valid @RequestBody VerificationCodeVerifyRequest request) {
        return verificationCodeService.verify(request.email(), request.code())
                ? R.ok()
                : R.fail(400, "验证码错误或已过期");
    }
}