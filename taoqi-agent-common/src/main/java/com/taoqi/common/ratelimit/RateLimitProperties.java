package com.taoqi.common.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 限流配置，前缀为 {@code taoqi.rate-limit}。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ConfigurationProperties(prefix = "taoqi.rate-limit")
public class RateLimitProperties {

    /**
     * 限流存储类型：{@code memory}（默认，单机）或 {@code redis}（多实例共享）。
     */
    private StoreType storeType = StoreType.MEMORY;

    public StoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    public enum StoreType {
        MEMORY,
        REDIS
    }
}