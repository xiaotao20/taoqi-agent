package com.taoqi.chat.controller;

import com.taoqi.chat.facade.EmailClientFacade;
import com.taoqi.chat.handler.GlobalExceptionHandler;
import com.taoqi.common.aspect.RateLimitAspect;
import com.taoqi.common.exception.RateLimitExceededException;
import com.taoqi.common.ratelimit.InMemoryRateLimitStore;
import com.taoqi.common.result.R;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 验证 {@code @RateLimit} 在聊天接口上的实际效果及限流异常处理。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerRateLimitTest {

    @Mock
    private EmailClientFacade emailClientFacade;

    @InjectMocks
    private ChatController controller;

    @Test
    void blocksCallsOverLimit() {
        when(emailClientFacade.sendTestEmail(any())).thenReturn(EmailSubmitResult.accepted(1L));
        ChatController proxy = proxy(controller);

        for (int i = 0; i < 5; i++) {
            assertThat(proxy.sendTestEmail("someone@qq.com").data().recordId()).isEqualTo(1L);
        }

        assertThatThrownBy(() -> proxy.sendTestEmail("someone@qq.com"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void convertsRateLimitExceptionTo429() {
        ResponseEntity<R<Void>> response = new GlobalExceptionHandler()
                .handleRateLimitExceeded(new RateLimitExceededException("请求过于频繁", 30));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("30");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().code()).isEqualTo(429);
        assertThat(response.getBody().message()).isEqualTo("请求过于频繁");
    }

    private static ChatController proxy(ChatController target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new RateLimitAspect(new InMemoryRateLimitStore()));
        return factory.getProxy();
    }
}