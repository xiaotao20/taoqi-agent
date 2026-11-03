package com.taoqi.chat.service;

import java.security.SecureRandom;
import java.util.List;

import com.taoqi.chat.cache.VerificationCodeCache;
import com.taoqi.chat.constant.EmailTemplateConstants;
import com.taoqi.chat.constant.VerificationCodeConstants;
import com.taoqi.chat.facade.EmailClientFacade;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 邮箱验证码服务：生成验证码、写入本地缓存，并通过邮件服务发送 HTML 邮件。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Service
public class VerificationCodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Resource
    private VerificationCodeCache verificationCodeCache;

    @Resource
    private EmailClientFacade emailClientFacade;

    /**
     * 生成并发送验证码，验证码有效期为 5 分钟。
     */
    public EmailSubmitResult send(String email) {
        String code = generateCode();
        verificationCodeCache.put(email, code);

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

    private String generateCode() {
        return String.format("%0" + VerificationCodeConstants.CODE_LENGTH + "d",
                SECURE_RANDOM.nextInt((int) Math.pow(10, VerificationCodeConstants.CODE_LENGTH)));
    }
}