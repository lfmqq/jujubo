package com.admin.service;

import com.admin.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface MenuService extends IService<SysMenu> {
    /**
     * 获取菜单树，支持按菜单名称模糊搜索
     */
    List<SysMenu> getMenuTree(String menuName);

    /**
     * 获取当前用户可见的菜单树（目录+菜单，不含按钮）
     */
    List<SysMenu> getUserMenus(Long userId);

    /**
     * 获取用户所有权限标识（含父级菜单权限，向上追溯祖先链）
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 仅更新菜单启用/禁用状态（避免 updateById 对 Integer 0 值的歧义处理）
     */
    void updateStatus(Long id, Integer status);
}