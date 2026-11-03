package com.taoqi.integration.service;

import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSendResult;

/**
 * 邮件发送服务接口。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public interface EmailService {

    EmailSendResult send(EmailSendRequest request);
}
