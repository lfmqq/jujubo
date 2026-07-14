package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.result.Result;
import com.admin.common.result.ResultCodeEnum;
import com.admin.common.util.SecurityUtil;
import com.admin.entity.SysNotifyMessage;
import com.admin.service.NotifyMessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知 Controller
 */
@Slf4j
@RestController
@RequestMapping("/system/notify")
@RequiredArgsConstructor
public class NotifyMessageController {

    private final NotifyMessageService notifyMessageService;

    /**
     * 分页查询通知列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:notify:list')")
    public Result<Page<SysNotifyMessage>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer readStatus) {

        Long userId = SecurityUtil.getUserId();
        log.info("查询通知列表: userId={}, pageNum={}, pageSize={}, readStatus={}", userId, pageNum, pageSize, readStatus);

        LambdaQueryWrapper<SysNotifyMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotifyMessage::getStatus, 0);

        if (title != null && !title.isBlank()) {
            wrapper.like(SysNotifyMessage::getTitle, title);
        }
        if (type != null) {
            wrapper.eq(SysNotifyMessage::getType, type);
        }
        if (readStatus != null) {
            wrapper.eq(SysNotifyMessage::getReadStatus, readStatus);
        }

        wrapper.orderByDesc(SysNotifyMessage::getCreateTime);

        Page<SysNotifyMessage> page = new Page<>(pageNum, pageSize);
        return Result.success(notifyMessageService.page(page, wrapper));
    }

    /**
     * 获取当前用户未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long userId = SecurityUtil.getUserId();
        long count = notifyMessageService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:notify:list')")
    public Result<SysNotifyMessage> detail(@PathVariable Long id) {
        return Result.success(notifyMessageService.getById(id));
    }

    /**
     * 新增通知
     */
    @Log(title = "通知管理", businessType = BusinessType.INSERT)
    @PostMapping
    @PreAuthorize("hasAuthority('system:notify:add')")
    public Result<Void> add(@RequestBody SysNotifyMessage notify) {
        notifyMessageService.sendNotify(
                notify.getTitle(),
                notify.getContent(),
                notify.getType(),
                notify.getSenderId(),
                notify.getReceiverId()
        );
        return Result.success();
    }

    /**
     * 标记单条为已读
     */
    @PutMapping("/read/{id}")
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = SecurityUtil.getUserId();
        boolean result = notifyMessageService.markRead(id, userId);
        if (!result) {
            return Result.fail(ResultCodeEnum.FAIL.getCode(), "标记已读失败，消息不存在或无权限");
        }
        return Result.success();
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        Long userId = SecurityUtil.getUserId();
        boolean result = notifyMessageService.markAllRead(userId);
        if (!result) {
            return Result.fail(ResultCodeEnum.FAIL.getCode(), "全部已读失败");
        }
        return Result.success();
    }

    /**
     * 删除通知（逻辑删除）
     */
    @Log(title = "通知管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:notify:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        SysNotifyMessage msg = new SysNotifyMessage();
        msg.setId(id);
        msg.setStatus(1);
        notifyMessageService.updateById(msg);
        return Result.success();
    }
}
