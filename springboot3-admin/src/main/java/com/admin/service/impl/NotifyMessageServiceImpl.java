package com.admin.service.impl;

import com.admin.entity.SysNotifyMessage;
import com.admin.mapper.SysNotifyMessageMapper;
import com.admin.service.NotifyMessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 消息通知 Service 实现
 */
@Slf4j
@Service
public class NotifyMessageServiceImpl extends ServiceImpl<SysNotifyMessageMapper, SysNotifyMessage> implements NotifyMessageService {

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<SysNotifyMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotifyMessage::getStatus, 0)
               .eq(SysNotifyMessage::getReadStatus, 0);
        long count = count(wrapper);
        log.info("未读消息数查询: userId={}, count={}", userId, count);
        return count;
    }

    @Override
    public boolean markRead(Long id, Long userId) {
        SysNotifyMessage msg = getById(id);
        if (msg == null || msg.getStatus() == 1) {
            log.warn("标记已读失败: id={}, 消息不存在或已删除", id);
            return false;
        }
        LambdaUpdateWrapper<SysNotifyMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysNotifyMessage::getId, id)
               .set(SysNotifyMessage::getReadStatus, 1)
               .set(SysNotifyMessage::getReadTime, LocalDateTime.now());
        boolean result = update(wrapper);
        log.info("标记已读: id={}, userId={}, result={}", id, userId, result);
        return result;
    }

    @Override
    public boolean markAllRead(Long userId) {
        LambdaUpdateWrapper<SysNotifyMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysNotifyMessage::getStatus, 0)
               .eq(SysNotifyMessage::getReadStatus, 0)
               .set(SysNotifyMessage::getReadStatus, 1)
               .set(SysNotifyMessage::getReadTime, LocalDateTime.now());
        boolean result = update(wrapper);
        log.info("全部已读: userId={}, result={}", userId, result);
        return result;
    }

    @Override
    public void sendNotify(String title, String content, Integer type, Long senderId, Long receiverId) {
        SysNotifyMessage msg = new SysNotifyMessage();
        msg.setTitle(title);
        msg.setContent(content);
        msg.setType(type != null ? type : 1);
        msg.setSenderId(senderId != null ? senderId : 0L);
        msg.setReceiverId(receiverId != null ? receiverId : 0L);
        msg.setReadStatus(0);
        msg.setStatus(0);
        save(msg);
        log.info("发送通知: title={}, receiverId={}", title, receiverId);
    }
}
