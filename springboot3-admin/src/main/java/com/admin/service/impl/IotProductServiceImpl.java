package com.admin.service.impl;

import com.admin.entity.IotProduct;
import com.admin.mapper.IotProductMapper;
import com.admin.service.IotProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IotProductServiceImpl extends ServiceImpl<IotProductMapper, IotProduct> implements IotProductService {
}
