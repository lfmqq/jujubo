package com.admin.service.impl;

import com.admin.entity.SysDept;
import com.admin.mapper.SysDeptMapper;
import com.admin.service.DeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements DeptService {

    @Override
    public List<SysDept> getDeptTree(String deptName) {
        List<SysDept> all = list();
        if (deptName == null || deptName.isBlank()) {
            return buildTree(all, 0L);
        }
        // 按名称过滤 + 追溯父级保留树结构
        List<SysDept> filtered = filterByDeptName(all, deptName);
        return buildTree(filtered, 0L);
    }

    /**
     * 按部门名称模糊过滤，并自动补全匹配节点的所有祖先节点
     */
    private List<SysDept> filterByDeptName(List<SysDept> allDepts, String keyword) {
        Map<Long, SysDept> deptMap = allDepts.stream()
                .collect(Collectors.toMap(SysDept::getId, m -> m, (a, b) -> a));
        Set<Long> matchIds = new HashSet<>();
        for (SysDept dept : allDepts) {
            if (dept.getDeptName() != null && dept.getDeptName().contains(keyword)) {
                matchIds.add(dept.getId());
                // 向上追溯祖先
                SysDept current = dept;
                while (current.getParentId() != null && current.getParentId() != 0) {
                    matchIds.add(current.getParentId());
                    current = deptMap.get(current.getParentId());
                    if (current == null) break;
                }
            }
        }
        return allDepts.stream()
                .filter(m -> matchIds.contains(m.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 递归构建部门树形结构
     */
    private List<SysDept> buildTree(List<SysDept> all, Long parentId) {
        return all.stream()
                .filter(dept -> dept.getParentId().equals(parentId))
                .peek(dept -> dept.setChildren(buildTree(all, dept.getId())))
                .collect(Collectors.toList());
    }
}