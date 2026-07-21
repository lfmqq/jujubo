package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.result.Result;
import com.admin.entity.IotProduct;
import com.admin.service.IotProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IoT 产品管理
 */
@RestController
@RequestMapping("/iot/product")
@RequiredArgsConstructor
public class IotProductController {

    private final IotProductService productService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('iot:product:list')")
    public Result<IPage<IotProduct>> page(Long pageNum, Long pageSize,
                                          @RequestParam(required = false) String productName,
                                          @RequestParam(required = false) String productKey,
                                          @RequestParam(required = false) Integer status) {
        Page<IotProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IotProduct> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(productName)) {
            wrapper.like(IotProduct::getProductName, productName);
        }
        if (StringUtils.hasText(productKey)) {
            wrapper.like(IotProduct::getProductKey, productKey);
        }
        if (status != null) {
            wrapper.eq(IotProduct::getStatus, status);
        }
        wrapper.orderByDesc(IotProduct::getCreateTime);
        return Result.success(productService.page(page, wrapper));
    }

    /** 全部产品列表（用于设备表单下拉选择） */
    @GetMapping("/list")
    public Result<List<IotProduct>> list() {
        LambdaQueryWrapper<IotProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IotProduct::getStatus, 1).orderByDesc(IotProduct::getCreateTime);
        return Result.success(productService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<IotProduct> getInfo(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    @Log(title = "IoT产品", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('iot:product:add')")
    public Result<Void> add(@RequestBody IotProduct product) {
        productService.save(product);
        return Result.success();
    }

    @PutMapping
    @Log(title = "IoT产品", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('iot:product:edit')")
    public Result<Void> update(@RequestBody IotProduct product) {
        productService.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Log(title = "IoT产品", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('iot:product:remove')")
    public Result<Void> remove(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }
}
