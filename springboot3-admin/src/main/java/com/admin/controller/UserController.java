package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.enums.OperatorType;
import com.admin.common.result.Result;
import com.admin.common.security.LoginUser;
import com.admin.common.util.RedisUtil;
import com.admin.entity.SysRole;
import com.admin.entity.SysUser;
import com.admin.mapper.SysRoleMapper;
import com.admin.mapper.SysUserRoleMapper;
import com.admin.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<IPage<SysUser>> page(Long pageNum, Long pageSize,
                                        @RequestParam(required = false) String username,
                                        @RequestParam(required = false) Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return Result.success(userService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<SysUser> getInfo(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) {
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(id);
            user.setRoleIds(roleIds);
        }
        return Result.success(user);
    }

    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<?> add(@RequestBody SysUser user) {
        userService.save(user);
        return Result.success();
    }

    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<?> update(@RequestBody SysUser user) {
        userService.updateById(user);
        return Result.success();
    }

    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:remove')")
    public Result<?> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 获取当前登录用户信息（从 Redis 缓存读取，参考芋道源码）
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> profile() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser user = loginUser.getSysUser();
        user.setPassword(null); // 不暴露密码

        // 从数据库查询角色名称
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("permissions", loginUser.getPermissionList());
        data.put("roles", roles.stream().map(SysRole::getRoleName).toList());
        return Result.success(data);
    }

    /**
     * 更新当前用户个人信息（只更新昵称、邮箱、头像，不覆盖密码）
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody SysUser param) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser user = loginUser.getSysUser();

        // 只更新指定字段，避免 MyBatis-Plus updateById 覆盖 password
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId());

        boolean needUpdate = false;
        if (param.getNickname() != null) {
            wrapper.set(SysUser::getNickname, param.getNickname());
            user.setNickname(param.getNickname());
            needUpdate = true;
        }
        if (param.getEmail() != null) {
            wrapper.set(SysUser::getEmail, param.getEmail());
            user.setEmail(param.getEmail());
            needUpdate = true;
        }
        if (param.getAvatar() != null) {
            wrapper.set(SysUser::getAvatar, param.getAvatar());
            user.setAvatar(param.getAvatar());
            needUpdate = true;
        }

        if (needUpdate) {
            userService.update(wrapper);
            // 更新 Redis 缓存，保持与 DB 一致
            redisUtil.set("login:user:" + user.getId(), loginUser, 86400L);
        }
        return Result.success();
    }

    /**
     * 重置用户密码为默认密码 awei123456
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/reset-password/{id}")
    @PreAuthorize("hasAuthority('system:user:reset')")
    public Result<?> resetPassword(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        String encodedPwd = passwordEncoder.encode("awei123456");
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getPassword, encodedPwd);
        userService.update(wrapper);
        // 清除该用户 Redis 登录缓存，强制重新登录
        redisUtil.delete("login:user:" + id);
        return Result.success();
    }

    /**
     * 导出所有用户数据（不分页）
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<List<SysUser>> export(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        List<SysUser> list = userService.list(wrapper);
        // 脱敏：移除密码字段
        list.forEach(user -> user.setPassword(null));
        return Result.success(list);
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/profile/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> param) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser user = loginUser.getSysUser();
        String oldPassword = param.get("oldPassword");
        String newPassword = param.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.fail(400, "参数缺失");
        }
        // 校验原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.fail(400, "原密码不正确");
        }
        String encodedPwd = passwordEncoder.encode(newPassword);
        // 只更新密码字段
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getPassword, encodedPwd);
        userService.update(wrapper);
        // 清除 Redis 缓存，强制用户重新登录（避免缓存中旧密码被 write-back 覆盖新密码）
        redisUtil.delete("login:user:" + user.getId());
        return Result.success();
    }
}