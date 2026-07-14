package com.admin.controller;

import com.admin.common.result.Result;
import com.admin.common.util.CaptchaUtil;
import com.admin.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 验证码接口：后台生成图片验证码，明文存入 Redis（key=uuid，有效期 2 分钟），
 * 返回 uuid 与 base64 图片给前端；登录时由 AuthController 校验。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class CaptchaController {

    private final RedisUtil redisUtil;

    /** 验证码在 Redis 中的有效期（秒） */
    private static final long CAPTCHA_EXPIRE = 120;

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        CaptchaUtil.CaptchaInfo info = CaptchaUtil.generate();
        String uuid = UUID.randomUUID().toString();
        // 统一转大写存储，登录时忽略大小写比对
        redisUtil.set("captcha:" + uuid, info.getCode().toUpperCase(), CAPTCHA_EXPIRE);

        Map<String, String> map = new HashMap<>();
        map.put("uuid", uuid);
        map.put("img", info.getBase64());
        return Result.success(map);
    }
}
