package com.admin.common.security;

import com.admin.entity.SysUser;
import com.admin.mapper.SysUserMapper;
import com.admin.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final SysUserMapper userMapper;
    private final MenuService menuService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("账号或密码错误");
        }
        List<String> permissions = menuService.getUserPermissions(user.getId());
        return new LoginUser(user, permissions);
    }
}