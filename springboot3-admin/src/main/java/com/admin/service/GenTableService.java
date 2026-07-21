package com.admin.service;

import com.admin.entity.GenTable;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 代码生成 Service
 */
public interface GenTableService extends IService<GenTable> {

    /** 查询数据库表列表 */
    List<Map<String, Object>> selectDbTables();

    /** 查询表列信息 */
    List<Map<String, Object>> selectDbColumns(String tableName);

    /** 导入表 */
    void importTable(String tableName);

    /** 预览生成代码 */
    Map<String, String> preview(Long tableId);

    /** 生成代码到本地 */
    byte[] download(Long tableId);
}
