package com.admin.controller;

import com.admin.common.result.Result;
import com.admin.mapper.SysDeptMapper;
import com.admin.mapper.SysMenuMapper;
import com.admin.mapper.SysRoleMapper;
import com.admin.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysDeptMapper deptMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("roleCount", roleMapper.selectCount(null));
        data.put("menuCount", menuMapper.selectCount(null));
        data.put("deptCount", deptMapper.selectCount(null));
        return Result.success(data);
    }
}
