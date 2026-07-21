package com.admin.service.impl;

import com.admin.entity.IotDevice;
import com.admin.mapper.IotDeviceMapper;
import com.admin.service.IotDeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IotDeviceServiceImpl extends ServiceImpl<IotDeviceMapper, IotDevice> implements IotDeviceService {
}
