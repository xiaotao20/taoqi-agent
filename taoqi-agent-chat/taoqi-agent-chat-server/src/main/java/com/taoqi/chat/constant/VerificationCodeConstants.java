package com.taoqi.chat.constant;

/**
 * 邮箱验证码的公共常量。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public final class VerificationCodeConstants {

    /**
     * 验证码有效时间（分钟）。
     */
    public static final int EXPIRE_MINUTES = 5;

    /**
     * 验证码位数。
     */
    public static final int CODE_LENGTH = 6;

    private VerificationCodeConstants() {
    }
}