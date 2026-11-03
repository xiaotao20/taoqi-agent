package com.taoqi.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 聊天模块的启动类，扫描 {@code com.taoqi} 下各服务的 Feign 客户端。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "com.taoqi")
public class ChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}
