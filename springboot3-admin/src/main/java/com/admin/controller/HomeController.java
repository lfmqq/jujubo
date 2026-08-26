package com.admin.controller;

import com.admin.common.result.Result;
import com.admin.common.util.IpUtil;
import com.admin.mapper.SysDeptMapper;
import com.admin.mapper.SysMenuMapper;
import com.admin.mapper.SysRoleMapper;
import com.admin.mapper.SysUserMapper;
import com.admin.service.WeatherService;
import com.admin.vo.HomeWeatherVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysDeptMapper deptMapper;
    private final WeatherService weatherService;

    /**
     * 统计首页顶部卡片所需的用户、角色、菜单和部门数量。
     *
     * @return 首页统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        // 分别查询各业务表总数，统一聚合为首页统计响应。
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("roleCount", roleMapper.selectCount(null));
        data.put("menuCount", menuMapper.selectCount(null));
        data.put("deptCount", deptMapper.selectCount(null));
        return Result.success(data);
    }

    /**
     * 获取首页天气组件所需的当前天气和每日预报。
     *
     * @param refresh 是否跳过缓存并强制刷新
     * @param latitude 浏览器定位纬度，可为空
     * @param longitude 浏览器定位经度，可为空
     * @param request 当前 HTTP 请求，用于提取真实客户端 IP
     * @return 首页天气数据
     */
    @GetMapping("/weather")
    public Result<HomeWeatherVO> weather(
            @RequestParam(defaultValue = "false") boolean refresh,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude,
            HttpServletRequest request
    ) {
        // 浏览器定位不可用时，将代理转发的真实 IP 交给后端作为兜底定位依据。
        String clientIp = IpUtil.getIpAddr(request);
        return Result.success(weatherService.getHomeWeather(refresh, latitude, longitude, clientIp));
    }
}
