package com.taoqi.chat.cache;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.taoqi.chat.constant.VerificationCodeConstants;

/**
 * 验证码内存存储：基于 Caffeine 按邮箱保存验证码并设置 5 分钟过期。
 *
 * <p>只适用于单实例部署，生产多实例环境使用 {@code RedisVerificationCodeStore}；测试和本地无 Redis 时使用本实现。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
public class InMemoryVerificationCodeStore implements VerificationCodeStore {

    private static final int MAX_ENTRIES = 10_000;

    private final Cache<String, String> cache;

    public InMemoryVerificationCodeStore() {
        this(Ticker.systemTicker());
    }

    /**
     * 允许注入时钟源，便于测试过期逻辑。
     */
    InMemoryVerificationCodeStore(Ticker ticker) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(VerificationCodeConstants.EXPIRE_MINUTES))
                .maximumSize(MAX_ENTRIES)
                .ticker(ticker)
                .build();
    }

    @Override
    public void put(String email, String code) {
        cache.put(VerificationCodeStore.normalizeEmail(email), code);
    }

    @Override
    public boolean verify(String email, String code) {
        String key = VerificationCodeStore.normalizeEmail(email);
        String cached = cache.getIfPresent(key);
        if (cached == null || !cached.equals(code)) {
            return false;
        }
        cache.invalidate(key);
        return true;
    }
}