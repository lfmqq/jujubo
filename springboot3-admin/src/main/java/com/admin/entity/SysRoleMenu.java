package com.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色-菜单关联表（多对多中间表，无单独主键）
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {
    /**
     * 使用 INPUT 策略，表示由用户程序提供值，不由 MP 自动生成
     * 避免默认 ASSIGN_ID 策略对 SQL 执行造成干扰
     */
    @TableId(type = IdType.INPUT)
    private Long roleId;
    private Long menuId;
}