package com.taoqi.common.aspect;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.taoqi.common.annotation.RateLimit;
import com.taoqi.common.exception.RateLimitExceededException;
import com.taoqi.common.ratelimit.InMemoryRateLimitStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code @RateLimit} 切面的单元测试：验证窗口内放行、超限拒绝和窗口过期后重置。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
class RateLimitAspectTest {

    @Test
    void allowsCallsWithinLimit() {
        SampleService service = proxy(new SampleService(), new RateLimitAspect(new InMemoryRateLimitStore()));

        assertThat(service.query()).isEqualTo("ok");
        assertThat(service.query()).isEqualTo("ok");
    }

    @Test
    void rejectsCallsOverLimit() {
        SampleService service = proxy(new SampleService(), new RateLimitAspect(new InMemoryRateLimitStore()));

        service.query();
        service.query();

        assertThatThrownBy(service::query)
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("秒后重试");
    }

    @Test
    void resetsAfterWindowExpires() {
        AtomicLong now = new AtomicLong(0);
        RateLimitAspect aspect = new RateLimitAspect(new InMemoryRateLimitStore(now::get));
        SampleService service = proxy(new SampleService(), aspect);

        service.query();
        service.query();
        assertThatThrownBy(service::query).isInstanceOf(RateLimitExceededException.class);

        now.addAndGet(60_000);

        assertThat(service.query()).isEqualTo("ok");
    }

    @Test
    void limitsDifferentKeysIndependently() {
        SampleService service = proxy(new SampleService(), new RateLimitAspect(new InMemoryRateLimitStore()));

        service.query();
        service.query();
        assertThatThrownBy(service::query).isInstanceOf(RateLimitExceededException.class);

        assertThat(service.other()).isEqualTo("ok");
    }

    private static SampleService proxy(SampleService target, RateLimitAspect aspect) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    static class SampleService {

        @RateLimit(limit = 2, windowSeconds = 60, key = "sample-query")
        public String query() {
            return "ok";
        }

        @RateLimit(limit = 2, windowSeconds = 60, key = "sample-other")
        public String other() {
            return "ok";
        }
    }
}