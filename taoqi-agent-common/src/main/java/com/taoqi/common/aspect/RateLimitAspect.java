package com.taoqi.common.aspect;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.LongSupplier;

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

/**
 * {@code @RateLimit} 的切面实现：固定时间窗口计数，超过阈值直接抛异常，不执行目标方法。
 *
 * <p>每个限流 key 在内存中保存“窗口重置时间 + 当前计数”，窗口到期后自动从 1 重新计数；
 * key 由方法签名（或注解上的 {@code key}）与调用方 IP 拼成，避免不同接口、不同客户端互相影响。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@Aspect
public class RateLimitAspect {

    /**
     * key 数量超过该值时清理已过期的窗口，防止内存无限增长。
     */
    private static final int CLEANUP_THRESHOLD = 1024;

    private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();

    private final LongSupplier clock;

    public RateLimitAspect() {
        this(System::currentTimeMillis);
    }

    /**
     * 允许注入时钟，便于测试窗口过期逻辑。
     */
    RateLimitAspect(LongSupplier clock) {
        this.clock = clock;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        long now = clock.getAsLong();
        long windowMillis = Math.max(1, rateLimit.windowSeconds()) * 1000L;
        int limit = Math.max(1, rateLimit.limit());
        String key = buildKey(joinPoint, rateLimit);

        Window window = windows.compute(key, (k, current) -> {
            if (current == null || now >= current.resetAt()) {
                return new Window(now + windowMillis, 1);
            }
            return new Window(current.resetAt(), current.count() + 1);
        });

        if (window.count() > limit) {
            long retryAfterSeconds = Math.max(1, (window.resetAt() - now + 999) / 1000);
            throw new RateLimitExceededException(
                    "请求过于频繁，请 " + retryAfterSeconds + " 秒后重试", retryAfterSeconds);
        }

        cleanupIfNecessary(now);
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

    private void cleanupIfNecessary(long now) {
        if (windows.size() > CLEANUP_THRESHOLD) {
            windows.entrySet().removeIf(entry -> now >= entry.getValue().resetAt());
        }
    }

    private record Window(long resetAt, int count) {
    }
}
