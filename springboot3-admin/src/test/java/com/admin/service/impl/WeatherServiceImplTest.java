package com.admin.service.impl;

import com.admin.common.util.RedisUtil;
import com.admin.config.WeatherProperties;
import com.admin.vo.HomeWeatherVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;
import java.net.InetSocketAddress;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 和风天气服务单元测试
 */
class WeatherServiceImplTest {

    private HttpServer httpServer;
    private WeatherProperties properties;
    private RedisUtil redisUtil;
    private WeatherServiceImpl weatherService;
    private AtomicReference<String> apiKeyHeader;

    /**
     * 启动本地天气接口并创建待测服务。
     *
     * @throws IOException 本地测试服务启动失败时抛出
     */
    @BeforeEach
    void setUp() throws IOException {
        apiKeyHeader = new AtomicReference<>();
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/weather/v1/current/39.92/116.41", exchange -> {
            apiKeyHeader.set(exchange.getRequestHeaders().getFirst("X-QW-Api-Key"));
            writeGzipJson(exchange, """
                    {
                      "condition": {"text": "多云", "code": "101"},
                      "temperature": {"value": 31.7, "unit": "°C"},
                      "feelsLike": {"value": 33.6, "unit": "°C"},
                      "humidity": 0.69,
                      "wind": {
                        "direction": {"compass": "sw"},
                        "speed": {"value": 4.74, "unit": "m/s"},
                        "scale": 3
                      },
                      "pressure": {"value": 1001.5, "unit": "hPa"},
                      "visibility": {"value": 29020, "unit": "m"}
                    }
                    """);
        });
        httpServer.createContext("/weather/v1/daily/39.92/116.41", exchange -> writeJson(exchange, """
                {
                  "days": [
                    {
                      "forecastStartTime": "2026-08-20T00:00+08:00",
                      "temperatureMin": {"value": 24.2, "unit": "°C"},
                      "temperatureMax": {"value": 31.8, "unit": "°C"},
                      "daytime": {
                        "condition": {"text": "小雨", "code": "305"},
                        "precipitation": {"probability": 0.64}
                      }
                    },
                    {
                      "forecastStartTime": "2026-08-21T00:00+08:00",
                      "temperatureMin": {"value": 23.7, "unit": "°C"},
                      "temperatureMax": {"value": 30.2, "unit": "°C"},
                      "daytime": {
                        "condition": {"text": "多云", "code": "101"},
                        "precipitation": {"probability": 0.1}
                      }
                    }
                  ]
                }
                """));
        httpServer.createContext("/geo/v2/city/lookup", exchange -> writeJson(exchange, """
                {
                  "location": [
                    {"name": "北京", "adm2": "北京"}
                  ]
                }
                """));
        httpServer.start();

        properties = new WeatherProperties();
        properties.setEnabled(true);
        properties.setApiHost("http://127.0.0.1:" + httpServer.getAddress().getPort());
        properties.setApiKey("test-api-key");
        properties.setLocationName("北京");
        properties.setForecastDays(2);
        properties.setCacheSeconds(1800L);

        redisUtil = mock(RedisUtil.class);
        when(redisUtil.get(anyString())).thenReturn(null);
        weatherService = new WeatherServiceImpl(properties, redisUtil, new ObjectMapper());
    }

    /**
     * 停止本地天气接口，释放测试端口。
     */
    @AfterEach
    void tearDown() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    /**
     * 验证实时天气和每日预报能够正确聚合、换算并写入缓存。
     */
    @Test
    void shouldAggregateWeatherAndCacheResult() {
        HomeWeatherVO result = weatherService.getHomeWeather(false);

        assertTrue(result.isConfigured());
        assertTrue(result.isAvailable());
        assertEquals("北京", result.getLocationName());
        assertEquals(32, result.getCurrent().getTemperature());
        assertEquals(69, result.getCurrent().getHumidity());
        assertEquals("西南风", result.getCurrent().getWindDirection());
        assertEquals(4.7D, result.getCurrent().getWindSpeed());
        assertEquals(29D, result.getCurrent().getVisibility());
        assertEquals(2, result.getForecast().size());
        assertEquals(64, result.getForecast().get(0).getPrecipitationProbability());
        assertEquals("test-api-key", apiKeyHeader.get());
        verify(redisUtil).set(anyString(), eq(result), eq(1800L));
    }

    /**
     * 验证未启用天气服务时返回可识别状态且不会访问缓存或外部接口。
     */
    @Test
    void shouldReturnUnavailableWhenNotConfigured() {
        properties.setEnabled(false);

        HomeWeatherVO result = weatherService.getHomeWeather(false);

        assertFalse(result.isConfigured());
        assertFalse(result.isAvailable());
        assertEquals("天气服务尚未配置", result.getMessage());
        verifyNoInteractions(redisUtil);
    }

    /**
     * 验证浏览器坐标能够覆盖默认城市，并通过 GeoAPI 解析城市名称。
     */
    @Test
    void shouldUseBrowserCoordinatesWhenProvided() {
        HomeWeatherVO result = weatherService.getHomeWeather(
                false,
                new BigDecimal("39.9201"),
                new BigDecimal("116.4101"),
                null
        );

        assertTrue(result.isAvailable());
        assertEquals("北京", result.getLocationName());
        assertEquals("浏览器定位", result.getLocationSource());
    }

    /**
     * 验证内网 IP 不调用公网归属地服务，而是回退到默认城市。
     */
    @Test
    void shouldFallbackToDefaultForPrivateIp() {
        HomeWeatherVO result = weatherService.getHomeWeather(false, null, null, "127.0.0.1");

        assertTrue(result.isAvailable());
        assertEquals("北京", result.getLocationName());
        assertEquals("默认城市", result.getLocationSource());
    }

    /**
     * 向本地测试接口返回 UTF-8 JSON 数据。
     *
     * @param exchange 当前 HTTP 请求上下文
     * @param json 响应 JSON 文本
     * @throws IOException 响应写入失败时抛出
     */
    private void writeJson(HttpExchange exchange, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    /**
     * 向本地测试接口返回 gzip 压缩的 UTF-8 JSON，覆盖和风天气网关的压缩响应场景。
     *
     * @param exchange 当前 HTTP 请求上下文
     * @param json 响应 JSON 文本
     * @throws IOException 压缩或响应写入失败时抛出
     */
    private void writeGzipJson(HttpExchange exchange, String json) throws IOException {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
            gzip.write(json.getBytes(StandardCharsets.UTF_8));
        }
        byte[] body = compressed.toByteArray();
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
