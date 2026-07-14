package com.admin.service;

import com.admin.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RoleService extends IService<SysRole> {
    void saveRoleMenus(Long roleId, List<Long> menuIdList);
    List<Long> getRoleMenuIds(Long roleId);
}