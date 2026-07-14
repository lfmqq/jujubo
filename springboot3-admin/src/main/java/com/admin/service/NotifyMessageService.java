package com.admin.service;

import com.admin.entity.SysNotifyMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 消息通知 Service 接口
 */
public interface NotifyMessageService extends IService<SysNotifyMessage> {

    /**
     * 获取当前用户的未读消息数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记单条消息为已读
     */
    boolean markRead(Long id, Long userId);

    /**
     * 标记当前用户全部消息为已读
     */
    boolean markAllRead(Long userId);

    /**
     * 发送通知
     */
    void sendNotify(String title, String content, Integer type, Long senderId, Long receiverId);
}
