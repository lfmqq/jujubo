package com.admin.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * IP 地址工具：兼容 nginx 等反向代理，获取客户端真实 IP。
 *
 * 线上部署时，请求通常经过 nginx -> 后端。nginx 需配置转发真实 IP，例如：
 * <pre>
 *   proxy_set_header Host $host;
 *   proxy_set_header X-Real-IP $remote_addr;
 *   proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
 *   proxy_set_header X-Forwarded-Proto $scheme;
 * </pre>
 * 本工具优先读取 X-Forwarded-For / X-Real-IP 等代理头，最后回退到 getRemoteAddr()。
 */
public class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 获取客户端真实 IP 地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return LOCALHOST_IPV4;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (!isEffective(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!isEffective(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!isEffective(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!isEffective(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (!isEffective(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!isEffective(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 格式：client, proxy1, proxy2 ... 取第一个真实 IP
        if (StringUtils.hasText(ip) && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(',')).trim();
        }
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }
        return ip;
    }

    private static boolean isEffective(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }
        return !UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 根据 IP 获取归属地（无外部依赖，仅做内网识别）。
     * 如需精确归属地，可接入 ip2region / 纯真库 / 在线 API。
     */
    public static String getRealAddressByIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "未知";
        }
        return isInnerIp(ip) ? "内网IP" : "未知";
    }

    public static boolean isInnerIp(String ip) {
        if (LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
            return true;
        }
        return ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.16.")
                || ip.startsWith("172.17.") || ip.startsWith("172.18.") || ip.startsWith("172.19.")
                || ip.startsWith("172.2") || ip.startsWith("127.") || ip.startsWith("169.254.");
    }
}
