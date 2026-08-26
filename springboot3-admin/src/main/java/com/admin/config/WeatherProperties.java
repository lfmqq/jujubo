package com.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 和风天气接口配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "weather.qweather")
public class WeatherProperties {

    /** 是否启用天气服务 */
    private boolean enabled;
    /** 是否启用访问者定位；关闭后始终使用默认城市坐标 */
    private boolean dynamicLocation = true;
    /** 控制台分配的专属 API Host */
    private String apiHost;
    /** 服务端使用的 API KEY */
    private String apiKey;
    /** 首页展示的地点名称 */
    private String locationName = "北京";
    /** 地点纬度 */
    private BigDecimal latitude = new BigDecimal("39.92");
    /** 地点经度 */
    private BigDecimal longitude = new BigDecimal("116.41");
    /** 预报天数，和风天气支持 1 至 10 天 */
    private int forecastDays = 5;
    /** 成功响应的缓存时间，单位为秒 */
    private long cacheSeconds = 1800L;

    /**
     * 判断天气服务是否具备调用条件。
     *
     * @return 已启用且 API Host、API KEY 均已填写时返回 true
     */
    public boolean isConfigured() {
        return enabled && StringUtils.hasText(apiHost) && StringUtils.hasText(apiKey);
    }
}
