package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.result.Result;
import com.admin.entity.SysRole;
import com.admin.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<IPage<SysRole>> page(Long pageNum, Long pageSize,
                                        @RequestParam(required = false) String roleName,
                                        @RequestParam(required = false) String roleCode) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        if (StringUtils.hasText(roleCode)) {
            wrapper.like(SysRole::getRoleCode, roleCode);
        }
        wrapper.orderByDesc(SysRole::getCreateTime);
        return Result.success(roleService.page(page, wrapper));
    }

    @GetMapping("/list")
    public Result<List<SysRole>> listAll() {
        return Result.success(roleService.list());
    }

    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<?> add(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.success();
    }

    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<?> update(@RequestBody SysRole role) {
        roleService.updateById(role);
        return Result.success();
    }

    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:remove')")
    public Result<?> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.success();
    }

    // 分配菜单
    @PostMapping("/assignMenu")
    public Result<?> assignMenu(@RequestParam Long roleId, @RequestBody List<Long> menuIdList) {
        roleService.saveRoleMenus(roleId, menuIdList);
        return Result.success();
    }

    // 获取角色已绑定的菜单ID
    @GetMapping("/getMenuIds/{roleId}")
    public Result<List<Long>> getMenuIds(@PathVariable Long roleId) {
        return Result.success(roleService.getRoleMenuIds(roleId));
    }
}