package com.taoqi.chat.cache;

import java.util.Locale;

/**
 * 验证码存储抽象：发送时写入，校验成功后立即失效。
 *
 * <p>实现由 {@code taoqi.chat.verification-code.store-type} 选择：{@code redis} 多实例共享，{@code memory} 仅用于单实例或测试。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
public interface VerificationCodeStore {

    /**
     * 保存验证码，同一邮箱重新发送会覆盖旧验证码。
     */
    void put(String email, String code);

    /**
     * 校验验证码，成功后立即失效。
     */
    boolean verify(String email, String code);

    /**
     * 邮箱统一转小写并去除首尾空格，保证发码和校验使用同一个 key。
     */
    static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}