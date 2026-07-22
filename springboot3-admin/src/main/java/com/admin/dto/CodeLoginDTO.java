package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证码登录请求（短信/邮箱通用）
 */
@Data
public class CodeLoginDTO {
    /** 账号：手机号或邮箱 */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 验证码 */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /** 类型：sms-短信, email-邮箱 */
    @NotBlank(message = "登录类型不能为空")
    private String type;
}
