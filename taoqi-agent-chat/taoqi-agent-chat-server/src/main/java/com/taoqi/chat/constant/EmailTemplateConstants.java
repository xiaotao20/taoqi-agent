package com.taoqi.chat.constant;

/**
 * 邮件 HTML 模板常量。
 *
 * <p>模板使用 {@code {{code}}}、{@code {{expireMinutes}}} 作为占位符，避免像 {@code String#format}
 * 那样与 CSS 中的百分号冲突。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
public final class EmailTemplateConstants {

    /**
     * 验证码邮件模板，风格参考 ChatGPT 的验证码邮件。
     */
    public static final String VERIFICATION_CODE_HTML = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Verify your email</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f7f7f8;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;">
              <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f7f7f8;padding:40px 16px;">
                <tr>
                  <td align="center">
                    <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="max-width:480px;background-color:#ffffff;border:1px solid #e5e5e5;border-radius:12px;">
                      <tr>
                        <td style="padding:40px 40px 8px 40px;font-size:20px;font-weight:600;color:#0d0d0d;">
                          Verify your email
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:8px 40px 0 40px;font-size:15px;line-height:24px;color:#40414f;">
                          Enter the following code to continue. This code expires in {{expireMinutes}} minutes.
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:28px 40px;">
                          <div style="font-family:'SFMono-Regular',Consolas,'Liberation Mono',Menlo,monospace;font-size:34px;font-weight:600;letter-spacing:10px;color:#0d0d0d;background-color:#f7f7f8;border-radius:10px;padding:20px 0;text-align:center;">
                            {{code}}
                          </div>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:0 40px 32px 40px;font-size:13px;line-height:20px;color:#8e8ea0;">
                          If you didn't request this email, you can safely ignore it.
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:20px 40px;border-top:1px solid #ececf1;font-size:12px;color:#8e8ea0;">
                          taoqi-agent · This is an automated message, please do not reply.
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;

    private EmailTemplateConstants() {
    }

    /**
     * 渲染验证码邮件 HTML。
     *
     * @param code          验证码
     * @param expireMinutes 有效分钟数
     */
    public static String verificationCodeHtml(String code, int expireMinutes) {
        return VERIFICATION_CODE_HTML
                .replace("{{code}}", code)
                .replace("{{expireMinutes}}", String.valueOf(expireMinutes));
    }
}