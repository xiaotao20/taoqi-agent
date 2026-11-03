package com.taoqi.starter.accesslog;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 访问日志与 traceId 的配置项，前缀为 {@code taoqi.access-log}。
 *
 * <p>所有配置都有默认值，使用方引入 starter 后不写任何配置即可生效；
 * 配置项会由 {@code spring-boot-configuration-processor} 编译成元数据，供 IDE 提示。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ConfigurationProperties(prefix = "taoqi.access-log")
public class AccessLogProperties {

    /** 是否启用访问日志过滤器。 */
    private boolean enabled = true;

    /** 透传 traceId 的请求头与响应头名称。 */
    private String headerName = "X-Trace-Id";

    /** traceId 写入 MDC 时使用的键，可在日志格式中用 %X{traceId} 引用。 */
    private String mdcKey = "traceId";

    /** 超过该耗时的请求按慢请求打印 WARN 日志。 */
    private Duration slowThreshold = Duration.ofMillis(1000);

    /** 不记录访问日志的路径，支持 Ant 风格，如 /actuator/**。 */
    private List<String> excludePathPatterns = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getMdcKey() {
        return mdcKey;
    }

    public void setMdcKey(String mdcKey) {
        this.mdcKey = mdcKey;
    }

    public Duration getSlowThreshold() {
        return slowThreshold;
    }

    public void setSlowThreshold(Duration slowThreshold) {
        this.slowThreshold = slowThreshold;
    }

    public List<String> getExcludePathPatterns() {
        return excludePathPatterns;
    }

    public void setExcludePathPatterns(List<String> excludePathPatterns) {
        this.excludePathPatterns = excludePathPatterns;
    }
}
