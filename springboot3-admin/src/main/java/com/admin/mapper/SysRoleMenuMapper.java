package com.admin.mapper;

import com.admin.entity.SysRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    void deleteByRoleId(@Param("roleId") Long roleId);

    void insertBatch(@Param("list") List<SysRoleMenu> list);

    /**
     * 直接查询角色关联的菜单ID列表
     * 绕过 sys_menu 联表，避免 MyBatis-Plus 拦截器干扰
     */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}