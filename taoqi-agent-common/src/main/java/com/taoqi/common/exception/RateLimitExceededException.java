package com.taoqi.common.exception;

/**
 * 请求被限流时抛出的异常，携带建议的重试等待时间。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * 建议调用方等待的秒数，可用于响应头的 Retry-After。
     */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
