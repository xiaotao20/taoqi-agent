package com.taoqi.integration.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 邮件发送请求。
 *
 * @param to      收件人列表，不能为空
 * @param cc      抄送列表，可为空
 * @param bcc     密送列表，可为空
 * @param subject 邮件主题
 * @param content 邮件正文
 * @param html    正文是否为 HTML，默认纯文本
 */
public record EmailSendRequest(
        @NotEmpty(message = "收件人不能为空")
        List<@Email(message = "收件人邮箱格式不正确") String> to,

        List<@Email(message = "抄送邮箱格式不正确") String> cc,

        List<@Email(message = "密送邮箱格式不正确") String> bcc,

        @NotBlank(message = "邮件主题不能为空")
        String subject,

        @NotBlank(message = "邮件内容不能为空")
        String content,

        Boolean html
) {

    /**
     * 未传 html 字段时按纯文本处理。
     */
    public boolean htmlEnabled() {
        return Boolean.TRUE.equals(html);
    }
}
