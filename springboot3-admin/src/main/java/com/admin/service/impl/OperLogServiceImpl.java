package com.admin.service.impl;

import com.admin.entity.SysOperLog;
import com.admin.mapper.SysOperLogMapper;
import com.admin.service.OperLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements OperLogService {

    @Override
    public void cleanOperLog() {
        // 不带条件删除全部记录
        baseMapper.delete(null);
    }
}
