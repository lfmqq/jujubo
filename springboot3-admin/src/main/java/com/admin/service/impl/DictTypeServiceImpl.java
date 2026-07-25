package com.admin.service.impl;

import com.admin.entity.SysDictType;
import com.admin.mapper.SysDictTypeMapper;
import com.admin.service.DictTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements DictTypeService {
}
