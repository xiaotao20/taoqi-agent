package com.taoqi.chat.cache;

import java.time.Duration;
import java.util.List;

import com.taoqi.chat.constant.VerificationCodeConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * 验证码 Redis 存储：多实例共享同一份验证码，校验通过后用 Lua 脚本原子删除。
 *
 * <p>验证码以字符串形式保存，key 形如 {@code taoqi:chat:verification-code:{email}}，由 Redis 负责 5 分钟过期。
 * 只有校验成功才删除验证码，输错一次不会让用户的正确验证码失效。
 * Redis 不可用时异常直接向上抛出，发码接口失败而不是发出一封无法校验的邮件。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
public class RedisVerificationCodeStore implements VerificationCodeStore {

    private static final String KEY_PREFIX = "taoqi:chat:verification-code:";

    private static final Duration TTL = Duration.ofMinutes(VerificationCodeConstants.EXPIRE_MINUTES);

    private static final RedisScript<Long> VERIFY_SCRIPT = new DefaultRedisScript<>("""
            local cached = redis.call('GET', KEYS[1])
            if cached == ARGV[1] then
                redis.call('DEL', KEYS[1])
                return 1
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisVerificationCodeStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(String email, String code) {
        redisTemplate.opsForValue().set(key(email), code, TTL);
    }

    @Override
    public boolean verify(String email, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        Long matched = redisTemplate.execute(VERIFY_SCRIPT, List.of(key(email)), code);
        return matched != null && matched == 1L;
    }

    private String key(String email) {
        return KEY_PREFIX + VerificationCodeStore.normalizeEmail(email);
    }
}