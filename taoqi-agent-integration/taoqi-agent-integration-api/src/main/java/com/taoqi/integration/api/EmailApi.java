package com.taoqi.integration.api;

import jakarta.validation.Valid;
import com.taoqi.common.result.R;
import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;
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
     * 提交邮件发送任务：立即返回记录 ID 与状态，实际发送异步完成。
     */
    @PostMapping("/api/integration/email/send")
    R<EmailSubmitResult> send(@Valid @RequestBody EmailSendRequest request);
}
