package com.admin.common.result;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    TOKEN_ERROR(401, "token失效，请重新登录"),
    PERMISSION_DENY(403, "权限不足"),
    USER_NOT_EXIST(5001, "用户不存在"),
    PASSWORD_ERROR(5002, "密码错误");

    private final Integer code;
    private final String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}