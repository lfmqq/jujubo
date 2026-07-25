package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.result.ResultCodeEnum;
import com.admin.entity.SysDictData;
import com.admin.entity.SysDictType;
import com.admin.common.result.Result;
import com.admin.service.DictDataService;
import com.admin.service.DictTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictTypeService dictTypeService;
    private final DictDataService dictDataService;

    // ==================== 字典类型 ====================

    /**
     * 字典类型全量列表（不分页，供前端左侧分类面板使用）
     */
    @GetMapping("/type/list")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public Result<List<SysDictType>> typeList(
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysDictType::getTypeName, keyword)
                    .or().like(SysDictType::getTypeCode, keyword));
        }
        wrapper.orderByDesc(SysDictType::getCreateTime);
        return Result.success(dictTypeService.list(wrapper));
    }

    @GetMapping("/type/page")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public Result<Page<SysDictType>> typePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(typeName)) wrapper.like(SysDictType::getTypeName, typeName);
        if (StringUtils.hasText(typeCode)) wrapper.like(SysDictType::getTypeCode, typeCode);
        if (status != null) wrapper.eq(SysDictType::getStatus, status);
        wrapper.orderByDesc(SysDictType::getCreateTime);
        return Result.success(dictTypeService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    @GetMapping("/type/{id}")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public Result<SysDictType> typeInfo(@PathVariable Long id) {
        return Result.success(dictTypeService.getById(id));
    }

    @PostMapping("/type")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "新增字典类型")
    public Result<Void> typeAdd(@RequestBody SysDictType dictType) {
        // 编码唯一性校验
        long exists = dictTypeService.count(
                new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getTypeCode, dictType.getTypeCode())
        );
        if (exists > 0) {
            return Result.fail(ResultCodeEnum.FAIL.getCode(), "字典编码已存在");
        }
        dictTypeService.save(dictType);
        return Result.success();
    }

    @PutMapping("/type")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "修改字典类型")
    public Result<Void> typeEdit(@RequestBody SysDictType dictType) {
        // 编码唯一性校验（排除自身）
        long exists = dictTypeService.count(
                new LambdaQueryWrapper<SysDictType>()
                        .eq(SysDictType::getTypeCode, dictType.getTypeCode())
                        .ne(SysDictType::getId, dictType.getId())
        );
        if (exists > 0) {
            return Result.fail(ResultCodeEnum.FAIL.getCode(), "字典编码已存在");
        }
        dictTypeService.updateById(dictType);
        return Result.success();
    }

    @DeleteMapping("/type/{id}")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "删除字典类型")
    public Result<Void> typeRemove(@PathVariable Long id) {
        SysDictType type = dictTypeService.getById(id);
        if (type != null) {
            // 同时删除该类型下的字典数据
            dictDataService.remove(
                    new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getTypeCode, type.getTypeCode())
            );
            dictTypeService.removeById(id);
        }
        return Result.success();
    }

    // ==================== 字典数据 ====================

    @GetMapping("/data/page")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public Result<Page<SysDictData>> dataPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam String typeCode,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getTypeCode, typeCode);
        if (StringUtils.hasText(label)) wrapper.like(SysDictData::getLabel, label);
        if (status != null) wrapper.eq(SysDictData::getStatus, status);
        wrapper.orderByAsc(SysDictData::getSort);
        return Result.success(dictDataService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    @GetMapping("/data/{id}")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public Result<SysDictData> dataInfo(@PathVariable Long id) {
        return Result.success(dictDataService.getById(id));
    }

    @PostMapping("/data")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "新增字典数据")
    public Result<Void> dataAdd(@RequestBody SysDictData dictData) {
        dictDataService.save(dictData);
        return Result.success();
    }

    @PutMapping("/data")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "修改字典数据")
    public Result<Void> dataEdit(@RequestBody SysDictData dictData) {
        dictDataService.updateById(dictData);
        return Result.success();
    }

    @DeleteMapping("/data/{id}")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "删除字典数据")
    public Result<Void> dataRemove(@PathVariable Long id) {
        dictDataService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除字典数据
     */
    @DeleteMapping("/data/batch")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "批量删除字典数据")
    public Result<Void> dataBatchRemove(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ResultCodeEnum.FAIL.getCode(), "请选择要删除的数据");
        }
        dictDataService.removeByIds(ids);
        return Result.success();
    }
}
