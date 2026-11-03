package com.taoqi.common.autoconfigure;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.context.request.RequestContextHolder;

import com.taoqi.common.aspect.RateLimitAspect;
import com.taoqi.common.ratelimit.InMemoryRateLimitStore;
import com.taoqi.common.ratelimit.RateLimitProperties;
import com.taoqi.common.ratelimit.RateLimitStore;
import com.taoqi.common.ratelimit.RedisRateLimitStore;

/**
 * {@code @RateLimit} 的自动配置：引入 common 模块后自动注册限流切面与存储实现。
 *
 * <p>存储实现通过 {@code taoqi.rate-limit.store-type} 切换：默认 {@code memory}（单机），
 * 设为 {@code redis} 时使用 Redis 共享计数（需要运行时引入 Redis starter）。
 * 使用方自行声明 {@code RateLimitStore} 或 {@code RateLimitAspect} 时自动退让。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@AutoConfiguration
@ConditionalOnClass({Aspect.class, RequestContextHolder.class})
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "taoqi.rate-limit", name = "store-type",
            havingValue = "memory", matchIfMissing = true)
    public RateLimitStore inMemoryRateLimitStore() {
        return new InMemoryRateLimitStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect(RateLimitStore rateLimitStore) {
        return new RateLimitAspect(rateLimitStore);
    }

    /**
     * Redis 存储配置：只有在选择 redis 模式且类路径存在 Spring Data Redis 时生效。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({StringRedisTemplate.class, DefaultRedisScript.class})
    @ConditionalOnProperty(prefix = "taoqi.rate-limit", name = "store-type", havingValue = "redis")
    static class RedisRateLimitStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public RateLimitStore redisRateLimitStore(StringRedisTemplate stringRedisTemplate) {
            return new RedisRateLimitStore(stringRedisTemplate);
        }
    }
}