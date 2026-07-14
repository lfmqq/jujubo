package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.result.Result;
import com.admin.entity.SysDept;
import com.admin.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/tree")
    public Result<List<SysDept>> tree(@RequestParam(required = false) String deptName) {
        return Result.success(deptService.getDeptTree(deptName));
    }

    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    @PreAuthorize("hasAuthority('system:dept:add')")
    public Result<?> add(@RequestBody SysDept dept) {
        deptService.save(dept);
        return Result.success();
    }

    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @PreAuthorize("hasAuthority('system:dept:edit')")
    public Result<?> update(@RequestBody SysDept dept) {
        deptService.updateById(dept);
        return Result.success();
    }

    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:remove')")
    public Result<?> delete(@PathVariable Long id) {
        deptService.removeById(id);
        return Result.success();
    }
}