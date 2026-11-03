package com.taoqi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解，基于固定时间窗口统计方法调用次数，超过阈值时抛出
 * {@code RateLimitExceededException}。
 *
 * <p>默认按“方法 + 调用方 IP”限流；指定 {@link #key()} 后按“key + 调用方 IP”限流。
 * 统计信息保存在应用内存中，适用于单机限流；集群限流需要换成 Redis 等集中式存储。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 时间窗口内允许的最大调用次数。
     */
    int limit() default 10;

    /**
     * 时间窗口长度，单位秒。
     */
    int windowSeconds() default 60;

    /**
     * 自定义限流 key 前缀，留空时使用方法签名。
     */
    String key() default "";
}
