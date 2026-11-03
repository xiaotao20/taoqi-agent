package com.taoqi.chat.facade.impl;

import com.taoqi.chat.config.ChatProperties;
import com.taoqi.common.result.R;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;
import com.taoqi.integration.api.enums.EmailSendStatus;
import com.taoqi.integration.client.EmailFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code EmailClientFacadeImpl} 的单元测试：验证收件人逻辑与 {@code R} 的拆包。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@ExtendWith(MockitoExtension.class)
class EmailClientFacadeImplTest {

    @Mock
    private EmailFeignClient emailFeignClient;

    @Mock
    private ChatProperties chatProperties;

    @InjectMocks
    private EmailClientFacadeImpl emailClientFacade;

    @Test
    @DisplayName("未传 to 时使用配置的默认收件人")
    void usesConfiguredRecipientWhenToMissing() {
        when(chatProperties.testEmailTo()).thenReturn("default@qq.com");
        when(emailFeignClient.send(any())).thenReturn(R.data(EmailSubmitResult.accepted(1L)));

        EmailSubmitResult result = emailClientFacade.sendTestEmail(null);

        assertThat(result.status()).isEqualTo(EmailSendStatus.SENDING);
        assertThat(captureRequest().to()).containsExactly("default@qq.com");
    }

    @Test
    @DisplayName("传入 to 时使用指定收件人")
    void usesRequestedRecipient() {
        when(emailFeignClient.send(any())).thenReturn(R.data(EmailSubmitResult.accepted(1L)));

        emailClientFacade.sendTestEmail("other@qq.com");

        assertThat(captureRequest().to()).containsExactly("other@qq.com");
    }

    @Test
    @DisplayName("取出 R 中的业务数据")
    void unwrapsResponseData() {
        when(emailFeignClient.send(any())).thenReturn(R.data(EmailSubmitResult.accepted(88L)));

        EmailSubmitResult result = emailClientFacade.sendTestEmail("other@qq.com");

        assertThat(result.recordId()).isEqualTo(88L);
        assertThat(result.status()).isEqualTo(EmailSendStatus.SENDING);
    }

    @Test
    @DisplayName("远程调用没有数据时返回失败结果")
    void returnsFailureWhenResponseHasNoData() {
        when(emailFeignClient.send(any())).thenReturn(R.fail("邮件服务异常"));

        EmailSubmitResult result = emailClientFacade.sendTestEmail("other@qq.com");

        assertThat(result.status()).isEqualTo(EmailSendStatus.FAILED);
        assertThat(result.recordId()).isNull();
        assertThat(result.message()).isEqualTo("邮件服务异常");
    }

    private EmailSendRequest captureRequest() {
        ArgumentCaptor<EmailSendRequest> captor = ArgumentCaptor.forClass(EmailSendRequest.class);
        verify(emailFeignClient).send(captor.capture());
        return captor.getValue();
    }
}