package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Data
@TableName("sys_notify_message")
public class SysNotifyMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 通知类型：1=系统通知, 2=提醒, 3=私信 */
    private Integer type;

    /** 发送者ID（0=系统） */
    private Long senderId;

    /** 接收者ID（0=全部用户） */
    private Long receiverId;

    /** 阅读状态：0=未读, 1=已读 */
    private Integer readStatus;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 状态：0=正常, 1=已删除 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
