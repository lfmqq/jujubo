package com.admin.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.admin.common.exception.ServiceException;
import com.admin.common.util.RedisUtil;
import com.admin.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 验证码服务实现
 * <p>
 * 短信：通过控制台打印模拟发送（可替换为阿里云/腾讯云等 SMS SDK）
 * 邮箱：通过 Spring Mail 真实发送（未配置邮件时降级为控制台打印）
 */
@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final RedisUtil redisUtil;

    /** 注入可能为 null（未配置邮件时 Spring 不创建该 Bean） */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    /** 腾讯云短信配置（均未配置时处于降级模式，验证码仅打日志） */
    @Value("${sms.tencent.secret-id:}")
    private String smsSecretId;
    @Value("${sms.tencent.secret-key:}")
    private String smsSecretKey;
    @Value("${sms.tencent.sdk-app-id:}")
    private String smsSdkAppId;

    public VerificationCodeServiceImpl(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /** 验证码有效期（秒） */
    private static final long CODE_EXPIRE = 300L;
    /** 发送间隔限制（秒），防止频繁发送 */
    private static final long SEND_INTERVAL = 60L;

    @Override
    public String sendCode(String account, String type) {
        // 发送频率限制
        String intervalKey = "code:interval:" + type + ":" + account;
        if (redisUtil.hasKey(intervalKey)) {
            throw new ServiceException(500, "发送过于频繁，请" + SEND_INTERVAL + "秒后再试");
        }

        // 生成 6 位数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 存入 Redis
        String codeKey = "code:" + type + ":" + account;
        redisUtil.set(codeKey, code, CODE_EXPIRE);
        // 发送间隔标记
        redisUtil.set(intervalKey, "1", SEND_INTERVAL);

        // 发送验证码
        if ("sms".equals(type)) {
            sendSms(account, code);
        } else if ("email".equals(type)) {
            sendEmail(account, code);
        } else {
            throw new ServiceException(500, "不支持的验证码类型");
        }

        log.info("验证码已发送 -> type={}, account={}, code={}", type, account, code);
        return code;
    }

    /**
     * 判断指定类型是否处于降级模式（验证码仅打印日志，未真实发送）
     */
    @Override
    public boolean isDegradeMode(String type) {
        if ("sms".equals(type)) {
            return !isSmsConfigured();
        } else if ("email".equals(type)) {
            return mailSender == null;
        }
        return true;
    }

    @Override
    public void validateCode(String account, String type, String code) {
        String key = "code:" + type + ":" + account;
        Object cached = redisUtil.get(key);
        if (cached == null) {
            throw new ServiceException(500, "验证码已过期，请重新获取");
        }
        // 无论对错，一次性使用后删除
        redisUtil.delete(key);
        if (!cached.toString().equals(code == null ? "" : code.trim())) {
            throw new ServiceException(500, "验证码错误");
        }
    }

    /**
     * 发送短信验证码（模拟实现，打印到控制台）
     * <p>
     * 接入真实 SMS 时替换此处即可，例如：
     * - 阿里云短信：com.aliyun.dysmsapi20170525
     * - 腾讯云短信：com.tencentcloudapi.sms
     */
    private void sendSms(String phone, String code) {
        log.info("========== 短信验证码（模拟） ==========");
        log.info("  手机号: {}", phone);
        log.info("  验证码: {}", code);
        log.info("  有效期: {} 秒", CODE_EXPIRE);
        log.info("========================================");
        // TODO: 接入真实短信服务后替换此处
    }

    /**
     * 发送邮箱验证码（真实发送，未配置邮件时降级为控制台打印）
     */
    private void sendEmail(String email, String code) {
        if (mailSender == null) {
            // 未配置邮件服务，降级为控制台打印
            log.info("========== 邮箱验证码（邮件未配置，降级打印） ==========");
            log.info("  邮箱: {}", email);
            log.info("  验证码: {}", code);
            log.info("  有效期: {} 秒", CODE_EXPIRE);
            log.info("===========================================================");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("【桔桔波管理系统】登录验证码");
            message.setText("您的验证码是：" + code + "，有效期为 " + (CODE_EXPIRE / 60) + " 分钟。\n\n"
                    + "如非本人操作，请忽略此邮件。\n\n"
                    + "桔桔波管理系统");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败 -> email={}", email, e);
            throw new ServiceException(500, "邮件发送失败，请检查邮箱配置");
        }
    }

    /**
     * 判断短信服务是否已配置（腾讯云四项参数均已填写）
     */
    private boolean isSmsConfigured() {
        return smsSecretId != null && !smsSecretId.isBlank()
                && smsSecretKey != null && !smsSecretKey.isBlank()
                && smsSdkAppId != null && !smsSdkAppId.isBlank();
    }
}
