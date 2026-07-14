package com.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志记录
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模块标题 */
    private String title;

    /** 业务类型（0其它 1新增 2修改 3删除 4查询 5登录 6退出 ...） */
    private Integer businessType;

    /** 操作人类别（0后台 1前端 2手机端） */
    private Integer operatorType;

    /** 请求方法（类.方法） */
    private String method;

    /** 请求方式（GET/POST...） */
    private String requestMethod;

    /** 操作人员 */
    private String operName;

    /** 部门名称 */
    private String deptName;

    /** 请求URL */
    private String operUrl;

    /** 主机地址（真实IP） */
    private String operIp;

    /** IP归属地 */
    private String operLocation;

    /** 请求参数 */
    private String operParam;

    /** 返回参数 */
    private String jsonResult;

    /** 操作状态（0成功 1失败） */
    private Integer status;

    /** 错误消息 */
    private String errorMsg;

    /** 操作时间 */
    private LocalDateTime operTime;

    /** 业务类型描述（不入库） */
    @TableField(exist = false)
    private String businessTypeDesc;

    /** 操作人类别描述（不入库） */
    @TableField(exist = false)
    private String operatorTypeDesc;
}
