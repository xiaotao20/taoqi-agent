package com.taoqi.common.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.taoqi.common.aspect.RateLimitAspect;
import com.taoqi.common.ratelimit.InMemoryRateLimitStore;
import com.taoqi.common.ratelimit.RateLimitStore;
import com.taoqi.common.ratelimit.RedisRateLimitStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 验证限流切面与存储实现的自动配置、配置切换与退让行为。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
class RateLimitAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RateLimitAutoConfiguration.class));

    @Test
    void registersInMemoryStoreByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RateLimitAspect.class);
            assertThat(context.getBean(RateLimitStore.class)).isInstanceOf(InMemoryRateLimitStore.class);
        });
    }

    @Test
    void switchesToRedisStore() {
        contextRunner.withPropertyValues("taoqi.rate-limit.store-type=redis")
                .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
                .run(context -> assertThat(context.getBean(RateLimitStore.class))
                        .isInstanceOf(RedisRateLimitStore.class));
    }

    @Test
    void backsOffWhenUserDefinesOwnAspect() {
        contextRunner.withBean("customRateLimitAspect", RateLimitAspect.class,
                        () -> new RateLimitAspect(new InMemoryRateLimitStore()))
                .run(context -> assertThat(context).hasSingleBean(RateLimitAspect.class));
    }
}