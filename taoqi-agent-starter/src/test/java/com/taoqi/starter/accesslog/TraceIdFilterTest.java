package com.taoqi.starter.accesslog;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 traceId 的生成、透传、MDC 清理与排除路径。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
class TraceIdFilterTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void generatesTraceIdAndClearsMdcAfterRequest() throws Exception {
        TraceIdFilter filter = new TraceIdFilter(new AccessLogProperties());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/chat/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (req, res) -> assertThat(MDC.get("traceId")).hasSize(16));

        assertThat(response.getHeader("X-Trace-Id")).hasSize(16);
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void reusesIncomingTraceId() throws Exception {
        TraceIdFilter filter = new TraceIdFilter(new AccessLogProperties());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/chat/ping");
        request.addHeader("X-Trace-Id", "frontend-abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (req, res) -> assertThat(MDC.get("traceId")).isEqualTo("frontend-abc"));

        assertThat(response.getHeader("X-Trace-Id")).isEqualTo("frontend-abc");
    }

    @Test
    void usesConfiguredHeaderNameAndMdcKey() throws Exception {
        AccessLogProperties properties = new AccessLogProperties();
        properties.setHeaderName("X-Request-Id");
        properties.setMdcKey("requestId");
        TraceIdFilter filter = new TraceIdFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/chat/ping");
        request.addHeader("X-Request-Id", "custom-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (req, res) -> assertThat(MDC.get("requestId")).isEqualTo("custom-123"));

        assertThat(response.getHeader("X-Request-Id")).isEqualTo("custom-123");
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void skipsExcludedPaths() throws Exception {
        AccessLogProperties properties = new AccessLogProperties();
        properties.setExcludePathPatterns(List.of("/actuator/**"));
        TraceIdFilter filter = new TraceIdFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (req, res) -> assertThat(MDC.get("traceId")).isNull());

        assertThat(response.getHeader("X-Trace-Id")).isNull();
    }
}
