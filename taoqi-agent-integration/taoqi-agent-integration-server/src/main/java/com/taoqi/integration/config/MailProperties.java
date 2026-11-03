package com.taoqi.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 邮件发送相关配置。
 *
 * @param from     发件人邮箱地址
 * @param personal 发件人显示名称
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ConfigurationProperties(prefix = "taoqi.mail")
public record MailProperties(String from, String personal) {
}
