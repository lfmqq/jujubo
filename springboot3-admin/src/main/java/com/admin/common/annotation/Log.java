package com.admin.common.annotation;

import com.admin.common.enums.BusinessType;
import com.admin.common.enums.OperatorType;

import java.lang.annotation.*;

/**
 * 操作日志注解：标注在 Controller 方法上，由 {@link com.admin.aspect.LogAspect} 自动记录操作日志。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /** 模块标题，如「用户管理」 */
    String title() default "";

    /** 业务类型（新增/修改/删除/查询...） */
    BusinessType businessType() default BusinessType.OTHER;

    /** 操作人类别（后台/前端/手机端） */
    OperatorType operatorType() default OperatorType.MANAGE;

    /** 是否保存请求参数 */
    boolean isSaveRequestData() default true;

    /** 是否保存响应结果 */
    boolean isSaveResponseData() default true;
}
