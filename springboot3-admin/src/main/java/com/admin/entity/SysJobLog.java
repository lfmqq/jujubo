package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务执行日志
 *
 * @author admin
 */
@Data
@TableName("sys_job_log")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysJobLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务ID */
    private Long jobId;

    /** 任务名称 */
    private String jobName;

    /** 调用目标 */
    private String invokeTarget;

    /** 执行状态：0=成功，1=失败 */
    private Integer status;

    /** 执行耗时（毫秒） */
    private Long duration;

    /** 异常信息 */
    private String errorMsg;

    /** 执行时间 */
    private LocalDateTime execTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
