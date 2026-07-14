package com.admin.service.impl;

import com.admin.entity.SysRole;
import com.admin.entity.SysRoleMenu;
import com.admin.mapper.SysRoleMapper;
import com.admin.mapper.SysRoleMenuMapper;
import com.admin.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {

    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleMenus(Long roleId, List<Long> menuIdList) {
        System.out.println("[saveRoleMenus] roleId=" + roleId + ", menuIds=" + menuIdList);
        // 1、删除该角色原有菜单关联
        roleMenuMapper.deleteByRoleId(roleId);

        // 2、批量插入新的角色-菜单关联
        if (menuIdList != null && !menuIdList.isEmpty()) {
            List<SysRoleMenu> roleMenuList = menuIdList.stream()
                    .map(menuId -> {
                        SysRoleMenu rm = new SysRoleMenu();
                        rm.setRoleId(roleId);
                        rm.setMenuId(menuId);
                        return rm;
                    }).collect(Collectors.toList());
            // 调用自定义Mapper批量插入，彻底解决类型不匹配报错
            roleMenuMapper.insertBatch(roleMenuList);
        }
        System.out.println("[saveRoleMenus] 保存完成");
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        System.out.println("[getRoleMenuIds] 查询角色 roleId=" + roleId);
        // 直接查 sys_role_menu 表，绕过 sys_menu 联表，避免 MyBatis-Plus 拦截器干扰
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleId(roleId);
        System.out.println("[getRoleMenuIds] 查到 " + menuIds.size() + " 条: " + menuIds);
        return menuIds;
    }
}