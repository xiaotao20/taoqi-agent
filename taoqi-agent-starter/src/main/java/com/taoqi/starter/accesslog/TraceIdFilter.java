package com.taoqi.starter.accesslog;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 请求级 traceId 与访问日志过滤器。
 *
 * <p>每次请求优先复用请求头中携带的 traceId（便于跨服务串联），没有则生成 16 位十六进制 ID，
 * 写入 MDC 并回写到响应头；请求结束后打印方法、路径、状态码与耗时，超过阈值时降级为 WARN 慢请求日志。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public class TraceIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);

    private final AccessLogProperties properties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public TraceIdFilter(AccessLogProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return properties.getExcludePathPatterns().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        MDC.put(properties.getMdcKey(), traceId);
        response.setHeader(properties.getHeaderName(), traceId);

        long startNanos = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        }
        finally {
            long costMillis = (System.nanoTime() - startNanos) / 1_000_000;
            logRequest(request, response, traceId, costMillis);
            MDC.remove(properties.getMdcKey());
        }
    }

    /**
     * 优先使用调用方传入的 traceId，没有则生成一个新的。
     */
    private String resolveTraceId(HttpServletRequest request) {
        String incoming = request.getHeader(properties.getHeaderName());
        if (StringUtils.hasText(incoming)) {
            return incoming.trim();
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private void logRequest(HttpServletRequest request, HttpServletResponse response,
            String traceId, long costMillis) {
        if (costMillis >= properties.getSlowThreshold().toMillis()) {
            log.warn("慢请求 {} {} -> {}，耗时 {} ms（阈值 {} ms），traceId={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), costMillis,
                    properties.getSlowThreshold().toMillis(), traceId);
        }
        else {
            log.info("{} {} -> {}，耗时 {} ms，traceId={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), costMillis, traceId);
        }
    }
}
