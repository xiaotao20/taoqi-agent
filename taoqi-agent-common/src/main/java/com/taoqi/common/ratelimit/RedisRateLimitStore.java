package com.taoqi.common.ratelimit;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * 基于 Redis 的限流存储，所有实例共享同一份计数。
 *
 * <p>通过 Lua 脚本原子完成“自增 + 首次设置过期时间 + 读取剩余时间”。
 * Redis 不可用时采取放行策略（fail-open），避免限流组件拖垮业务。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public class RedisRateLimitStore implements RateLimitStore {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitStore.class);

    private static final String SCRIPT_TEXT = """
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('PEXPIRE', KEYS[1], ARGV[2])
            end
            local ttl = redis.call('PTTL', KEYS[1])
            if ttl < 0 then
                redis.call('PEXPIRE', KEYS[1], ARGV[2])
                ttl = tonumber(ARGV[2])
            end
            if current > tonumber(ARGV[1]) then
                return {0, ttl}
            end
            return {1, ttl}
            """;

    @SuppressWarnings("rawtypes")
    private static final RedisScript<List> SCRIPT = new DefaultRedisScript<>(SCRIPT_TEXT, List.class);

    private final StringRedisTemplate redisTemplate;

    public RedisRateLimitStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RateLimitDecision tryAcquire(String key, int limit, Duration window) {
        long windowMillis = Math.max(1, window.toMillis());
        try {
            List<Object> result = (List<Object>) redisTemplate.execute(
                    SCRIPT, List.of(key), String.valueOf(Math.max(1, limit)), String.valueOf(windowMillis));
            if (result == null || result.size() < 2) {
                return RateLimitDecision.permit();
            }
            long allowed = ((Number) result.get(0)).longValue();
            long ttlMillis = ((Number) result.get(1)).longValue();
            if (allowed == 1) {
                return RateLimitDecision.permit();
            }
            return RateLimitDecision.deny((ttlMillis + 999) / 1000);
        } catch (RuntimeException e) {
            log.warn("Redis 限流执行失败，放行本次请求, key={}", key, e);
            return RateLimitDecision.permit();
        }
    }
}