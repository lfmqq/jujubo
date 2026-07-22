package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.enums.OperatorType;
import com.admin.common.exception.ServiceException;
import com.admin.common.result.Result;
import com.admin.common.security.LoginUser;
import com.admin.common.util.JwtUtil;
import com.admin.common.util.RedisUtil;
import com.admin.dto.CodeLoginDTO;
import com.admin.dto.LoginDTO;
import com.admin.dto.SendCodeDTO;
import com.admin.entity.SysUser;
import com.admin.mapper.SysUserMapper;
import com.admin.service.MenuService;
import com.admin.service.VerificationCodeService;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final VerificationCodeService verificationCodeService;
    private final SysUserMapper userMapper;
    private final MenuService menuService;

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

    /**
     * 发送短信/邮箱验证码
     */
    @Log(title = "发送验证码", businessType = BusinessType.OTHER, operatorType = OperatorType.MANAGE, isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping("/send-code")
    public Result<?> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        verificationCodeService.sendCode(dto.getAccount(), dto.getType());
        return Result.success();
    }

    /**
     * 短信/邮箱验证码登录
     */
    @Log(title = "验证码登录", businessType = BusinessType.LOGIN, operatorType = OperatorType.MANAGE, isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping("/login/code")
    public Result<Map<String, String>> codeLogin(@Valid @RequestBody CodeLoginDTO dto) {
        // 1. 校验验证码
        verificationCodeService.validateCode(dto.getAccount(), dto.getType(), dto.getCode());

        // 2. 根据 type 查找用户
        SysUser user;
        if ("sms".equals(dto.getType())) {
            user = userMapper.selectByPhone(dto.getAccount());
        } else if ("email".equals(dto.getType())) {
            user = userMapper.selectByEmail(dto.getAccount());
        } else {
            throw new ServiceException(500, "不支持的登录类型");
        }

        if (user == null) {
            throw new ServiceException(500, "账号未注册");
        }
        if (user.getStatus() != 1) {
            throw new ServiceException(500, "账号已被禁用，请联系管理员");
        }

        // 3. 获取权限并构建 LoginUser
        List<String> permissions = menuService.getUserPermissions(user.getId());
        LoginUser loginUser = new LoginUser(user, permissions);

        // 4. 生成 JWT 并缓存
        String token = jwtUtil.generateToken(user.getId());
        redisUtil.set("login:user:" + user.getId(), loginUser, 86400L);

        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        return Result.success(map);
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