package com.taoqi.common.ratelimit;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

/**
 * {@code RedisRateLimitStore} 的单元测试：验证 Lua 脚本结果的解析与 Redis 故障时的放行策略。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ExtendWith(MockitoExtension.class)
class RedisRateLimitStoreTest {

    private static final Duration WINDOW = Duration.ofSeconds(60);

    @Mock
    private StringRedisTemplate redisTemplate;

    private RedisRateLimitStore store;

    @BeforeEach
    void setUp() {
        store = new RedisRateLimitStore(redisTemplate);
    }

    @Test
    void allowsWhenScriptReturnsAllowed() {
        doReturn(List.of(1L, 60_000L))
                .when(redisTemplate).execute(any(RedisScript.class), anyList(), any(), any());

        RateLimitDecision decision = store.tryAcquire("k", 10, WINDOW);

        assertThat(decision.allowed()).isTrue();
    }

    @Test
    void rejectsWhenScriptReturnsDenied() {
        doReturn(List.of(0L, 30_000L))
                .when(redisTemplate).execute(any(RedisScript.class), anyList(), any(), any());

        RateLimitDecision decision = store.tryAcquire("k", 10, WINDOW);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.retryAfterSeconds()).isEqualTo(30);
    }

    @Test
    void failsOpenWhenRedisUnavailable() {
        doThrow(new RedisConnectionFailureException("redis down"))
                .when(redisTemplate).execute(any(RedisScript.class), anyList(), any(), any());

        RateLimitDecision decision = store.tryAcquire("k", 10, WINDOW);

        assertThat(decision.allowed()).isTrue();
    }
}