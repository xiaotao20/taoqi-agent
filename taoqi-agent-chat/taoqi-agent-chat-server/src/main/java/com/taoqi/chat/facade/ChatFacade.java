package com.taoqi.chat.facade;

import com.taoqi.integration.api.dto.EmailSendResult;

/**
 * 聊天模块门面：对 controller 暴露业务能力，内部封装对远程服务的调用。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public interface ChatFacade {

    /**
     * 发送测试邮件（通过 Feign 调用邮件服务）。
     *
     * @param to 收件人地址，为空时使用配置的默认收件人
     */
    EmailSendResult sendTestEmail(String to);
}
