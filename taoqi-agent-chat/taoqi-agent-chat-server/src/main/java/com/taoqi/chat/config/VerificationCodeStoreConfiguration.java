package com.taoqi.chat.config;

import com.taoqi.chat.cache.InMemoryVerificationCodeStore;
import com.taoqi.chat.cache.RedisVerificationCodeStore;
import com.taoqi.chat.cache.VerificationCodeStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 验证码存储的装配：按 {@code taoqi.chat.verification-code.store-type} 选择 Redis 或内存实现，默认 Redis。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@Configuration
public class VerificationCodeStoreConfiguration {

    /**
     * 单实例或测试环境的内存实现。
     */
    @Bean
    @ConditionalOnProperty(name = "taoqi.chat.verification-code.store-type", havingValue = "memory")
    public VerificationCodeStore inMemoryVerificationCodeStore() {
        return new InMemoryVerificationCodeStore();
    }

    /**
     * 多实例共享的 Redis 实现，未配置 store-type 时默认使用。
     */
    @Bean
    @ConditionalOnProperty(name = "taoqi.chat.verification-code.store-type",
            havingValue = "redis", matchIfMissing = true)
    public VerificationCodeStore redisVerificationCodeStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisVerificationCodeStore(stringRedisTemplate);
    }
}