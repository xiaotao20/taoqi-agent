package com.taoqi.common.ratelimit;

import java.time.Duration;

/**
 * 限流计数存储抽象。
 *
 * <p>内存实现只对单实例有效；多实例部署时切换到 Redis 实现，所有实例共享同一份计数。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public interface RateLimitStore {

    /**
     * 尝试获取一次调用额度。
     *
     * @param key    限流 key
     * @param limit  时间窗口内允许的最大次数
     * @param window 时间窗口
     * @return 是否放行，以及被拒绝时建议的重试等待时间
     */
    RateLimitDecision tryAcquire(String key, int limit, Duration window);
}