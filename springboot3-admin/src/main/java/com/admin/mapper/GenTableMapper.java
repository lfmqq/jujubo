package com.admin.mapper;

import com.admin.entity.GenTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 代码生成 Mapper
 */
public interface GenTableMapper extends BaseMapper<GenTable> {

    /** 查询数据库中的所有表（排除 sys_ / gen_ 前缀） */
    @Select("SELECT table_name, table_comment, create_time " +
            "FROM information_schema.tables " +
            "WHERE table_schema = (SELECT DATABASE()) " +
            "AND table_name NOT LIKE 'sys_%' AND table_name NOT LIKE 'gen_%' " +
            "ORDER BY create_time DESC")
    List<Map<String, Object>> selectDbTables();

    /** 查询表的列信息 */
    @Select("SELECT column_name, column_comment, data_type, column_key, " +
            "is_nullable, character_maximum_length, ordinal_position " +
            "FROM information_schema.columns " +
            "WHERE table_schema = (SELECT DATABASE()) AND table_name = #{tableName} " +
            "ORDER BY ordinal_position")
    List<Map<String, Object>> selectDbColumns(@Param("tableName") String tableName);

    /** 按表名查询表信息 */
    @Select("SELECT table_name, table_comment, create_time " +
            "FROM information_schema.tables " +
            "WHERE table_schema = (SELECT DATABASE()) AND table_name = #{tableName}")
    Map<String, Object> selectDbTableByName(@Param("tableName") String tableName);
}
