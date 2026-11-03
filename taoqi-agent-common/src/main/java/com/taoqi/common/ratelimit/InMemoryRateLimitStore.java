package com.taoqi.common.ratelimit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.LongSupplier;

/**
 * 基于本机内存的限流存储，使用固定时间窗口计数。
 *
 * <p>只对单实例有效：多实例部署时每个实例各自计数，全局额度会随实例数放大。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public class InMemoryRateLimitStore implements RateLimitStore {

    private static final int CLEANUP_THRESHOLD = 1024;

    private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();

    private final LongSupplier clock;

    public InMemoryRateLimitStore() {
        this(System::currentTimeMillis);
    }

    /**
     * 允许注入时钟，便于测试窗口过期逻辑。
     */
    public InMemoryRateLimitStore(LongSupplier clock) {
        this.clock = clock;
    }

    @Override
    public RateLimitDecision tryAcquire(String key, int limit, Duration window) {
        long now = clock.getAsLong();
        long windowMillis = Math.max(1, window.toMillis());
        int maxLimit = Math.max(1, limit);

        Window current = windows.compute(key, (k, existing) -> {
            if (existing == null || now >= existing.resetAt()) {
                return new Window(now + windowMillis, 1);
            }
            return new Window(existing.resetAt(), existing.count() + 1);
        });

        cleanupIfNecessary(now);

        if (current.count() > maxLimit) {
            return RateLimitDecision.deny((current.resetAt() - now + 999) / 1000);
        }
        return RateLimitDecision.permit();
    }

    private void cleanupIfNecessary(long now) {
        if (windows.size() > CLEANUP_THRESHOLD) {
            windows.entrySet().removeIf(entry -> now >= entry.getValue().resetAt());
        }
    }

    private record Window(long resetAt, int count) {
    }
}