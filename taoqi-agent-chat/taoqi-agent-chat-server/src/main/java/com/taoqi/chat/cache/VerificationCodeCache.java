package com.taoqi.chat.cache;

import java.time.Duration;
import java.util.Locale;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.taoqi.chat.constant.VerificationCodeConstants;
import org.springframework.stereotype.Component;

/**
 * 验证码本地缓存：基于 Caffeine 按邮箱保存验证码并设置 5 分钟过期。
 *
 * <p>只适用于单实例部署；多实例部署时需要换成 Redis 等集中式缓存。验证成功后验证码立即失效。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Component
public class VerificationCodeCache {

    private static final int MAX_ENTRIES = 10_000;

    private final Cache<String, String> cache;

    public VerificationCodeCache() {
        this(Ticker.systemTicker());
    }

    /**
     * 允许注入时钟源，便于测试过期逻辑。
     */
    VerificationCodeCache(Ticker ticker) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(VerificationCodeConstants.EXPIRE_MINUTES))
                .maximumSize(MAX_ENTRIES)
                .ticker(ticker)
                .build();
    }

    /**
     * 保存验证码，同一邮箱重新发送会覆盖旧验证码。
     */
    public void put(String email, String code) {
        cache.put(normalize(email), code);
    }

    /**
     * 校验验证码，成功后立即失效。
     */
    public boolean verify(String email, String code) {
        String key = normalize(email);
        String cached = cache.getIfPresent(key);
        if (cached == null || !cached.equals(code)) {
            return false;
        }
        cache.invalidate(key);
        return true;
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}