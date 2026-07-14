package com.admin.common.util;

import com.admin.common.security.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtil {
    public static Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            log.error("获取用户ID失败: SecurityContext 中无认证信息");
            throw new RuntimeException("用户未登录");
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof LoginUser)) {
            log.error("获取用户ID失败: principal 类型为 {}", principal.getClass().getName());
            throw new RuntimeException("认证信息异常");
        }
        LoginUser loginUser = (LoginUser) principal;
        if (loginUser.getSysUser() == null || loginUser.getSysUser().getId() == null) {
            log.error("获取用户ID失败: LoginUser 中 sysUser 为 null");
            throw new RuntimeException("用户信息异常");
        }
        return loginUser.getSysUser().getId();
    }
}