package com.admin.service;

import com.admin.entity.SysDept;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DeptService extends IService<SysDept> {
    /**
     * 获取部门树，支持按部门名称模糊搜索
     */
    List<SysDept> getDeptTree(String deptName);
}