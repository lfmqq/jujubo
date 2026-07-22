package com.admin.service;

import com.admin.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<SysUser> {
    /**
     * 手机号或邮箱自动注册（验证码登录时，账号不存在则自动创建）
     * @param account 手机号或邮箱
     * @param type    sms-短信(手机号), email-邮箱
     * @return 注册成功后的用户
     */
    SysUser registerByPhoneOrEmail(String account, String type);
}