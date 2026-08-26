package com.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页天气展示数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeWeatherVO {

    /** 天气服务是否已配置 */
    private boolean configured;
    /** 当前是否有可展示的天气数据 */
    private boolean available;
    /** 无数据时的状态说明 */
    private String message;
    /** 地点名称 */
    private String locationName;
    /** 定位来源：浏览器定位、IP 归属地或默认城市 */
    private String locationSource;
    /** 数据更新时间 */
    private String updatedAt;
    /** 当前天气 */
    private CurrentWeather current;
    /** 每日天气预报 */
    @Builder.Default
    private List<DailyForecast> forecast = new ArrayList<>();
    /** 数据来源名称 */
    private String attribution;
    /** 数据来源链接 */
    private String attributionUrl;

    /**
     * 当前天气数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentWeather {
        /** 天气现象 */
        private String condition;
        /** 和风天气图标代码 */
        private String iconCode;
        /** 当前温度，单位为摄氏度 */
        private Integer temperature;
        /** 体感温度，单位为摄氏度 */
        private Integer feelsLike;
        /** 相对湿度百分比 */
        private Integer humidity;
        /** 中文风向 */
        private String windDirection;
        /** 蒲福风级 */
        private Integer windScale;
        /** 风速，单位为米每秒 */
        private Double windSpeed;
        /** 海平面气压，单位为百帕 */
        private Integer pressure;
        /** 能见度，单位为千米 */
        private Double visibility;
    }

    /**
     * 单日天气预报数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyForecast {
        /** 预报日期，格式为 yyyy-MM-dd */
        private String date;
        /** 白天天气现象 */
        private String condition;
        /** 和风天气图标代码 */
        private String iconCode;
        /** 最低温度，单位为摄氏度 */
        private Integer minTemperature;
        /** 最高温度，单位为摄氏度 */
        private Integer maxTemperature;
        /** 降水概率百分比 */
        private Integer precipitationProbability;
    }
}
