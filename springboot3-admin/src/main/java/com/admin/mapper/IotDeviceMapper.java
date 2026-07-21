package com.admin.mapper;

import com.admin.entity.IotDevice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 设备 Mapper
 */
public interface IotDeviceMapper extends BaseMapper<IotDevice> {

    /** 按状态统计设备数量 */
    @Select("SELECT status, COUNT(*) AS count FROM iot_device GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /** 按产品统计设备数量 */
    @Select("SELECT p.product_name AS name, COUNT(d.id) AS value " +
            "FROM iot_product p LEFT JOIN iot_device d ON p.id = d.product_id " +
            "GROUP BY p.id, p.product_name")
    List<Map<String, Object>> countByProduct();

    /** 按天统计新增设备 */
    @Select("SELECT DATE(create_time) AS day, COUNT(*) AS count " +
            "FROM iot_device " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(create_time) ORDER BY day")
    List<Map<String, Object>> countByDay();
}
