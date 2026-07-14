package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String component;
    private String perms;
    private Integer type;
    private String icon;
    private Integer sort;
    /** 是否在侧边栏显示：1=显示，0=隐藏，null 等同于显示 */
    private Integer visible;
    /** 是否总是显示该菜单：1=总是（默认），0=不是；当为不是且只有一个子菜单时，折叠显示子菜单 */
    private Integer alwaysShow;
    /** 菜单状态：1=启用（默认），0=禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 树形结构子节点，不映射数据库
    @TableField(exist = false)
    private List<SysMenu> children;
}