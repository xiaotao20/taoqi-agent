package com.taoqi.integration.api;

import jakarta.validation.Valid;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 邮件服务的接口契约：服务端 Controller 实现它，其它服务通过 Feign 客户端调用它。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public interface EmailApi {

    /**
     * 发送邮件。
     */
    @PostMapping("/api/email/send")
    EmailSendResult send(@Valid @RequestBody EmailSendRequest request);
}
