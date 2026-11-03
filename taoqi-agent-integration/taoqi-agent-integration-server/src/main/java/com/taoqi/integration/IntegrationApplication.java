package com.taoqi.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 第三方服务接入模块的启动类。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class IntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }
}
