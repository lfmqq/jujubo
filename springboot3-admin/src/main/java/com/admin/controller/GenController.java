package com.admin.controller;

import com.admin.common.result.Result;
import com.admin.entity.GenTable;
import com.admin.service.GenTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 代码生成控制器
 */
@RestController
@RequestMapping("/tool/gen")
@RequiredArgsConstructor
public class GenController {

    private final GenTableService genTableService;

    /** 查询数据库表列表 */
    @GetMapping("/db/list")
    public Result<List<Map<String, Object>>> dbList() {
        return Result.success(genTableService.selectDbTables());
    }

    /** 导入表 */
    @PostMapping("/import")
    public Result<Void> importTable(@RequestBody Map<String, String> body) {
        String tableName = body.get("tableName");
        if (tableName == null || tableName.isBlank()) {
            return Result.fail(400, "表名不能为空");
        }
        genTableService.importTable(tableName);
        return Result.success();
    }

    /** 已导入表列表 */
    @GetMapping("/list")
    public Result<List<GenTable>> list() {
        return Result.success(genTableService.list());
    }

    /** 预览代码 */
    @GetMapping("/preview/{tableId}")
    public Result<Map<String, String>> preview(@PathVariable Long tableId) {
        return Result.success(genTableService.preview(tableId));
    }

    /** 下载代码 */
    @GetMapping("/download/{tableId}")
    public ResponseEntity<byte[]> download(@PathVariable Long tableId) {
        byte[] data = genTableService.download(tableId);
        GenTable table = genTableService.getById(tableId);
        String filename = (table != null ? table.getClassName() : "code") + ".zip";
        String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    /** 删除已导入表 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        genTableService.removeById(id);
        return Result.success();
    }
}
