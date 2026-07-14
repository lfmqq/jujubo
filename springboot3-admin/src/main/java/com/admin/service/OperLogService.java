package com.admin.service;

import com.admin.entity.SysOperLog;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OperLogService extends IService<SysOperLog> {

    /** 清空所有操作日志 */
    void cleanOperLog();
}
