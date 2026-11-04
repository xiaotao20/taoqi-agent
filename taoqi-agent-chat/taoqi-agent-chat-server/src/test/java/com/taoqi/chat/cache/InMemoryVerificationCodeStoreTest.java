package com.taoqi.chat.cache;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code InMemoryVerificationCodeStore} 的单元测试：验证校验、覆盖、一次性使用与 5 分钟过期。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
class InMemoryVerificationCodeStoreTest {

    private final AtomicLong nanos = new AtomicLong();
    private final Ticker ticker = nanos::get;
    private final InMemoryVerificationCodeStore store = new InMemoryVerificationCodeStore(ticker);

    @Test
    void verifiesStoredCode() {
        store.put("user@example.com", "123456");

        assertThat(store.verify("user@example.com", "123456")).isTrue();
    }

    @Test
    void codeCanOnlyBeUsedOnce() {
        store.put("user@example.com", "123456");

        assertThat(store.verify("user@example.com", "123456")).isTrue();
        assertThat(store.verify("user@example.com", "123456")).isFalse();
    }

    @Test
    void rejectsWrongCode() {
        store.put("user@example.com", "123456");

        assertThat(store.verify("user@example.com", "654321")).isFalse();
    }

    @Test
    void expiresAfterFiveMinutes() {
        store.put("user@example.com", "123456");

        nanos.addAndGet(Duration.ofMinutes(5).toNanos());

        assertThat(store.verify("user@example.com", "123456")).isFalse();
    }

    @Test
    void resendOverwritesOldCode() {
        store.put("user@example.com", "111111");
        store.put("user@example.com", "222222");

        assertThat(store.verify("user@example.com", "111111")).isFalse();
        assertThat(store.verify("user@example.com", "222222")).isTrue();
    }

    @Test
    void emailIsCaseInsensitive() {
        store.put("User@Example.com", "123456");

        assertThat(store.verify("user@example.com", "123456")).isTrue();
    }
}