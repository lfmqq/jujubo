package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IoT 设备上报数据
 *
 * @author admin
 */
@Data
@TableName("iot_device_data")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IotDeviceData {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备ID */
    private Long deviceId;

    /** 属性名（如 temperature/humidity） */
    private String propertyName;

    /** 属性值 */
    private String propertyValue;

    /** 数据类型：int/float/string/bool */
    private String dataType;

    /** 数据上报时间 */
    private LocalDateTime reportTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
