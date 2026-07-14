package com.admin.common.enums;

import lombok.Getter;

/**
 * 业务操作类型
 */
@Getter
public enum BusinessType {
    OTHER(0, "其他"),
    INSERT(1, "新增"),
    UPDATE(2, "修改"),
    DELETE(3, "删除"),
    SELECT(4, "查询"),
    LOGIN(5, "登录"),
    LOGOUT(6, "退出"),
    EXPORT(7, "导出"),
    IMPORT(8, "导入"),
    FORCE(9, "强退"),
    CLEAN(10, "清空");

    private final int code;
    private final String desc;

    BusinessType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
