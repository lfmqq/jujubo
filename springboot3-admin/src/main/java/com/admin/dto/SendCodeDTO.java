package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送验证码请求
 */
@Data
public class SendCodeDTO {
    /** 账号：手机号或邮箱 */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 类型：sms-短信, email-邮箱 */
    @NotBlank(message = "验证码类型不能为空")
    private String type;
}
