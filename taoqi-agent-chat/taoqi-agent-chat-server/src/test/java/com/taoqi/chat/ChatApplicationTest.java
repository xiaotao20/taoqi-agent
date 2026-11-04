package com.taoqi.chat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 聊天模块的 Spring 上下文启动测试。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@SpringBootTest(properties = "spring.cloud.nacos.discovery.enabled=false")
class ChatApplicationTest {

    @Test
    void contextLoads() {
    }
}
