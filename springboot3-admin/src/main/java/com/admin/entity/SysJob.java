package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务
 *
 * @author admin
 */
@Data
@TableName("sys_job")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysJob {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务名称 */
    private String jobName;

    /** 任务组 */
    private String jobGroup;

    /** cron 表达式 */
    private String cronExpression;

    /** 调用目标字符串（beanName.method） */
    private String invokeTarget;

    /** 状态：1=运行，0=暂停 */
    private Integer status;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
