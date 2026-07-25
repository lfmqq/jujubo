package com.admin.service.impl;

import com.admin.entity.SysDictData;
import com.admin.mapper.SysDictDataMapper;
import com.admin.service.DictDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements DictDataService {
}
