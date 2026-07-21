package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.result.Result;
import com.admin.entity.SysJob;
import com.admin.entity.SysJobLog;
import com.admin.mapper.SysJobLogMapper;
import com.admin.service.impl.SysJobServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务管理
 */
@RestController
@RequestMapping("/monitor/job")
@RequiredArgsConstructor
public class SysJobController {

    private final SysJobServiceImpl jobService;
    private final SysJobLogMapper jobLogMapper;

    /* ===================== 任务 CRUD ===================== */

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('monitor:job:list')")
    public Result<IPage<SysJob>> page(Long pageNum, Long pageSize,
                                      @RequestParam(required = false) String jobName,
                                      @RequestParam(required = false) Integer status) {
        Page<SysJob> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
        if (jobName != null && !jobName.isBlank()) wrapper.like(SysJob::getJobName, jobName);
        if (status != null) wrapper.eq(SysJob::getStatus, status);
        wrapper.orderByDesc(SysJob::getCreateTime);
        return Result.success(jobService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<SysJob> getInfo(@PathVariable Long id) {
        return Result.success(jobService.getById(id));
    }

    @PostMapping
    @Log(title = "定时任务", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('monitor:job:add')")
    public Result<Void> add(@RequestBody SysJob job) {
        jobService.save(job);
        if (job.getStatus() == 1) {
            jobService.startJob(job);
        }
        return Result.success();
    }

    @PutMapping
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    public Result<Void> update(@RequestBody SysJob job) {
        jobService.updateById(job);
        // 重新调度
        if (job.getStatus() == 1) {
            jobService.startJob(job);
        } else {
            jobService.stopJob(job.getId());
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('monitor:job:remove')")
    public Result<Void> remove(@PathVariable Long id) {
        jobService.stopJob(id);
        jobService.removeById(id);
        return Result.success();
    }

    /** 暂停 */
    @PutMapping("/pause/{id}")
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    public Result<Void> pause(@PathVariable Long id) {
        SysJob job = jobService.getById(id);
        job.setStatus(0);
        jobService.updateById(job);
        jobService.stopJob(id);
        return Result.success();
    }

    /** 恢复 */
    @PutMapping("/resume/{id}")
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    public Result<Void> resume(@PathVariable Long id) {
        SysJob job = jobService.getById(id);
        job.setStatus(1);
        jobService.updateById(job);
        jobService.startJob(job);
        return Result.success();
    }

    /** 立即执行一次 */
    @PostMapping("/run/{id}")
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    public Result<Void> run(@PathVariable Long id) {
        jobService.run(id);
        return Result.success();
    }

    /* ===================== 执行日志 ===================== */

    @GetMapping("/log/page")
    public Result<IPage<SysJobLog>> logPage(Long pageNum, Long pageSize,
                                            @RequestParam(required = false) Long jobId,
                                            @RequestParam(required = false) Integer status) {
        Page<SysJobLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysJobLog> wrapper = new LambdaQueryWrapper<>();
        if (jobId != null) wrapper.eq(SysJobLog::getJobId, jobId);
        if (status != null) wrapper.eq(SysJobLog::getStatus, status);
        wrapper.orderByDesc(SysJobLog::getExecTime);
        return Result.success(jobLogMapper.selectPage(page, wrapper));
    }

    @DeleteMapping("/log/{id}")
    @PreAuthorize("hasAuthority('monitor:job:remove')")
    public Result<Void> removeLog(@PathVariable Long id) {
        jobLogMapper.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/log/clean")
    @PreAuthorize("hasAuthority('monitor:job:remove')")
    public Result<Void> cleanLog() {
        jobLogMapper.delete(new LambdaQueryWrapper<>());
        return Result.success();
    }
}
