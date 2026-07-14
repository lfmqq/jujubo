package com.admin.controller;

import com.admin.common.result.Result;
import com.admin.entity.SysUser;
import com.admin.mapper.SysDeptMapper;
import com.admin.mapper.SysMenuMapper;
import com.admin.mapper.SysRoleMapper;
import com.admin.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysDeptMapper deptMapper;

    /**
     * 数据概览 — 各模块总数 + 启用/禁用用户数
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('statistics:overview:list')")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("roleCount", roleMapper.selectCount(null));
        data.put("menuCount", menuMapper.selectCount(null));
        data.put("deptCount", deptMapper.selectCount(null));

        // 启用用户数（status = 1 正常）
        LambdaQueryWrapper<SysUser> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(SysUser::getStatus, 1);
        data.put("activeUsers", userMapper.selectCount(activeWrapper));

        // 禁用用户数（status = 0 禁用）
        LambdaQueryWrapper<SysUser> disabledWrapper = new LambdaQueryWrapper<>();
        disabledWrapper.eq(SysUser::getStatus, 0);
        data.put("disabledUsers", userMapper.selectCount(disabledWrapper));

        return Result.success(data);
    }

    /**
     * 用户注册趋势 — 按月统计
     */
    @GetMapping("/user-trend")
    @PreAuthorize("hasAuthority('statistics:overview:list')")
    public Result<List<Map<String, Object>>> userTrend() {
        return Result.success(userMapper.countByMonth());
    }

    /**
     * 用户注册趋势 — 按周统计（最近 7 天）
     */
    @GetMapping("/user-trend-week")
    @PreAuthorize("hasAuthority('statistics:overview:list')")
    public Result<List<Map<String, Object>>> userTrendWeek() {
        return Result.success(userMapper.countByWeek());
    }

    /**
     * 最近注册用户 TOP 10
     */
    @GetMapping("/latest-users")
    @PreAuthorize("hasAuthority('statistics:overview:list')")
    public Result<List<SysUser>> latestUsers() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysUser::getId, SysUser::getUsername, SysUser::getNickname,
                        SysUser::getEmail, SysUser::getCreateTime, SysUser::getStatus)
                .orderByDesc(SysUser::getCreateTime)
                .last("LIMIT 10");
        return Result.success(userMapper.selectList(wrapper));
    }
}
