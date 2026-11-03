package com.taoqi.integration.client;

import feign.MethodMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmailFeignClientTest {

    @Test
    @DisplayName("Feign 契约解析出的请求路径与 HTTP 方法正确")
    void contractResolvesSendEndpoint() {
        List<MethodMetadata> metadata = new SpringMvcContract().parseAndValidateMetadata(EmailFeignClient.class);

        assertThat(metadata).hasSize(1);
        MethodMetadata send = metadata.get(0);
        assertThat(send.template().method()).isEqualTo("POST");
        assertThat(send.template().url()).isEqualTo("/api/email/send");
    }
}
