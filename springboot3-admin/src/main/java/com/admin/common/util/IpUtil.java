package com.admin.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP 地址工具：兼容 nginx 等反向代理，获取客户端真实 IP，并解析 IP 归属地。
 *
 * 归属地解析策略（自动降级，无需人工切换）：
 *   1) ip2region 离线库（快速、无网络依赖）—— 把 ip2region.xdb 放到 jar 同目录即可启用
 *   2) ip-api.com 在线 API（免费，无需密钥）  —— xdb 缺失时自动使用，有缓存每小时同 IP 只查一次
 *   3) 以上都不可用时返回「未知」
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
    private static final String UNKNOWN_LOCATION = "未知";
    private static final String DB_FILE_NAME = "ip2region.xdb";

    // ---------- ip2region 离线库（可选，有 xdb 文件时自动启用） ----------

    /** ip2region 离线搜索器（全量载入内存，线程安全） */
    private static volatile Searcher searcher;

    static {
        initSearcher();
    }

    private static void initSearcher() {
        byte[] dbBytes = loadDbBytes();
        if (dbBytes == null) {
            return;
        }
        try {
            searcher = Searcher.newWithBuffer(dbBytes);
        } catch (Exception e) {
            searcher = null;
            org.slf4j.LoggerFactory.getLogger(IpUtil.class)
                    .warn("ip2region 初始化失败，将降级为在线 API 查询", e);
        }
    }

    /**
     * 按优先级加载 xdb 字节：外部文件优先，classpath 兜底。
     */
    private static byte[] loadDbBytes() {
        // 1) 外部文件：jar 同目录 / config 子目录（生产环境推荐，更新库无需重新打包）
        String[] externalCandidates = {
                DB_FILE_NAME,
                "config/" + DB_FILE_NAME,
                System.getProperty("user.dir") + File.separator + DB_FILE_NAME
        };
        for (String candidate : externalCandidates) {
            try {
                Path p = Path.of(candidate);
                if (Files.exists(p) && Files.size(p) > 1024 * 1024) { // 小于 1MB 视为无效
                    return Files.readAllBytes(p);
                }
            } catch (Exception ignored) {
                // 继续尝试下一个候选
            }
        }
        // 2) classpath 兜底（同样要求文件大小合理，避免陈旧/损坏文件被误加载）
        try {
            ClassPathResource resource = new ClassPathResource(DB_FILE_NAME);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    if (bytes.length > 1024 * 1024) {
                        return bytes;
                    }
                }
            }
        } catch (Exception ignored) {
            // 忽略
        }
        return null;
    }

    // ---------- 在线 API 降级（ip-api.com，免费、无需密钥） ----------

    /**
     * IP 归属地缓存：key=IP, value=CacheEntry。
     * ip-api.com 免费版限制 45次/分钟，缓存可有效避免超限。
     */
    private static final Map<String, CacheEntry> IP_CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = Duration.ofHours(1).toMillis();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static class CacheEntry {
        final String location;
        final String city;
        final Double latitude;
        final Double longitude;
        final long expireAt;

        /**
         * 创建 IP 查询缓存项。
         *
         * @param location 完整归属地文本
         * @param city 城市名称
         * @param latitude 纬度
         * @param longitude 经度
         * @param expireAt 过期时间戳
         */
        CacheEntry(String location, String city, Double latitude, Double longitude, long expireAt) {
            this.location = location;
            this.city = city;
            this.latitude = latitude;
            this.longitude = longitude;
            this.expireAt = expireAt;
        }
    }

    /**
     * IP 归属地及天气定位所需的坐标。
     *
     * @param location 完整归属地文本
     * @param city 城市名称
     * @param latitude 纬度
     * @param longitude 经度
     */
    public record IpLocation(String location, String city, Double latitude, Double longitude) {
    }

    /**
     * 通过 ip-api.com 在线查询 IP 归属地。
     * 返回格式示例：{"country":"China","regionName":"Guangdong","city":"Shenzhen"}
     *
     * @param ip 客户端公网 IP
     * @return IP 归属地缓存项，查询失败时返回无坐标项
     */
    private static CacheEntry resolveByOnlineApi(String ip) {
        // 查缓存
        CacheEntry cached = IP_CACHE.get(ip);
        if (cached != null && System.currentTimeMillis() < cached.expireAt) {
            return cached;
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(
                    "http://ip-api.com/json/" + ip
                            + "?lang=zh-CN&fields=status,message,country,regionName,city,lat,lon"
            ).toURL().openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("Accept", "application/json");
            conn.connect();

            if (conn.getResponseCode() != 200) {
                return new CacheEntry(UNKNOWN_LOCATION, "", null, null, 0L);
            }
            JsonNode root = MAPPER.readTree(conn.getInputStream());
            if (!"success".equalsIgnoreCase(root.path("status").asText())) {
                return new CacheEntry(UNKNOWN_LOCATION, "", null, null, 0L);
            }
            String country = root.path("country").asText("");
            String region = root.path("regionName").asText("");
            String city = root.path("city").asText("");

            StringBuilder sb = new StringBuilder();
            if (!country.isEmpty()) sb.append(country);
            if (!region.isEmpty()) sb.append(region);
            if (!city.isEmpty()) sb.append(city);
            String location = sb.length() > 0 ? sb.toString() : UNKNOWN_LOCATION;
            Double latitude = root.path("lat").isNumber() ? root.path("lat").asDouble() : null;
            Double longitude = root.path("lon").isNumber() ? root.path("lon").asDouble() : null;

            // 写入缓存
            CacheEntry entry = new CacheEntry(
                    location,
                    city,
                    latitude,
                    longitude,
                    System.currentTimeMillis() + CACHE_TTL_MS
            );
            IP_CACHE.put(ip, entry);
            return entry;
        } catch (Exception e) {
            return new CacheEntry(UNKNOWN_LOCATION, "", null, null, 0L);
        }
    }

    // ---------- 公共方法 ----------

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
     * 根据 IP 获取归属地（自动降级）。
     * 优先级：离线库 → 在线 API → 「未知」
     */
    public static String getRealAddressByIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return UNKNOWN_LOCATION;
        }
        if (isInnerIp(ip)) {
            return "内网IP";
        }
        // 1) 优先 ip2region 离线库
        if (searcher != null) {
            try {
                String region = searcher.search(ip);
                return resolveXdbResult(region);
            } catch (Exception e) {
                // 离线库查询失败，继续走在线降级
            }
        }
        // 2) 在线 API 降级
        return resolveByOnlineApi(ip).location;
    }

    /**
     * 根据公网 IP 查询城市及经纬度，供天气服务进行城市级定位。
     *
     * @param ip 客户端公网 IP
     * @return 查询到完整坐标时返回归属地，否则返回空
     */
    public static Optional<IpLocation> resolveCoordinatesByIp(String ip) {
        if (!StringUtils.hasText(ip) || isInnerIp(ip)) {
            return Optional.empty();
        }
        CacheEntry entry = resolveByOnlineApi(ip);
        if (entry.latitude == null || entry.longitude == null) {
            return Optional.empty();
        }
        return Optional.of(new IpLocation(entry.location, entry.city, entry.latitude, entry.longitude));
    }

    /**
     * 将 ip2region 原始结果（国家|区域|省份|城市|运营商，未知段为 0）转为「省份+城市」。
     */
    private static String resolveXdbResult(String region) {
        if (!StringUtils.hasText(region) || "0".equals(region)) {
            return UNKNOWN_LOCATION;
        }
        String[] parts = region.split("\\|");
        StringBuilder sb = new StringBuilder();
        if (parts.length >= 4) {
            String province = "0".equals(parts[2]) ? "" : parts[2];
            String city = "0".equals(parts[3]) ? "" : parts[3];
            sb.append(province).append(city);
        } else {
            sb.append(region);
        }
        String result = sb.toString().trim();
        return StringUtils.hasText(result) ? result : UNKNOWN_LOCATION;
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
