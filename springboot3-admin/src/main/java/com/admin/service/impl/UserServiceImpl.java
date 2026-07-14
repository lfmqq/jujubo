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
}