package com.taoqi.common.ratelimit;

/**
 * 一次限流判断的结果。
 *
 * @param allowed           是否放行
 * @param retryAfterSeconds 被拒绝时建议等待的秒数，放行时为 0
 * @author xiaotao
 * @date 2026-11-03
 */
public record RateLimitDecision(boolean allowed, long retryAfterSeconds) {

    public static RateLimitDecision permit() {
        return new RateLimitDecision(true, 0);
    }

    public static RateLimitDecision deny(long retryAfterSeconds) {
        return new RateLimitDecision(false, Math.max(1, retryAfterSeconds));
    }
}