package com.admin.service;

import com.admin.vo.HomeWeatherVO;

import java.math.BigDecimal;

/**
 * 天气服务
 */
public interface WeatherService {

    /**
     * 获取首页展示所需的当前天气和每日预报。
     *
     * @param forceRefresh 是否跳过已有缓存并重新请求天气服务
     * @return 首页天气展示数据
     */
    HomeWeatherVO getHomeWeather(boolean forceRefresh);

    /**
     * 根据浏览器坐标或客户端 IP 获取首页天气，定位参数缺失时回退到默认城市。
     *
     * @param forceRefresh 是否跳过已有缓存并重新请求天气服务
     * @param latitude 浏览器定位纬度，可为空
     * @param longitude 浏览器定位经度，可为空
     * @param clientIp 经过代理转发后的客户端 IP，可为空
     * @return 首页天气展示数据
     */
    HomeWeatherVO getHomeWeather(boolean forceRefresh, BigDecimal latitude, BigDecimal longitude, String clientIp);
}
