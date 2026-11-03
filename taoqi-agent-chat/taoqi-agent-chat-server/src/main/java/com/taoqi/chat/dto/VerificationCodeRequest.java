package com.taoqi.chat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送邮箱验证码的请求参数。
 *
 * @param email 接收验证码的邮箱
 * @author xiaotao
 * @date 2026-11-03
 */
public record VerificationCodeRequest(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email) {
}