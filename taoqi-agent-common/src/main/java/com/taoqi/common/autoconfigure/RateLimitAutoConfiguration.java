package com.taoqi.common.autoconfigure;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;

import com.taoqi.common.aspect.RateLimitAspect;

/**
 * {@code @RateLimit} 的自动配置：引入 common 模块后自动注册限流切面，无需在启动类上额外声明。
 *
 * <p>通过 {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * 注册；类路径缺少 AspectJ 或 Web 相关类时自动跳过，使用方自行声明切面时退让。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@AutoConfiguration
@ConditionalOnClass({Aspect.class, RequestContextHolder.class})
public class RateLimitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect() {
        return new RateLimitAspect();
    }
}
