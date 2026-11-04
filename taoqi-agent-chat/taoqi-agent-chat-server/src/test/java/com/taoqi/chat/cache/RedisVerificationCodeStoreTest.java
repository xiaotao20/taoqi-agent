package com.taoqi.chat.cache;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@code RedisVerificationCodeStore} 的单元测试：验证 key 规范化、TTL 与 Lua 原子校验。
 *
 * @author xiaotao
 * @date 2026-11-04
 */
@ExtendWith(MockitoExtension.class)
class RedisVerificationCodeStoreTest {

    private static final String KEY = "taoqi:chat:verification-code:user@example.com";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisVerificationCodeStore store;

    @BeforeEach
    void setUp() {
        store = new RedisVerificationCodeStore(redisTemplate);
    }

    @Test
    void storesCodeWithFiveMinuteTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        store.put(" User@Example.com ", "123456");

        verify(valueOperations).set(KEY, "123456", Duration.ofMinutes(5));
    }

    @Test
    void verifiesAndDeletesCode() {
        doReturn(1L).when(redisTemplate).execute(any(RedisScript.class), anyList(), any());

        assertThat(store.verify("user@example.com", "123456")).isTrue();

        verify(redisTemplate).execute(any(RedisScript.class), eq(List.of(KEY)), eq("123456"));
    }

    @Test
    void rejectsWrongCodeWithoutDeleting() {
        doReturn(0L).when(redisTemplate).execute(any(RedisScript.class), anyList(), any());

        assertThat(store.verify("user@example.com", "654321")).isFalse();
    }

    @Test
    void blankCodeDoesNotHitRedis() {
        assertThat(store.verify("user@example.com", " ")).isFalse();

        verifyNoInteractions(redisTemplate);
    }
}