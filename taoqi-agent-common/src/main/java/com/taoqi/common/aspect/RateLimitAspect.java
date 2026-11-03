package com.taoqi.common.aspect;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.taoqi.common.annotation.RateLimit;
import com.taoqi.common.exception.RateLimitExceededException;
import com.taoqi.common.ratelimit.RateLimitDecision;
import com.taoqi.common.ratelimit.RateLimitStore;

/**
 * {@code @RateLimit} 的切面实现：把限流判断委托给 {@code RateLimitStore}，超过阈值直接抛异常。
 *
 * <p>key 由方法签名（或注解上的 {@code key}）与调用方 IP 拼成，避免不同接口、不同客户端互相影响。
 * 单机存储与 Redis 存储通过配置切换，切面本身不感知具体实现。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Aspect
public class RateLimitAspect {

    private final RateLimitStore rateLimitStore;

    public RateLimitAspect(RateLimitStore rateLimitStore) {
        this.rateLimitStore = rateLimitStore;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);
        Duration window = Duration.ofSeconds(Math.max(1, rateLimit.windowSeconds()));

        RateLimitDecision decision = rateLimitStore.tryAcquire(key, rateLimit.limit(), window);
        if (!decision.allowed()) {
            throw new RateLimitExceededException(
                    "请求过于频繁，请 " + decision.retryAfterSeconds() + " 秒后重试",
                    decision.retryAfterSeconds());
        }
        return joinPoint.proceed();
    }

    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        String prefix = StringUtils.hasText(rateLimit.key())
                ? rateLimit.key()
                : joinPoint.getSignature().toShortString();
        return prefix + "#" + clientIp();
    }

    /**
     * 取调用方 IP：优先使用代理转发的 X-Forwarded-For，非 Web 调用返回 unknown。
     */
    private String clientIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(forwardedFor)) {
                return forwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "unknown";
    }
}