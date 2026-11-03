package com.taoqi.common.ratelimit;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code InMemoryRateLimitStore} 的单元测试：验证固定窗口的放行、拒绝与重置。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
class InMemoryRateLimitStoreTest {

    private final AtomicLong now = new AtomicLong(0);
    private final InMemoryRateLimitStore store = new InMemoryRateLimitStore(now::get);
    private final Duration window = Duration.ofSeconds(60);

    @Test
    void allowsUpToLimit() {
        assertThat(store.tryAcquire("k", 2, window).allowed()).isTrue();
        assertThat(store.tryAcquire("k", 2, window).allowed()).isTrue();
    }

    @Test
    void rejectsOverLimitAndReportsRetryAfter() {
        store.tryAcquire("k", 2, window);
        store.tryAcquire("k", 2, window);

        RateLimitDecision decision = store.tryAcquire("k", 2, window);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.retryAfterSeconds()).isEqualTo(60);
    }

    @Test
    void resetsAfterWindowExpires() {
        store.tryAcquire("k", 1, window);
        assertThat(store.tryAcquire("k", 1, window).allowed()).isFalse();

        now.addAndGet(60_000);

        assertThat(store.tryAcquire("k", 1, window).allowed()).isTrue();
    }

    @Test
    void keysAreIndependent() {
        store.tryAcquire("a", 1, window);

        assertThat(store.tryAcquire("a", 1, window).allowed()).isFalse();
        assertThat(store.tryAcquire("b", 1, window).allowed()).isTrue();
    }
}