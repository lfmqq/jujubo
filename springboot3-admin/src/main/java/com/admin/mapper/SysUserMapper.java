package com.admin.mapper;

import com.admin.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("select * from sys_user where username = #{username}")
    SysUser selectByUsername(@Param("username") String username);

    @Select("select * from sys_user where phone = #{phone}")
    SysUser selectByPhone(@Param("phone") String phone);

    @Select("select * from sys_user where email = #{email}")
    SysUser selectByEmail(@Param("email") String email);

    /** 按月统计用户注册数量 */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, COUNT(*) AS count " +
            "FROM sys_user GROUP BY DATE_FORMAT(create_time, '%Y-%m') ORDER BY month")
    List<Map<String, Object>> countByMonth();

    /** 按周统计用户注册数量（最近 7 天，缺失日期补 0） */
    @Select("SELECT d.date AS day, COALESCE(t.count, 0) AS count " +
            "FROM ( " +
            "  SELECT CURDATE() - INTERVAL (a.a + (10 * b.a)) DAY AS date " +
            "  FROM (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 " +
            "        UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6) AS a " +
            "  CROSS JOIN (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 " +
            "              UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 " +
            "              UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 " +
            "              UNION ALL SELECT 9) AS b " +
            ") AS d " +
            "LEFT JOIN ( " +
            "  SELECT DATE(create_time) AS day, COUNT(*) AS count " +
            "  FROM sys_user " +
            "  WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "  GROUP BY DATE(create_time) " +
            ") AS t ON d.date = t.day " +
            "WHERE d.date BETWEEN DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND CURDATE() " +
            "ORDER BY d.date")
    List<Map<String, Object>> countByWeek();
}