package com.taoqi.chat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 校验邮箱验证码的请求参数。
 *
 * @param email 接收验证码的邮箱
 * @param code  6 位数字验证码
 * @author xiaotao
 * @date 2026-11-04
 */
public record VerificationCodeVerifyRequest(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,

        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "\\d{6}", message = "验证码为 6 位数字")
        String code) {
}