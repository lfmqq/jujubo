package com.admin.service.impl;

import com.admin.entity.SysUser;
import com.admin.entity.SysUserRole;
import com.admin.mapper.SysUserMapper;
import com.admin.mapper.SysUserRoleMapper;
import com.admin.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysUser entity) {
        // 密码为空时使用默认密码，避免数据库 NOT NULL 约束报错
        if (entity.getPassword() == null || entity.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode("123456"));
        } else {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
        boolean save = super.save(entity);
        // 绑定角色（需要你在User实体加roleIds接收前端传参）
        if (entity.getRoleIds() != null && !entity.getRoleIds().isEmpty()) {
            List<SysUserRole> roleList = entity.getRoleIds().stream()
                    .map(rid -> {
                        SysUserRole ur = new SysUserRole();
                        ur.setUserId(entity.getId());
                        ur.setRoleId(rid);
                        return ur;
                    }).collect(Collectors.toList());
            userRoleMapper.insertBatch(roleList);
        }
        return save;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysUser entity) {
        // 密码不为空才加密
        if (entity.getPassword() != null && !entity.getPassword().isEmpty()) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
        boolean update = super.updateById(entity);
        // 先删角色再新增
        userRoleMapper.deleteByUserId(entity.getId());
        if (entity.getRoleIds() != null && !entity.getRoleIds().isEmpty()) {
            List<SysUserRole> roleList = entity.getRoleIds().stream()
                    .map(rid -> {
                        SysUserRole ur = new SysUserRole();
                        ur.setUserId(entity.getId());
                        ur.setRoleId(rid);
                        return ur;
                    }).collect(Collectors.toList());
            userRoleMapper.insertBatch(roleList);
        }
        return update;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser registerByPhoneOrEmail(String account, String type) {
        SysUser user = new SysUser();
        user.setStatus(1);
        // 默认分配普通用户角色 (role_id = 2)
        user.setRoleIds(List.of(2L));

        if ("sms".equals(type)) {
            // 手机号注册
            user.setPhone(account);
            user.setNickname("手机号用户");
            user.setUsername(generateUniqueUsername(account));
        } else {
            // 邮箱注册：提取@前面部分作为账号，如 123@163.com → 123
            user.setEmail(account);
            user.setNickname("邮箱用户");
            String prefix = account.contains("@") ? account.split("@")[0] : account;
            user.setUsername(generateUniqueUsername(prefix));
        }

        // password 为 null，父类 save 方法会使用默认密码 123456
        save(user);
        return user;
    }

    /**
     * 生成唯一用户名：如果 base 已存在，则追加 _1, _2... 后缀
     */
    private String generateUniqueUsername(String base) {
        String username = base;
        int suffix = 1;
        while (baseMapper.selectByUsername(username) != null) {
            username = base + "_" + suffix;
            suffix++;
        }
        return username;
    }
}