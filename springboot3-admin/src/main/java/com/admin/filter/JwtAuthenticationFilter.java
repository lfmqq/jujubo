package com.admin.filter;

import com.admin.common.security.LoginUser;
import com.admin.common.util.JwtUtil;
import com.admin.common.util.RedisUtil;
import com.admin.entity.SysUser;
import com.admin.mapper.SysUserMapper;
import com.admin.service.MenuService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final SysUserMapper userMapper;
    private final MenuService menuService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtUtil.getToken(request);
        if (token != null) {
            try {
                Long userId = jwtUtil.getUserId(token);
                log.info("JWT filter: userId={}, uri={}", userId, request.getRequestURI());
                // 从 Redis 获取缓存的用户信息
                LoginUser loginUser = (LoginUser) redisUtil.get("login:user:" + userId);
                // 每次都从 DB 加载最新权限，避免权限变更后缓存不更新
                List<String> permissions = menuService.getUserPermissions(userId);
                log.info("Loaded permissions for userId {}: {}", userId, permissions);
                if (loginUser != null) {
                    // Redis 命中：复用用户信息，刷新权限
                    loginUser = new LoginUser(loginUser.getSysUser(), permissions);
                } else {
                    // Redis 未命中：从 DB 加载用户信息
                    log.info("Redis cache miss for userId: {}, loading from DB", userId);
                    SysUser user = userMapper.selectById(userId);
                    if (user != null) {
                        loginUser = new LoginUser(user, permissions);
                    }
                }
                if (loginUser != null) {
                    // 更新 Redis 缓存
                    redisUtil.set("login:user:" + userId, loginUser, 86400L);
                    log.info("Setting authentication for userId: {}, authorities: {}",
                            userId, loginUser.getAuthorities().stream().map(Object::toString).toList());
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("LoginUser is null for userId: {}", userId);
                }
            } catch (Exception e) {
                log.error("JWT authentication failed: {}", e.getMessage(), e);
            }
        }
        filterChain.doFilter(request, response);
    }
}