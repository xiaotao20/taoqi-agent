package com.taoqi.integration.client;

import com.taoqi.integration.api.EmailApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 邮件服务 Feign 客户端。
 *
 * <p>其它服务引入 {@code taoqi-agent-integration-api} 依赖后，在启动类或配置类上添加
 * {@code @EnableFeignClients(basePackages = "com.taoqi.integration.client")}，
 * 即可注入 {@code EmailFeignClient} 调用邮件服务。
 */
@FeignClient(name = "taoqi-agent-integration", url = "${taoqi.integration.base-url:http://localhost:8081}")
public interface EmailFeignClient extends EmailApi {
}
