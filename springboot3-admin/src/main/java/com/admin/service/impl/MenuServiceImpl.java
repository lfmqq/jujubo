package com.admin.service.impl;

import com.admin.entity.SysMenu;
import com.admin.mapper.SysMenuMapper;
import com.admin.service.MenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements MenuService {

    @Override
    public List<SysMenu> getMenuTree(String menuName) {
        List<SysMenu> allMenu = baseMapper.selectAllMenu();
        if (menuName == null || menuName.isBlank()) {
            return buildTree(allMenu, 0L);
        }
        // 按名称过滤 + 追溯父级保留树结构
        List<SysMenu> filteredMenus = filterByMenuName(allMenu, menuName);
        return buildTree(filteredMenus, 0L);
    }

    /**
     * 按菜单名称模糊过滤，并自动补全匹配节点的所有祖先节点
     */
    private List<SysMenu> filterByMenuName(List<SysMenu> allMenus, String keyword) {
        Map<Long, SysMenu> menuMap = allMenus.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m, (a, b) -> a));
        Set<Long> matchIds = new HashSet<>();
        // 找到所有名称匹配的节点
        for (SysMenu menu : allMenus) {
            if (menu.getMenuName() != null && menu.getMenuName().contains(keyword)) {
                matchIds.add(menu.getId());
                // 向上追溯祖先
                SysMenu current = menu;
                while (current.getParentId() != null && current.getParentId() != 0) {
                    matchIds.add(current.getParentId());
                    current = menuMap.get(current.getParentId());
                    if (current == null) break;
                }
            }
        }
        return allMenus.stream()
                .filter(m -> matchIds.contains(m.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SysMenu> getUserMenus(Long userId) {
        // 1. 获取用户所有已分配的菜单ID（含按钮 type=2）
        List<Long> assignedIds = baseMapper.selectMenuIdsByUserId(userId);
        System.out.println("[getUserMenus] userId=" + userId + " 已分配=" + assignedIds);

        // 2. 加载全部菜单，构建 id → menu 映射
        List<SysMenu> allMenus = baseMapper.selectAllMenu();
        Map<Long, SysMenu> menuMap = allMenus.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m, (a, b) -> a));

        // 3. 从每个已分配的菜单ID开始，向上追溯所有目录/菜单类型的祖先
        Set<Long> visibleIds = new HashSet<>();
        for (Long id : assignedIds) {
            SysMenu current = menuMap.get(id);
            while (current != null) {
                // 目录(0) 和 菜单(1) 需要显示，按钮(2) 不显示但继续向上找父级
                if (current.getType() == 0 || current.getType() == 1) {
                    visibleIds.add(current.getId());
                }
                // 查找父级（parentId=0 说明到顶）
                current = menuMap.get(current.getParentId());
            }
        }
        System.out.println("[getUserMenus] 可见菜单ID=" + visibleIds);

        // 4. 过滤：禁用菜单(status=0)不返回；visible=0 由前端侧边栏过滤，但路由仍需注册因此返回
        List<SysMenu> userMenus = allMenus.stream()
                .filter(m -> visibleIds.contains(m.getId()) && (m.getStatus() == null || m.getStatus() != 0))
                .collect(Collectors.toList());
        return buildTree(userMenus, 0L);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 1. 获取用户所有已分配的菜单ID（含按钮 type=2）
        List<Long> assignedIds = baseMapper.selectMenuIdsByUserId(userId);
        System.out.println("[getUserPermissions] userId=" + userId + " 已分配=" + assignedIds);

        // 2. 加载全部菜单，构建 id → menu 映射
        List<SysMenu> allMenus = baseMapper.selectAllMenu();
        Map<Long, SysMenu> menuMap = allMenus.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m, (a, b) -> a));

        // 3. 追溯祖先链，收集所有 perms（含父级菜单的 list 权限）
        Set<String> perms = new HashSet<>();
        for (Long id : assignedIds) {
            SysMenu current = menuMap.get(id);
            while (current != null) {
                if (current.getPerms() != null && !current.getPerms().isEmpty()) {
                    perms.add(current.getPerms());
                }
                current = menuMap.get(current.getParentId());
            }
        }
        List<String> result = new ArrayList<>(perms);
        System.out.println("[getUserPermissions] 权限列表=" + result);
        return result;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        lambdaUpdate()
                .set(SysMenu::getStatus, status)
                .eq(SysMenu::getId, id)
                .update();
    }

    /**
     * 递归构建菜单树形结构
     */
    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        return all.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .peek(menu -> menu.setChildren(buildTree(all, menu.getId())))
                .collect(Collectors.toList());
    }
}