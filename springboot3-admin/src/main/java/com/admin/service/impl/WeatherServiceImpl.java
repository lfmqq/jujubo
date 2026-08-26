package com.admin.service.impl;

import com.admin.common.util.RedisUtil;
import com.admin.common.util.IpUtil;
import com.admin.config.WeatherProperties;
import com.admin.service.WeatherService;
import com.admin.vo.HomeWeatherVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.util.StringUtils;

/**
 * 和风天气服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private static final String CACHE_KEY_PREFIX = "home:weather:";
    private static final int LOCATION_SCALE = 3;
    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");
    private static final DateTimeFormatter UPDATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Map<String, String> WIND_DIRECTIONS = Map.ofEntries(
            Map.entry("n", "北风"), Map.entry("nne", "北东北风"), Map.entry("ne", "东北风"),
            Map.entry("ene", "东东北风"), Map.entry("e", "东风"), Map.entry("ese", "东东南风"),
            Map.entry("se", "东南风"), Map.entry("sse", "南东南风"), Map.entry("s", "南风"),
            Map.entry("ssw", "南西南风"), Map.entry("sw", "西南风"), Map.entry("wsw", "西西南风"),
            Map.entry("w", "西风"), Map.entry("wnw", "西西北风"), Map.entry("nw", "西北风"),
            Map.entry("nnw", "北西北风"), Map.entry("none", "无持续风向"), Map.entry("vrb", "风向不定")
    );

    private final WeatherProperties properties;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    /**
     * 天气查询使用的定位信息，坐标统一保留三位小数以控制缓存数量。
     *
     * @param locationName 展示城市名称，可为空
     * @param latitude 纬度
     * @param longitude 经度
     * @param source 定位来源
     */
    private record ResolvedLocation(String locationName, BigDecimal latitude, BigDecimal longitude, String source) {
    }

    /**
     * 获取首页天气，优先读取 Redis 缓存，手动刷新时重新请求和风天气。
     *
     * @param forceRefresh 是否强制刷新天气数据
     * @return 可直接供首页展示的天气数据
     */
    @Override
    public HomeWeatherVO getHomeWeather(boolean forceRefresh) {
        return getHomeWeather(forceRefresh, null, null, null);
    }

    /**
     * 获取首页天气，按浏览器坐标、客户端 IP、默认城市的顺序确定查询位置。
     *
     * @param forceRefresh 是否强制刷新天气数据
     * @param latitude 浏览器定位纬度，可为空
     * @param longitude 浏览器定位经度，可为空
     * @param clientIp 客户端公网 IP，可为空
     * @return 可直接供首页展示的天气数据
     */
    @Override
    public HomeWeatherVO getHomeWeather(
            boolean forceRefresh,
            BigDecimal latitude,
            BigDecimal longitude,
            String clientIp
    ) {
        if (!properties.isConfigured()) {
            return createUnavailableWeather(false, "天气服务尚未配置");
        }

        ResolvedLocation location = resolveLocation(latitude, longitude, clientIp);
        String cacheKey = buildCacheKey(location);
        // 先读取缓存，普通访问直接复用；强制刷新失败时也可回退到最近一次数据。
        HomeWeatherVO cachedWeather = readCache(cacheKey);
        if (!forceRefresh && cachedWeather != null) {
            return cachedWeather;
        }

        try {
            // 实时天气和每日预报分别由和风天气接口提供，在服务端聚合后再返回前端。
            HomeWeatherVO weather = fetchWeather(location);
            writeCache(cacheKey, weather);
            return weather;
        } catch (InterruptedException exception) {
            // 恢复线程中断标记，避免上层任务无法感知取消信号。
            Thread.currentThread().interrupt();
            log.warn("获取和风天气时线程被中断，location={}", properties.getLocationName(), exception);
            return cachedWeather != null
                    ? cachedWeather
                    : createUnavailableWeather(true, "天气数据暂时不可用");
        } catch (Exception exception) {
            log.error("获取和风天气失败，location={}", properties.getLocationName(), exception);
            return cachedWeather != null
                    ? cachedWeather
                    : createUnavailableWeather(true, "天气数据暂时不可用");
        }
    }

    /**
     * 按优先级解析天气查询位置：浏览器坐标、IP 归属地、默认城市。
     *
     * @param latitude 浏览器定位纬度
     * @param longitude 浏览器定位经度
     * @param clientIp 客户端公网 IP
     * @return 规范化后的天气查询位置
     */
    private ResolvedLocation resolveLocation(BigDecimal latitude, BigDecimal longitude, String clientIp) {
        if (properties.isDynamicLocation() && isValidCoordinates(latitude, longitude)) {
            return new ResolvedLocation(
                    null,
                    normalizeCoordinate(latitude),
                    normalizeCoordinate(longitude),
                    "浏览器定位"
            );
        }

        // 浏览器未授权或当前页面不在安全上下文时，使用客户端公网 IP 推断城市。
        if (properties.isDynamicLocation() && StringUtils.hasText(clientIp)) {
            Optional<IpUtil.IpLocation> ipLocation = IpUtil.resolveCoordinatesByIp(clientIp);
            if (ipLocation.isPresent()
                    && ipLocation.get().latitude() != null
                    && ipLocation.get().longitude() != null) {
                String city = StringUtils.hasText(ipLocation.get().city())
                        ? ipLocation.get().city()
                        : properties.getLocationName();
                return new ResolvedLocation(
                        city,
                        normalizeCoordinate(BigDecimal.valueOf(ipLocation.get().latitude())),
                        normalizeCoordinate(BigDecimal.valueOf(ipLocation.get().longitude())),
                        "IP归属地"
                );
            }
        }

        return new ResolvedLocation(
                properties.getLocationName(),
                normalizeCoordinate(properties.getLatitude()),
                normalizeCoordinate(properties.getLongitude()),
                "默认城市"
        );
    }

    /**
     * 校验浏览器传入的经纬度是否在合法范围内。
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @return 坐标完整且范围合法时返回 true
     */
    private boolean isValidCoordinates(BigDecimal latitude, BigDecimal longitude) {
        return latitude != null
                && longitude != null
                && latitude.compareTo(MIN_LATITUDE) >= 0
                && latitude.compareTo(MAX_LATITUDE) <= 0
                && longitude.compareTo(MIN_LONGITUDE) >= 0
                && longitude.compareTo(MAX_LONGITUDE) <= 0;
    }

    /**
     * 将坐标四舍五入到三位小数，降低 GPS 浮点波动造成的缓存碎片。
     *
     * @param coordinate 原始坐标
     * @return 规范化后的坐标
     */
    private BigDecimal normalizeCoordinate(BigDecimal coordinate) {
        BigDecimal fallback = coordinate == null ? BigDecimal.ZERO : coordinate;
        return fallback.setScale(LOCATION_SCALE, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    /**
     * 调用和风天气实时及每日预报接口并聚合结果。
     *
     * @param location 已解析的天气查询位置
     * @return 聚合后的首页天气数据
     * @throws IOException 接口响应异常或数据结构不完整时抛出
     * @throws InterruptedException 请求线程被中断时抛出
     */
    private HomeWeatherVO fetchWeather(ResolvedLocation location) throws IOException, InterruptedException {
        String coordinatePath = formatCoordinate(location.latitude()) + "/"
                + formatCoordinate(location.longitude());
        JsonNode currentRoot = requestJson("/weather/v1/current/" + coordinatePath
                + "?localTime=true&lang=zh");
        int forecastDays = Math.max(1, Math.min(properties.getForecastDays(), 10));
        JsonNode dailyRoot = requestJson("/weather/v1/daily/" + coordinatePath
                + "?days=" + forecastDays + "&localTime=true&lang=zh");

        if (!currentRoot.path("condition").isObject() || !dailyRoot.path("days").isArray()) {
            throw new IOException("和风天气响应数据不完整");
        }

        String locationName = StringUtils.hasText(location.locationName())
                ? location.locationName()
                : resolveCityName(location);
        return HomeWeatherVO.builder()
                .configured(true)
                .available(true)
                .locationName(locationName)
                .locationSource(location.source())
                .updatedAt(LocalDateTime.now().format(UPDATE_TIME_FORMATTER))
                .current(buildCurrentWeather(currentRoot))
                .forecast(buildDailyForecast(dailyRoot.path("days"), forecastDays))
                .attribution("和风天气")
                .attributionUrl("https://www.qweather.com/")
                .build();
    }

    /**
     * 使用和风天气地理编码接口将浏览器坐标转换为城市名称。
     *
     * @param location 浏览器定位坐标
     * @return 城市名称，解析失败时返回“当前位置”
     */
    private String resolveCityName(ResolvedLocation location) {
        try {
            // 和风天气 GeoAPI 的坐标顺序为经度、纬度。
            JsonNode root = requestJson("/geo/v2/city/lookup?location="
                    + formatCoordinate(location.longitude()) + ","
                    + formatCoordinate(location.latitude()) + "&lang=zh");
            JsonNode cityNode = root.path("location").path(0);
            String city = cityNode.path("adm2").asText("");
            if (!StringUtils.hasText(city)) {
                city = cityNode.path("name").asText("");
            }
            return StringUtils.hasText(city) ? city : "当前位置";
        } catch (Exception exception) {
            log.warn("根据浏览器坐标解析城市失败，source={}", location.source(), exception);
            return "当前位置";
        }
    }

    /**
     * 请求指定和风天气接口并解析 JSON。
     *
     * @param path 接口路径及查询参数
     * @return 接口返回的 JSON 根节点
     * @throws IOException 请求失败或返回非 2xx 状态时抛出
     * @throws InterruptedException 请求线程被中断时抛出
     */
    private JsonNode requestJson(String path) throws IOException, InterruptedException {
        String apiHost = normalizeApiHost(properties.getApiHost());
        HttpRequest request = HttpRequest.newBuilder(URI.create(apiHost + path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .header("X-QW-Api-Key", properties.getApiKey())
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("和风天气接口返回状态码 " + response.statusCode());
        }
        // 和风天气网关可能返回 gzip 压缩响应，先按响应头解压再交给 JSON 解析器。
        byte[] responseBody = response.body();
        String contentEncoding = response.headers().firstValue("Content-Encoding").orElse("");
        if (contentEncoding.toLowerCase(Locale.ROOT).contains("gzip")) {
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(responseBody));
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                gzipInputStream.transferTo(outputStream);
                responseBody = outputStream.toByteArray();
            }
        }
        return objectMapper.readTree(new String(responseBody, StandardCharsets.UTF_8));
    }

    /**
     * 将实时天气 JSON 转换为首页当前天气模型。
     *
     * @param root 实时天气接口根节点
     * @return 当前天气展示数据
     */
    private HomeWeatherVO.CurrentWeather buildCurrentWeather(JsonNode root) {
        JsonNode humidityNode = root.path("humidity");
        JsonNode visibilityNode = root.path("visibility");
        double humidity = humidityNode.asDouble(0D);
        double visibility = visibilityNode.path("value").asDouble(0D);

        // 新版接口的湿度取值为 0 至 1，统一换算为前端展示的百分比。
        int humidityPercent = toPercentage(humidity);
        // 能见度按响应单位换算为千米，避免前端重复处理单位差异。
        if ("m".equalsIgnoreCase(visibilityNode.path("unit").asText())) {
            visibility = visibility / 1000D;
        }

        JsonNode windNode = root.path("wind");
        return HomeWeatherVO.CurrentWeather.builder()
                .condition(root.path("condition").path("text").asText("未知"))
                .iconCode(root.path("condition").path("code").asText("999"))
                .temperature(roundToInteger(root.path("temperature").path("value").asDouble()))
                .feelsLike(roundToInteger(root.path("feelsLike").path("value").asDouble()))
                .humidity(humidityPercent)
                .windDirection(translateWindDirection(windNode.path("direction").path("compass").asText()))
                .windScale(windNode.path("scale").asInt(0))
                .windSpeed(roundToOneDecimal(windNode.path("speed").path("value").asDouble()))
                .pressure(roundToInteger(root.path("pressure").path("value").asDouble()))
                .visibility(roundToOneDecimal(visibility))
                .build();
    }

    /**
     * 将每日预报数组转换为首页预报列表。
     *
     * @param daysNode 每日预报数组节点
     * @param limit 最多返回的预报天数
     * @return 每日预报展示列表
     */
    private List<HomeWeatherVO.DailyForecast> buildDailyForecast(JsonNode daysNode, int limit) {
        List<HomeWeatherVO.DailyForecast> forecasts = new ArrayList<>();
        for (JsonNode dayNode : daysNode) {
            if (forecasts.size() >= limit) {
                break;
            }
            JsonNode daytimeNode = dayNode.path("daytime");
            // 降水概率在新版接口中为 0 至 1，转换成整数百分比便于直接展示。
            int precipitationProbability = toPercentage(
                    daytimeNode.path("precipitation").path("probability").asDouble(0D)
            );
            forecasts.add(HomeWeatherVO.DailyForecast.builder()
                    .date(extractDate(dayNode.path("forecastStartTime").asText()))
                    .condition(daytimeNode.path("condition").path("text").asText("未知"))
                    .iconCode(daytimeNode.path("condition").path("code").asText("999"))
                    .minTemperature(roundToInteger(dayNode.path("temperatureMin").path("value").asDouble()))
                    .maxTemperature(roundToInteger(dayNode.path("temperatureMax").path("value").asDouble()))
                    .precipitationProbability(precipitationProbability)
                    .build());
        }
        return forecasts;
    }

    /**
     * 从 ISO 日期时间中截取 yyyy-MM-dd 日期。
     *
     * @param dateTime ISO 日期时间文本
     * @return 日期文本，格式异常时原样返回
     */
    private String extractDate(String dateTime) {
        return dateTime != null && dateTime.length() >= 10 ? dateTime.substring(0, 10) : dateTime;
    }

    /**
     * 将小数比例或已有百分数统一换算为 0 至 100 的整数。
     *
     * @param value 原始比例或百分数
     * @return 取整后的百分比
     */
    private int toPercentage(double value) {
        double percent = value <= 1D ? value * 100D : value;
        return Math.max(0, Math.min(100, roundToInteger(percent)));
    }

    /**
     * 将数值四舍五入为整数。
     *
     * @param value 原始数值
     * @return 四舍五入后的整数
     */
    private int roundToInteger(double value) {
        return (int) Math.round(value);
    }

    /**
     * 将数值保留一位小数。
     *
     * @param value 原始数值
     * @return 保留一位小数后的数值
     */
    private double roundToOneDecimal(double value) {
        return Math.round(value * 10D) / 10D;
    }

    /**
     * 将和风天气方位代码转换为中文风向。
     *
     * @param compass 方位代码
     * @return 中文风向名称
     */
    private String translateWindDirection(String compass) {
        if (compass == null || compass.isBlank()) {
            return "无持续风向";
        }
        return WIND_DIRECTIONS.getOrDefault(compass.toLowerCase(Locale.ROOT), compass);
    }

    /**
     * 生成当前地点对应的 Redis 缓存键。
     *
     * @return 天气缓存键
     */
    private String buildCacheKey(ResolvedLocation location) {
        return CACHE_KEY_PREFIX + formatCoordinate(location.latitude()) + ":"
                + formatCoordinate(location.longitude()) + ":" + properties.getForecastDays();
    }

    /**
     * 从 Redis 读取天气缓存，缓存不可用时静默降级为实时请求。
     *
     * @param cacheKey 缓存键
     * @return 已缓存的天气数据，没有缓存时返回 null
     */
    private HomeWeatherVO readCache(String cacheKey) {
        try {
            Object cached = redisUtil.get(cacheKey);
            return cached instanceof HomeWeatherVO ? (HomeWeatherVO) cached : null;
        } catch (Exception exception) {
            log.warn("读取天气缓存失败，cacheKey={}", cacheKey, exception);
            return null;
        }
    }

    /**
     * 将成功获取的天气数据写入 Redis，缓存异常不影响接口返回。
     *
     * @param cacheKey 缓存键
     * @param weather 天气数据
     */
    private void writeCache(String cacheKey, HomeWeatherVO weather) {
        try {
            // 缓存时间最少为 60 秒，防止配置错误导致高频调用第三方接口。
            redisUtil.set(cacheKey, weather, Math.max(properties.getCacheSeconds(), 60L));
        } catch (Exception exception) {
            log.warn("写入天气缓存失败，cacheKey={}", cacheKey, exception);
        }
    }

    /**
     * 创建天气服务未配置或暂不可用时的响应。
     *
     * @param configured 天气服务是否已经配置
     * @param message 状态说明
     * @return 不可用状态的天气数据
     */
    private HomeWeatherVO createUnavailableWeather(boolean configured, String message) {
        return HomeWeatherVO.builder()
                .configured(configured)
                .available(false)
                .message(message)
                .locationName(properties.getLocationName())
                .locationSource("默认城市")
                .attribution("和风天气")
                .attributionUrl("https://www.qweather.com/")
                .build();
    }

    /**
     * 去除 API Host 末尾的斜杠，避免拼接接口路径时出现双斜杠。
     *
     * @param apiHost 控制台分配的 API Host
     * @return 规范化后的 API Host
     */
    private String normalizeApiHost(String apiHost) {
        String normalized = apiHost.trim();
        // 控制台通常只展示域名，复制后自动补全 HTTPS 协议，兼容直接粘贴的配置值。
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * 将经纬度格式化为不带多余零的接口路径参数。
     *
     * @param coordinate 经纬度
     * @return 十进制坐标文本
     */
    private String formatCoordinate(java.math.BigDecimal coordinate) {
        return coordinate.stripTrailingZeros().toPlainString();
    }
}
