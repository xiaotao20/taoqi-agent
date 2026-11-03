package com.taoqi.starter.accesslog;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@code taoqi-agent-starter} 的自动配置入口，通过
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} 注册。
 *
 * <p>仅在 Servlet Web 应用中且 {@code taoqi.access-log.enabled} 不为 false 时生效；
 * 使用方自行声明了 {@code TraceIdFilter} 时自动退让，便于覆盖默认实现。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(OncePerRequestFilter.class)
@ConditionalOnProperty(prefix = "taoqi.access-log", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AccessLogProperties.class)
public class TaoqiAccessLogAutoConfiguration {

    /**
     * 注册过滤器，优先级设为最高，保证 traceId 能覆盖后续所有过滤器和业务日志。
     */
    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public TraceIdFilter traceIdFilter(AccessLogProperties properties) {
        return new TraceIdFilter(properties);
    }
}
