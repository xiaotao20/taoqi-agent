package com.taoqi.common.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.taoqi.common.aspect.RateLimitAspect;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证限流切面的自动配置与退让行为。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
class RateLimitAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RateLimitAutoConfiguration.class));

    @Test
    void registersRateLimitAspect() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(RateLimitAspect.class));
    }

    @Test
    void backsOffWhenUserDefinesOwnAspect() {
        contextRunner.withBean("customRateLimitAspect", RateLimitAspect.class, RateLimitAspect::new)
                .run(context -> assertThat(context).hasSingleBean(RateLimitAspect.class));
    }
}
