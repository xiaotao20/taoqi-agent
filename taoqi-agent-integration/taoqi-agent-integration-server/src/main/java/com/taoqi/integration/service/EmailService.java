package com.taoqi.integration.service;

import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSubmitResult;

/**
 * 邮件发送服务接口：提交发送任务，实际发送异步执行。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public interface EmailService {

    /**
     * 提交邮件发送任务，先落一条状态为 {@code SENDING} 的记录，再交给线程池发送。
     */
    EmailSubmitResult submit(EmailSendRequest request);
}
