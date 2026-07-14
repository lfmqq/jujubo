package com.admin.mapper;

import com.admin.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询权限标识符
     */
    @Select("select distinct perms from sys_menu m " +
            "left join sys_role_menu rm on m.id = rm.menu_id " +
            "left join sys_user_role ur on rm.role_id = ur.role_id " +
            "where ur.user_id = #{userId} and m.perms is not null and m.perms <> ''")
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /**
     * 查询所有菜单
     */
    @Select("select * from sys_menu order by sort")
    List<SysMenu> selectAllMenu();

    /**
     * 根据角色ID查询绑定的菜单列表（RoleServiceImpl需要调用的核心方法）
     */
    @Select("SELECT m.* FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId}")
    List<SysMenu> selectMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询所有已分配菜单ID（含按钮type=2），用于向上追溯祖先目录/菜单
     */
    @Select("SELECT DISTINCT rm.menu_id FROM sys_role_menu rm " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Long> selectMenuIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询可见菜单（仅目录和菜单类型，排除按钮，用于侧边栏渲染）
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.type IN (0, 1) " +
            "AND (m.visible IS NULL OR m.visible = 1) ORDER BY m.sort")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}