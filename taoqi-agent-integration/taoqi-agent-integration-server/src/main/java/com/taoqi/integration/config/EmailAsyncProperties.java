package com.taoqi.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 邮件异步发送线程池配置，前缀为 {@code taoqi.email.async}。
 *
 * @param corePoolSize  核心线程数
 * @param maxPoolSize   最大线程数
 * @param queueCapacity 任务队列容量
 * @author xiaotao
 * @date 2026-11-03
 */
@ConfigurationProperties(prefix = "taoqi.email.async")
public record EmailAsyncProperties(Integer corePoolSize, Integer maxPoolSize, Integer queueCapacity) {

    public EmailAsyncProperties {
        corePoolSize = corePoolSize == null || corePoolSize < 1 ? 2 : corePoolSize;
        maxPoolSize = maxPoolSize == null || maxPoolSize < corePoolSize ? Math.max(corePoolSize, 4) : maxPoolSize;
        queueCapacity = queueCapacity == null || queueCapacity < 0 ? 100 : queueCapacity;
    }
}
