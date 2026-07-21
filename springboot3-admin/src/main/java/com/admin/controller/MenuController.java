package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.result.Result;
import com.admin.common.security.LoginUser;
import com.admin.entity.SysMenu;
import com.admin.service.MenuService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<SysMenu>> tree(@RequestParam(required = false) String menuName) {
        return Result.success(menuService.getMenuTree(menuName));
    }

    @GetMapping("/user-menu")
    public Result<List<SysMenu>> userMenu() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((LoginUser) auth.getPrincipal()).getSysUser().getId();
        return Result.success(menuService.getUserMenus(userId));
    }

    /** 获取当前用户的所有权限标识（含按钮级权限） */
    @GetMapping("/user-permissions")
    public Result<List<String>> userPermissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((LoginUser) auth.getPrincipal()).getSysUser().getId();
        return Result.success(menuService.getUserPermissions(userId));
    }

    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<?> add(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return Result.success();
    }

    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<?> update(@RequestBody SysMenu menu) {
        // 使用 LambdaUpdateWrapper 显式 SET 每个字段，
        // 避免 MyBatis-Plus updateById 对 Integer 0 值的模糊处理
        LambdaUpdateWrapper<SysMenu> wrapper = new LambdaUpdateWrapper<>();
        if (menu.getId() != null) { wrapper.eq(SysMenu::getId, menu.getId()); }
        if (menu.getParentId() != null) { wrapper.set(SysMenu::getParentId, menu.getParentId()); }
        if (menu.getMenuName() != null) { wrapper.set(SysMenu::getMenuName, menu.getMenuName()); }
        if (menu.getPath() != null) { wrapper.set(SysMenu::getPath, menu.getPath()); }
        if (menu.getComponent() != null) { wrapper.set(SysMenu::getComponent, menu.getComponent()); }
        if (menu.getPerms() != null) { wrapper.set(SysMenu::getPerms, menu.getPerms()); }
        if (menu.getType() != null) { wrapper.set(SysMenu::getType, menu.getType()); }
        if (menu.getIcon() != null) { wrapper.set(SysMenu::getIcon, menu.getIcon()); }
        if (menu.getSort() != null) { wrapper.set(SysMenu::getSort, menu.getSort()); }
        if (menu.getVisible() != null) { wrapper.set(SysMenu::getVisible, menu.getVisible()); }
        if (menu.getAlwaysShow() != null) { wrapper.set(SysMenu::getAlwaysShow, menu.getAlwaysShow()); }
        if (menu.getStatus() != null) { wrapper.set(SysMenu::getStatus, menu.getStatus()); }
        menuService.update(wrapper);
        return Result.success();
    }

    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping("/toggle-status")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<?> toggleStatus(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        Integer status = ((Number) params.get("status")).intValue();
        menuService.updateStatus(id, status);
        return Result.success();
    }

    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    public Result<?> delete(@PathVariable Long id) {
        menuService.removeById(id);
        return Result.success();
    }
}