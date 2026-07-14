package com.admin.common.enums;

import lombok.Getter;

/**
 * 操作人类别
 */
@Getter
public enum OperatorType {
    MANAGE(0, "后台用户"),
    FRONTEND(1, "前端用户"),
    MOBILE(2, "手机端用户");

    private final int code;
    private final String desc;

    OperatorType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
