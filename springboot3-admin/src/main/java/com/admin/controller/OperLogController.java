package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.enums.OperatorType;
import com.admin.common.result.Result;
import com.admin.common.security.LoginUser;
import com.admin.common.util.IpUtil;
import com.admin.dto.FrontendOperLogDto;
import com.admin.entity.SysOperLog;
import com.admin.service.OperLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志 Controller
 */
@Slf4j
@RestController
@RequestMapping("/monitor/operlog")
@RequiredArgsConstructor
public class OperLogController {

    private static final int MAX_LENGTH = 2000;

    private final OperLogService operLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('monitor:operlog:list')")
    public Result<IPage<SysOperLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String operName,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer operatorType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(SysOperLog::getTitle, title);
        }
        if (StringUtils.hasText(operName)) {
            wrapper.like(SysOperLog::getOperName, operName);
        }
        if (businessType != null) {
            wrapper.eq(SysOperLog::getBusinessType, businessType);
        }
        if (status != null) {
            wrapper.eq(SysOperLog::getStatus, status);
        }
        if (operatorType != null) {
            wrapper.eq(SysOperLog::getOperatorType, operatorType);
        }
        if (beginTime != null) {
            wrapper.ge(SysOperLog::getOperTime, beginTime);
        }
        if (endTime != null) {
            wrapper.le(SysOperLog::getOperTime, endTime);
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);

        Page<SysOperLog> page = new Page<>(pageNum, pageSize);
        return Result.success(operLogService.page(page, wrapper));
    }

    /**
     * 日志详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('monitor:operlog:list')")
    public Result<SysOperLog> detail(@PathVariable Long id) {
        return Result.success(operLogService.getById(id));
    }

    /**
     * 删除单条日志
     */
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('monitor:operlog:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        operLogService.removeById(id);
        return Result.success();
    }

    /**
     * 清空所有日志
     */
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    @PreAuthorize("hasAuthority('monitor:operlog:clean')")
    public Result<Void> clean() {
        operLogService.cleanOperLog();
        return Result.success();
    }

    /**
     * 接收前端上报的操作日志（前端用户行为 / 页面访问 / 请求异常）
     * 由前端埋点调用，自动补全真实 IP 与操作人员。
     */
    @PostMapping("/frontend")
    public Result<Void> receiveFrontendLog(@RequestBody FrontendOperLogDto dto) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        SysOperLog log = new SysOperLog();
        log.setOperTime(LocalDateTime.now());
        log.setTitle(dto.getTitle() != null ? dto.getTitle() : "前端操作");
        log.setBusinessType(dto.getBusinessType() != null ? dto.getBusinessType() : 0);
        log.setOperatorType(OperatorType.FRONTEND.getCode());
        log.setRequestMethod(dto.getRequestMethod());
        log.setMethod(dto.getMethod());
        log.setOperUrl(dto.getOperUrl());
        String ip = IpUtil.getIpAddr(request);
        log.setOperIp(ip);
        log.setOperLocation(IpUtil.getRealAddressByIp(ip));
        log.setOperParam(subStr(dto.getOperParam(), MAX_LENGTH));
        log.setJsonResult(subStr(dto.getJsonResult(), MAX_LENGTH));
        log.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        log.setErrorMsg(subStr(dto.getErrorMsg(), MAX_LENGTH));

        // 操作人员：优先取当前登录用户，否则用前端传入的用户名
        String operName = currentUsername();
        if (operName == null) {
            operName = dto.getOperName();
        }
        log.setOperName(operName);

        operLogService.save(log);
        return Result.success();
    }

    private String currentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser
                    && loginUser.getSysUser() != null) {
                return loginUser.getSysUser().getUsername();
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    private String subStr(String str, int max) {
        if (str == null) {
            return null;
        }
        return str.length() > max ? str.substring(0, max) : str;
    }
}
