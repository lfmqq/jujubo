package com.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代码生成 - 表信息
 */
@Data
@TableName("gen_table")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenTable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 表名 */
    private String tableName;

    /** 表描述 */
    private String tableComment;

    /** 实体类名（首字母大写驼峰） */
    private String className;

    /** 包路径 */
    private String packageName;

    /** 模块名 */
    private String moduleName;

    /** 业务名 */
    private String businessName;

    /** 功能名 */
    private String functionName;

    /** 作者 */
    private String functionAuthor;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
}
