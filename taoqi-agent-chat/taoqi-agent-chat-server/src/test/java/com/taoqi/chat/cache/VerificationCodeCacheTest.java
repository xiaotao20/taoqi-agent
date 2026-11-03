package com.taoqi.chat.cache;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code VerificationCodeCache} 的单元测试：验证校验、覆盖、一次性使用与 5 分钟过期。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
class VerificationCodeCacheTest {

    private final AtomicLong nanos = new AtomicLong();
    private final Ticker ticker = nanos::get;
    private final VerificationCodeCache cache = new VerificationCodeCache(ticker);

    @Test
    void verifiesStoredCode() {
        cache.put("user@example.com", "123456");

        assertThat(cache.verify("user@example.com", "123456")).isTrue();
    }

    @Test
    void codeCanOnlyBeUsedOnce() {
        cache.put("user@example.com", "123456");

        assertThat(cache.verify("user@example.com", "123456")).isTrue();
        assertThat(cache.verify("user@example.com", "123456")).isFalse();
    }

    @Test
    void rejectsWrongCode() {
        cache.put("user@example.com", "123456");

        assertThat(cache.verify("user@example.com", "654321")).isFalse();
    }

    @Test
    void expiresAfterFiveMinutes() {
        cache.put("user@example.com", "123456");

        nanos.addAndGet(Duration.ofMinutes(5).toNanos());

        assertThat(cache.verify("user@example.com", "123456")).isFalse();
    }

    @Test
    void resendOverwritesOldCode() {
        cache.put("user@example.com", "111111");
        cache.put("user@example.com", "222222");

        assertThat(cache.verify("user@example.com", "111111")).isFalse();
        assertThat(cache.verify("user@example.com", "222222")).isTrue();
    }

    @Test
    void emailIsCaseInsensitive() {
        cache.put("User@Example.com", "123456");

        assertThat(cache.verify("user@example.com", "123456")).isTrue();
    }
}