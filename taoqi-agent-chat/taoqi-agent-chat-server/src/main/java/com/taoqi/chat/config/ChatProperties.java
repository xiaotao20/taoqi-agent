package com.taoqi.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 聊天模块配置。
 *
 * <p>使用可变类是为了配合 {@code @RefreshScope}：Spring Cloud 通过 CGLIB 子类代理实现动态刷新，
 * record 是 final 类无法被代理，因此这里保留无参构造与 setter。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@RefreshScope
@ConfigurationProperties(prefix = "taoqi.chat")
public class ChatProperties {

    /**
     * 测试接口的默认收件人地址。
     */
    private String testEmailTo;

    public String getTestEmailTo() {
        return testEmailTo;
    }

    public void setTestEmailTo(String testEmailTo) {
        this.testEmailTo = testEmailTo;
    }
}