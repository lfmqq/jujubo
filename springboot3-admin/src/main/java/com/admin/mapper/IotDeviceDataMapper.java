package com.admin.mapper;

import com.admin.entity.IotDeviceData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 设备数据 Mapper
 */
public interface IotDeviceDataMapper extends BaseMapper<IotDeviceData> {

    /** 查询设备最近数据 */
    @Select("SELECT d.property_name, d.property_value, d.report_time " +
            "FROM iot_device_data d " +
            "INNER JOIN ( " +
            "  SELECT property_name, MAX(report_time) AS max_time " +
            "  FROM iot_device_data WHERE device_id = #{deviceId} " +
            "  GROUP BY property_name " +
            ") latest ON d.property_name = latest.property_name AND d.report_time = latest.max_time " +
            "WHERE d.device_id = #{deviceId} " +
            "ORDER BY d.property_name")
    List<Map<String, Object>> selectLatestByDevice(@Param("deviceId") Long deviceId);
}
