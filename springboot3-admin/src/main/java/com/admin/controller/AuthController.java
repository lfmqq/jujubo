package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.enums.OperatorType;
import com.admin.common.exception.ServiceException;
import com.admin.common.result.Result;
import com.admin.common.security.LoginUser;
import com.admin.common.util.JwtUtil;
import com.admin.common.util.RedisUtil;
import com.admin.dto.LoginDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Log(title = "用户登录", businessType = BusinessType.LOGIN, operatorType = OperatorType.MANAGE, isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginDTO loginDTO) {
        // 校验图形验证码（防止机器人暴力破解）
        validateCaptcha(loginDTO.getCode(), loginDTO.getUuid());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = jwtUtil.generateToken(loginUser.getSysUser().getId());
        // 设置token有效期和登录用户缓存一致
        redisUtil.set("login:user:" + loginUser.getSysUser().getId(), loginUser, 86400L);
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        return Result.success(map);
    }

    /** 校验图形验证码：过期 / 错误 / 一次性使用 */
    private void validateCaptcha(String code, String uuid) {
        String key = "captcha:" + uuid;
        Object cached = redisUtil.get(key);
        if (cached == null) {
            throw new ServiceException(500, "验证码已过期，请点击刷新");
        }
        // 无论对错，验证码使用一次后即失效
        redisUtil.delete(key);
        if (!cached.toString().equalsIgnoreCase(code == null ? "" : code.trim())) {
            throw new ServiceException(500, "验证码错误");
        }
    }

    @Log(title = "用户退出", businessType = BusinessType.LOGOUT, operatorType = OperatorType.MANAGE, isSaveResponseData = false)
    @PostMapping("/logout")
    public Result<?> logout() {
        // 从 SecurityContext 获取当前用户，未登录时直接返回成功
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            redisUtil.delete("login:user:" + loginUser.getSysUser().getId());
        }
        return Result.success();
    }
}