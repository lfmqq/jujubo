package com.admin.dto;

import lombok.Data;

/**
 * 前端上报的操作日志参数
 */
@Data
public class FrontendOperLogDto {
    /** 模块标题 */
    private String title;
    /** 业务类型（0其它 1新增 2修改 3删除 4查询 ...） */
    private Integer businessType;
    /** 请求方式 */
    private String requestMethod;
    /** 请求方法（前端：路由名 / API标识） */
    private String method;
    /** 请求URL / 页面路径 */
    private String operUrl;
    /** 操作人员（未登录时由前端传入） */
    private String operName;
    /** 请求参数 */
    private String operParam;
    /** 返回参数 */
    private String jsonResult;
    /** 操作状态（0成功 1失败） */
    private Integer status;
    /** 错误消息 */
    private String errorMsg;
}
