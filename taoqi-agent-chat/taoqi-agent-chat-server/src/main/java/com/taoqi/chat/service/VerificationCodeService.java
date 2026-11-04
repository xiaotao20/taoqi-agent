package com.taoqi.chat.service;

import java.security.SecureRandom;
import java.util.List;

import com.taoqi.chat.cache.VerificationCodeStore;
import com.taoqi.chat.constant.EmailTemplateConstants;
import com.taoqi.chat.constant.VerificationCodeConstants;
import com.taoqi.chat.facade.EmailClientFacade;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 邮箱验证码服务：生成验证码、写入验证码存储（默认 Redis），并通过邮件服务发送 HTML 邮件。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Service
public class VerificationCodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Resource
    private VerificationCodeStore verificationCodeStore;

    @Resource
    private EmailClientFacade emailClientFacade;

    /**
     * 生成并发送验证码，验证码有效期为 5 分钟。
     */
    public EmailSubmitResult send(String email) {
        String code = generateCode();
        verificationCodeStore.put(email, code);

        String html = EmailTemplateConstants.verificationCodeHtml(code, VerificationCodeConstants.EXPIRE_MINUTES);
        EmailSendRequest request = new EmailSendRequest(
                List.of(email),
                null,
                null,
                "Your ChatGPT verification code",
                html,
                true);
        return emailClientFacade.send(request);
    }

    /**
     * 校验验证码，校验成功后验证码立即失效。
     */
    public boolean verify(String email, String code) {
        return verificationCodeStore.verify(email, code);
    }

    private String generateCode() {
        return String.format("%0" + VerificationCodeConstants.CODE_LENGTH + "d",
                SECURE_RANDOM.nextInt((int) Math.pow(10, VerificationCodeConstants.CODE_LENGTH)));
    }
}