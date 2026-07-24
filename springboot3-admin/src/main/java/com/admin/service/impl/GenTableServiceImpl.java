package com.admin.service.impl;

import com.admin.entity.GenTable;
import com.admin.mapper.GenTableMapper;
import com.admin.service.GenTableService;
import com.admin.util.GenUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class GenTableServiceImpl extends ServiceImpl<GenTableMapper, GenTable> implements GenTableService {

    private final GenTableMapper genTableMapper;

    @Override
    public List<Map<String, Object>> selectDbTables() {
        return genTableMapper.selectDbTables().stream()
                .map(this::toCamelCaseMap)
                .toList();
    }

    private Map<String, Object> toCamelCaseMap(Map<String, Object> map) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(GenUtils.toCamelCase(entry.getKey(), false), entry.getValue());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> selectDbColumns(String tableName) {
        return genTableMapper.selectDbColumns(tableName);
    }

    @Override
    public void importTable(String tableName) {
        Map<String, Object> rawTable = genTableMapper.selectDbTableByName(tableName);
        if (rawTable == null) return;
        Map<String, Object> table = toCamelCaseMap(rawTable);

        // 检查是否已导入
        GenTable exist = baseMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GenTable>()
                        .eq(GenTable::getTableName, tableName));
        if (exist != null) return;

        GenTable gen = new GenTable();
        gen.setTableName(tableName);
        gen.setTableComment(String.valueOf(table.getOrDefault("tableComment", "")));
        gen.setClassName(GenUtils.toCamelCase(tableName, true));
        gen.setPackageName("com.admin");
        gen.setModuleName(tableName.contains("_") ? tableName.substring(0, tableName.indexOf("_")) : "system");
        gen.setBusinessName(GenUtils.toCamelCase(tableName.replaceFirst("^[a-z]+_", ""), false));
        gen.setFunctionName(String.valueOf(table.getOrDefault("tableComment", tableName)));
        gen.setFunctionAuthor("admin");
        gen.setCreateTime(java.time.LocalDateTime.now());
        baseMapper.insert(gen);
    }

    @Override
    public Map<String, String> preview(Long tableId) {
        GenTable table = baseMapper.selectById(tableId);
        if (table == null) return Collections.emptyMap();

        List<Map<String, Object>> columns = genTableMapper.selectDbColumns(table.getTableName());
        Map<String, String> result = new LinkedHashMap<>();

        // 模板引擎简单字符串替换
        result.put("domain/" + table.getClassName() + ".java", GenUtils.renderEntity(table, columns));
        result.put("mapper/" + table.getClassName() + "Mapper.java", GenUtils.renderMapper(table, columns));
        result.put("service/" + table.getClassName() + "Service.java", GenUtils.renderService(table, columns));
        result.put("service/impl/" + table.getClassName() + "ServiceImpl.java", GenUtils.renderServiceImpl(table, columns));
        result.put("controller/" + table.getClassName() + "Controller.java", GenUtils.renderController(table, columns));
        result.put("vue/" + GenUtils.toKebabCase(table.getBusinessName()) + "/index.vue", GenUtils.renderVue(table, columns));
        result.put("sql/" + table.getTableName() + ".sql", GenUtils.renderSql(table, columns));
        return result;
    }

    @Override
    public byte[] download(Long tableId) {
        Map<String, String> files = preview(tableId);
        if (files.isEmpty()) return new byte[0];

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos)) {
            for (Map.Entry<String, String> entry : files.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zos.closeEntry();
            }
            zos.finish();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成代码失败", e);
        }
    }
}
