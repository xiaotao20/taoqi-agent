package com.taoqi.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 邮件发送相关配置。
 *
 * <p>发件人信息在每次发送时读取，配合 {@code @RefreshScope} 可在 Nacos 修改后动态生效；
 * CGLIB 代理要求目标类不是 final，因此使用可变类而不是 record。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@RefreshScope
@ConfigurationProperties(prefix = "taoqi.mail")
public class MailProperties {

    /**
     * 发件人邮箱地址。
     */
    private String from;

    /**
     * 发件人显示名称。
     */
    private String personal;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }
}