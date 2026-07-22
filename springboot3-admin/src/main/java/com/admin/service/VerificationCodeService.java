package com.admin.service;

/**
 * 验证码服务（短信/邮箱）
 */
public interface VerificationCodeService {

    /**
     * 发送验证码
     * @param account 手机号或邮箱
     * @param type    sms / email
     * @return 生成的验证码
     */
    String sendCode(String account, String type);

    /**
     * 校验验证码（校验通过后立即删除）
     * @param account 手机号或邮箱
     * @param type    sms / email
     * @param code    用户输入的验证码
     */
    void validateCode(String account, String type, String code);

    /**
     * 判断是否处于降级模式（验证码仅打印日志，未真实发送）
     * @param type sms / email
     */
    boolean isDegradeMode(String type);
}
