package com.taoqi.starter.accesslog;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证自动配置的生效条件、配置绑定与退让行为。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
class TaoqiAccessLogAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TaoqiAccessLogAutoConfiguration.class));

    @Test
    void registersTraceIdFilterByDefault() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(TraceIdFilter.class));
    }

    @Test
    void notRegisteredWhenDisabled() {
        contextRunner.withPropertyValues("taoqi.access-log.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(TraceIdFilter.class));
    }

    @Test
    void bindsCustomProperties() {
        contextRunner.withPropertyValues(
                        "taoqi.access-log.header-name=X-Request-Id",
                        "taoqi.access-log.mdc-key=requestId",
                        "taoqi.access-log.slow-threshold=2s",
                        "taoqi.access-log.exclude-path-patterns=/actuator/**,/health")
                .run(context -> {
                    AccessLogProperties properties = context.getBean(AccessLogProperties.class);
                    assertThat(properties.getHeaderName()).isEqualTo("X-Request-Id");
                    assertThat(properties.getMdcKey()).isEqualTo("requestId");
                    assertThat(properties.getSlowThreshold()).isEqualTo(Duration.ofSeconds(2));
                    assertThat(properties.getExcludePathPatterns()).containsExactly("/actuator/**", "/health");
                });
    }

    @Test
    void backsOffWhenUserDefinesOwnFilter() {
        contextRunner.withUserConfiguration(CustomFilterConfiguration.class)
                .run(context -> assertThat(context.getBean(TraceIdFilter.class))
                        .isInstanceOf(CustomTraceIdFilter.class));
    }

    @Test
    void notRegisteredForNonWebApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(TaoqiAccessLogAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(TraceIdFilter.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomFilterConfiguration {

        @Bean
        TraceIdFilter customTraceIdFilter() {
            return new CustomTraceIdFilter(new AccessLogProperties());
        }
    }

    static class CustomTraceIdFilter extends TraceIdFilter {

        CustomTraceIdFilter(AccessLogProperties properties) {
            super(properties);
        }
    }
}
