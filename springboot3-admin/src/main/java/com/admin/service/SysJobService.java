package com.admin.service;

import com.admin.entity.SysJob;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysJobService extends IService<SysJob> {

    /** 立即执行一次 */
    void run(Long jobId);
}
