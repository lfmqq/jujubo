package com.admin.config;

import com.admin.entity.SysMenu;
import com.admin.entity.SysRoleMenu;
import com.admin.entity.SysUserRole;
import com.admin.mapper.SysMenuMapper;
import com.admin.mapper.SysRoleMenuMapper;
import com.admin.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时自动修复缺失的数据关联关系。
 * 仅修复，不覆盖已有数据。
 */
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Override
    public void run(ApplicationArguments args) {
        // 1. 检查 admin 用户（id=1）是否已有角色
        Long count = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, 1L)
        );
        if (count == 0) {
            System.out.println("[DataInitializer] admin 用户缺少角色关联，自动关联到超级管理员（role_id=1）...");
            SysUserRole ur = new SysUserRole();
            ur.setUserId(1L);
            ur.setRoleId(1L);
            userRoleMapper.insert(ur);
            System.out.println("[DataInitializer] 修复完成：admin → 超级管理员");
        }

        // 2. 确保通知管理的按钮权限存在（add / delete）
        ensureNotifyButtonPermissions();

        // 3. 确保用户管理的按钮权限存在（reset）
        ensureUserButtonPermissions();

        // 4. 确保「系统监控 → 操作日志」菜单存在（前端日志查看页）
        ensureMonitorMenu();

        // 5. 确保 IoT 物联网模块菜单
        ensureIotMenus();

        // 6. 确保代码生成模块菜单
        ensureGenMenus();

        // 7. 确保定时任务模块菜单
        ensureJobMenus();

        // 8. 确保字典管理模块菜单
        ensureDictMenus();
    }

    /**
     * 确保 sys_menu 中存在 system:notify:add 和 system:notify:delete 按钮权限记录，
     * 并分配给超级管理员角色（role_id=1）。
     * 按钮挂在通知管理父菜单（perms='system:notify:list'）下。
     */
    private void ensureNotifyButtonPermissions() {
        // 查找通知管理父菜单
        List<SysMenu> parentMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getPerms, "system:notify:list")
                        .eq(SysMenu::getType, 1)
        );
        if (parentMenus.isEmpty()) {
            System.out.println("[DataInitializer] 未找到通知管理菜单（perms='system:notify:list'），跳过按钮权限初始化");
            return;
        }
        Long parentId = parentMenus.get(0).getId();

        // 定义缺失的按钮权限
        String[][] buttonDefs = {
                {"system:notify:add", "新增通知", "1"},
                {"system:notify:delete", "删除通知", "2"}
        };

        for (String[] def : buttonDefs) {
            String perms = def[0];
            String name = def[1];
            int sort = Integer.parseInt(def[2]);

            Long exists = menuMapper.selectCount(
                    new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getPerms, perms)
            );
            if (exists > 0) {
                continue; // 已存在，跳过
            }

            SysMenu btn = new SysMenu();
            btn.setParentId(parentId);
            btn.setMenuName(name);
            btn.setPath("");
            btn.setComponent("");
            btn.setPerms(perms);
            btn.setType(2);     // 按钮
            btn.setIcon("");
            btn.setSort(sort);
            btn.setVisible(1);
            btn.setAlwaysShow(1);
            btn.setStatus(1);
            menuMapper.insert(btn);
            System.out.println("[DataInitializer] 创建按钮权限：" + perms + " (id=" + btn.getId() + ")");

            // 分配给 role_id=1（超级管理员）
            Long roleHas = roleMenuMapper.selectCount(
                    new LambdaQueryWrapper<SysRoleMenu>()
                            .eq(SysRoleMenu::getRoleId, 1L)
                            .eq(SysRoleMenu::getMenuId, btn.getId())
            );
            if (roleHas == 0) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(1L);
                rm.setMenuId(btn.getId());
                roleMenuMapper.insert(rm);
                System.out.println("[DataInitializer] 分配权限 " + perms + " → role_id=1");
            }
        }
    }

    /**
     * 确保 sys_menu 中存在 system:user:reset 按钮权限记录，
     * 并分配给超级管理员角色（role_id=1）。
     * 按钮挂在用户管理父菜单（perms='system:user:list'）下。
     */
    private void ensureUserButtonPermissions() {
        List<SysMenu> parentMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getPerms, "system:user:list")
                        .eq(SysMenu::getType, 1)
        );
        if (parentMenus.isEmpty()) {
            System.out.println("[DataInitializer] 未找到用户管理菜单（perms='system:user:list'），跳过重置密码按钮权限初始化");
            return;
        }
        Long parentId = parentMenus.get(0).getId();

        // 定义缺失的按钮权限
        String[][] buttonDefs = {
                {"system:user:reset", "重置密码", "3"}
        };

        for (String[] def : buttonDefs) {
            String perms = def[0];
            String name = def[1];
            int sort = Integer.parseInt(def[2]);

            Long exists = menuMapper.selectCount(
                    new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getPerms, perms)
            );
            if (exists > 0) {
                continue; // 已存在，跳过
            }

            SysMenu btn = new SysMenu();
            btn.setParentId(parentId);
            btn.setMenuName(name);
            btn.setPath("");
            btn.setComponent("");
            btn.setPerms(perms);
            btn.setType(2);     // 按钮
            btn.setIcon("");
            btn.setSort(sort);
            btn.setVisible(1);
            btn.setAlwaysShow(1);
            btn.setStatus(1);
            menuMapper.insert(btn);
            System.out.println("[DataInitializer] 创建按钮权限：" + perms + " (id=" + btn.getId() + ")");

            // 分配给 role_id=1（超级管理员）
            Long roleHas = roleMenuMapper.selectCount(
                    new LambdaQueryWrapper<SysRoleMenu>()
                            .eq(SysRoleMenu::getRoleId, 1L)
                            .eq(SysRoleMenu::getMenuId, btn.getId())
            );
            if (roleHas == 0) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(1L);
                rm.setMenuId(btn.getId());
                roleMenuMapper.insert(rm);
                System.out.println("[DataInitializer] 分配权限 " + perms + " → role_id=1");
            }
        }
    }

    /**
     * 确保 sys_menu 中存在「系统监控」目录与「操作日志」菜单，并分配给超级管理员（role_id=1）。
     * - 目录 component='Layout'（前端动态路由映射为 ParentView）
     * - 菜单 component='monitor/operlog'（映射为 views/monitor/operlog/index.vue）
     */
    private void ensureMonitorMenu() {
        // 1. 系统监控目录（type=0, parentId=0）
        String dirPerms = "";
        List<SysMenu> dirs = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getMenuName, "系统监控")
                        .eq(SysMenu::getType, 0)
        );
        Long dirId;
        if (dirs.isEmpty()) {
            SysMenu dir = new SysMenu();
            dir.setParentId(0L);
            dir.setMenuName("系统监控");
            dir.setPath("monitor");
            dir.setComponent("Layout");
            dir.setPerms(dirPerms);
            dir.setType(0);
            dir.setIcon("Monitor");
            dir.setSort(99);
            dir.setVisible(1);
            dir.setAlwaysShow(1);
            dir.setStatus(1);
            menuMapper.insert(dir);
            dirId = dir.getId();
            System.out.println("[DataInitializer] 创建目录菜单：系统监控 (id=" + dirId + ")");
            // 分配给 role_id=1
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(1L);
            rm.setMenuId(dirId);
            roleMenuMapper.insert(rm);
        } else {
            dirId = dirs.get(0).getId();
        }

        // 2. 操作日志菜单（type=1, parentId=dirId）
        String menuPerms = "monitor:operlog:list";
        List<SysMenu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getMenuName, "操作日志")
                        .eq(SysMenu::getType, 1)
        );
        Long menuId;
        if (menus.isEmpty()) {
            SysMenu menu = new SysMenu();
            menu.setParentId(dirId);
            menu.setMenuName("操作日志");
            menu.setPath("operlog");
            menu.setComponent("monitor/operlog");
            menu.setPerms(menuPerms);
            menu.setType(1);
            menu.setIcon("Document");
            menu.setSort(1);
            menu.setVisible(1);
            menu.setAlwaysShow(1);
            menu.setStatus(1);
            menuMapper.insert(menu);
            menuId = menu.getId();
            System.out.println("[DataInitializer] 创建菜单：操作日志 (id=" + menuId + ")");
        } else {
            menuId = menus.get(0).getId();
        }

        // 3. 确保操作日志的按钮权限（删除、清空）存在并分配给 role_id=1
        Long parentId = menuId;
        String[][] buttonDefs = {
                {"monitor:operlog:delete", "删除", "1"},
                {"monitor:operlog:clean", "清空", "2"}
        };
        for (String[] def : buttonDefs) {
            String perms = def[0];
            String name = def[1];
            int sort = Integer.parseInt(def[2]);
            Long exists = menuMapper.selectCount(
                    new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getPerms, perms)
            );
            if (exists > 0) {
                continue;
            }
            SysMenu btn = new SysMenu();
            btn.setParentId(parentId);
            btn.setMenuName(name);
            btn.setPath("");
            btn.setComponent("");
            btn.setPerms(perms);
            btn.setType(2);
            btn.setIcon("");
            btn.setSort(sort);
            btn.setVisible(1);
            btn.setAlwaysShow(1);
            btn.setStatus(1);
            menuMapper.insert(btn);
            System.out.println("[DataInitializer] 创建按钮权限：" + perms + " (id=" + btn.getId() + ")");

            Long roleHas = roleMenuMapper.selectCount(
                    new LambdaQueryWrapper<SysRoleMenu>()
                            .eq(SysRoleMenu::getRoleId, 1L)
                            .eq(SysRoleMenu::getMenuId, btn.getId())
            );
            if (roleHas == 0) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(1L);
                rm.setMenuId(btn.getId());
                roleMenuMapper.insert(rm);
                System.out.println("[DataInitializer] 分配权限 " + perms + " → role_id=1");
            }
        }
    }

    /** 确保 IoT 物联网模块菜单存在 */
    private void ensureIotMenus() {
        Long dirId = ensureDirMenu("IoT物联网", "/iot", "Cpu", 10);
        if (dirId == null) return;

        Long productId = ensureMenu(dirId, "产品管理", "/iot/product", "iot/product/index",
                "iot:product:list", "Goods", 1);
        Long deviceId = ensureMenu(dirId, "设备管理", "/iot/device", "iot/device/index",
                "iot:device:list", "Monitor", 2);

        ensureButton(productId, "iot:product:add", "产品新增", 1);
        ensureButton(productId, "iot:product:edit", "产品编辑", 2);
        ensureButton(productId, "iot:product:remove", "产品删除", 3);
        ensureButton(deviceId, "iot:device:add", "设备新增", 1);
        ensureButton(deviceId, "iot:device:edit", "设备编辑", 2);
        ensureButton(deviceId, "iot:device:remove", "设备删除", 3);
    }

    private void ensureGenMenus() {
        Long dirId = ensureDirMenu("开发工具", "/tool", "Tools", 20);
        if (dirId == null) return;

        Long genId = ensureMenu(dirId, "代码生成", "/tool/gen", "tool/gen/index",
                "tool:gen:list", "EditPen", 1);
        ensureButton(genId, "tool:gen:code", "生成代码", 1);
    }

    private void ensureJobMenus() {
        List<SysMenu> monitorDirs = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getMenuName, "系统监控")
                        .eq(SysMenu::getType, 0));
        if (monitorDirs.isEmpty()) return;
        Long parentId = monitorDirs.get(0).getId();

        Long jobId = ensureMenu(parentId, "定时任务", "/job", "monitor/job/index",
                "monitor:job:list", "Clock", 2);
        ensureMenu(parentId, "调度日志", "/job/log", "monitor/job/log",
                "monitor:job:log", "Tickets", 3);

        ensureButton(jobId, "monitor:job:add", "任务新增", 1);
        ensureButton(jobId, "monitor:job:edit", "任务编辑", 2);
        ensureButton(jobId, "monitor:job:remove", "任务删除", 3);
    }

    // ==================== 通用辅助方法 ====================

    private Long ensureDirMenu(String name, String path, String icon, int sort) {
        List<SysMenu> dirs = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getMenuName, name)
                        .eq(SysMenu::getType, 0));
        if (!dirs.isEmpty()) return dirs.get(0).getId();

        SysMenu dir = new SysMenu();
        dir.setParentId(0L);
        dir.setMenuName(name);
        dir.setPath(path);
        dir.setComponent("Layout");
        dir.setPerms("");
        dir.setType(0);
        dir.setIcon(icon);
        dir.setSort(sort);
        dir.setVisible(1);
        dir.setAlwaysShow(1);
        dir.setStatus(1);
        menuMapper.insert(dir);
        System.out.println("[DataInitializer] 创建目录菜单：" + name + " (id=" + dir.getId() + ")");

        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(1L);
        rm.setMenuId(dir.getId());
        roleMenuMapper.insert(rm);
        return dir.getId();
    }

    private Long ensureMenu(Long parentId, String name, String path, String component,
                            String perms, String icon, int sort) {
        List<SysMenu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getMenuName, name)
                        .eq(SysMenu::getType, 1));
        if (!menus.isEmpty()) return menus.get(0).getId();

        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName(name);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setPerms(perms);
        menu.setType(1);
        menu.setIcon(icon);
        menu.setSort(sort);
        menu.setVisible(1);
        menu.setAlwaysShow(1);
        menu.setStatus(1);
        menuMapper.insert(menu);
        System.out.println("[DataInitializer] 创建菜单：" + name + " (id=" + menu.getId() + ")");

        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(1L);
        rm.setMenuId(menu.getId());
        roleMenuMapper.insert(rm);
        return menu.getId();
    }

    private void ensureButton(Long parentId, String perms, String name, int sort) {
        Long exists = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getPerms, perms));
        if (exists > 0) return;

        SysMenu btn = new SysMenu();
        btn.setParentId(parentId);
        btn.setMenuName(name);
        btn.setPath("");
        btn.setComponent("");
        btn.setPerms(perms);
        btn.setType(2);
        btn.setIcon("");
        btn.setSort(sort);
        btn.setVisible(1);
        btn.setAlwaysShow(1);
        btn.setStatus(1);
        menuMapper.insert(btn);

        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(1L);
        rm.setMenuId(btn.getId());
        roleMenuMapper.insert(rm);
    }

    /** 确保字典管理模块菜单 */
    private void ensureDictMenus() {
        // 查找「系统管理」目录
        List<SysMenu> systemDirs = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getMenuName, "系统管理")
                        .eq(SysMenu::getType, 0));
        if (systemDirs.isEmpty()) return;
        Long dirId = systemDirs.get(0).getId();

        Long dictId = ensureMenu(dirId, "字典管理", "/system/dict", "system/dict/index",
                "system:dict:list", "Collection", 5);
        ensureButton(dictId, "system:dict:add", "字典新增", 1);
        ensureButton(dictId, "system:dict:edit", "字典编辑", 2);
        ensureButton(dictId, "system:dict:remove", "字典删除", 3);
    }
}
