package com.admin.controller;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.result.Result;
import com.admin.entity.IotDevice;
import com.admin.mapper.IotDeviceDataMapper;
import com.admin.mapper.IotDeviceMapper;
import com.admin.mapper.IotProductMapper;
import com.admin.service.IotDeviceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT 设备管理
 */
@RestController
@RequestMapping("/iot/device")
@RequiredArgsConstructor
public class IotDeviceController {

    private final IotDeviceService deviceService;
    private final IotDeviceMapper deviceMapper;
    private final IotProductMapper productMapper;
    private final IotDeviceDataMapper deviceDataMapper;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('iot:device:list')")
    public Result<IPage<IotDevice>> page(Long pageNum, Long pageSize,
                                         @RequestParam(required = false) String deviceName,
                                         @RequestParam(required = false) Long productId,
                                         @RequestParam(required = false) Integer status) {
        Page<IotDevice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IotDevice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(deviceName)) {
            wrapper.like(IotDevice::getDeviceName, deviceName);
        }
        if (productId != null) {
            wrapper.eq(IotDevice::getProductId, productId);
        }
        if (status != null) {
            wrapper.eq(IotDevice::getStatus, status);
        }
        wrapper.orderByDesc(IotDevice::getCreateTime);
        IPage<IotDevice> result = deviceService.page(page, wrapper);

        // 填充产品名称
        result.getRecords().forEach(device -> {
            if (device.getProductId() != null) {
                productMapper.selectById(device.getProductId());
                var product = productMapper.selectById(device.getProductId());
                if (product != null) {
                    device.setProductName(product.getProductName());
                }
            }
        });

        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<IotDevice> getInfo(@PathVariable Long id) {
        IotDevice device = deviceService.getById(id);
        if (device != null && device.getProductId() != null) {
            var product = productMapper.selectById(device.getProductId());
            if (product != null) {
                device.setProductName(product.getProductName());
            }
        }
        return Result.success(device);
    }

    /** 设备最近数据 */
    @GetMapping("/{id}/data")
    public Result<List<Map<String, Object>>> getDeviceData(@PathVariable Long id) {
        return Result.success(deviceDataMapper.selectLatestByDevice(id));
    }

    /** 设备统计数据 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("statusCount", deviceMapper.countByStatus());
        data.put("productCount", deviceMapper.countByProduct());
        data.put("dailyCount", deviceMapper.countByDay());
        return Result.success(data);
    }

    @PostMapping
    @Log(title = "IoT设备", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('iot:device:add')")
    public Result<Void> add(@RequestBody IotDevice device) {
        // 默认未激活
        if (device.getStatus() == null) {
            device.setStatus(0);
        }
        deviceService.save(device);
        return Result.success();
    }

    @PutMapping
    @Log(title = "IoT设备", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('iot:device:edit')")
    public Result<Void> update(@RequestBody IotDevice device) {
        deviceService.updateById(device);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Log(title = "IoT设备", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('iot:device:remove')")
    public Result<Void> remove(@PathVariable Long id) {
        deviceService.removeById(id);
        return Result.success();
    }
}
