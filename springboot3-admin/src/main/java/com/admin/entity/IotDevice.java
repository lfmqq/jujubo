package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IoT 设备
 *
 * @author admin
 */
@Data
@TableName("iot_device")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IotDevice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备名称 */
    private String deviceName;

    /** 设备标识（唯一，如设备 SN/MAC） */
    private String deviceKey;

    /** 设备密钥（用于认证） */
    private String deviceSecret;

    /** 所属产品ID */
    private Long productId;

    /** 设备状态：0=未激活, 1=在线, 2=离线 */
    private Integer status;

    /** 最后上线时间 */
    private LocalDateTime lastOnlineTime;

    /** 设备描述 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /* ---------- 非数据库字段 ---------- */

    /** 所属产品名称（联表查询用） */
    @TableField(exist = false)
    private String productName;
}
