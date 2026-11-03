package com.taoqi.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 聊天模块配置。
 *
 * @param testEmailTo 测试接口的默认收件人地址
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ConfigurationProperties(prefix = "taoqi.chat")
public record ChatProperties(String testEmailTo) {
}
