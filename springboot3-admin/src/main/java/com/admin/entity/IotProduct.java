package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IoT 产品（物模型产品定义）
 *
 * @author admin
 */
@Data
@TableName("iot_product")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IotProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 产品名称 */
    private String productName;

    /** 产品标识（唯一，如 "temp_sensor_v1"） */
    private String productKey;

    /** 产品描述 */
    private String description;

    /** 设备类型：sensor=传感器, actuator=执行器, gateway=网关 */
    private String deviceType;

    /** 通信协议：mqtt/http/coap/tcp */
    private String protocolType;

    /** 数据格式：json/custom */
    private String dataFormat;

    /** 状态：1=启用，0=禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
