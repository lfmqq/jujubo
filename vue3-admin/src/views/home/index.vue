<template>
  <div class="home">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/system/user')">
          <div class="stat-card-inner">
            <div class="stat-icon" style="background: #e8f4fd;">
              <el-icon :size="28" color="var(--color-primary)"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount || 0 }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/system/role')">
          <div class="stat-card-inner">
            <div class="stat-icon" style="background: #e8f8e8;">
              <el-icon :size="28" color="#67C23A"><Avatar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.roleCount || 0 }}</div>
              <div class="stat-label">角色数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/system/menu')">
          <div class="stat-card-inner">
            <div class="stat-icon" style="background: #fdf6ec;">
              <el-icon :size="28" color="#E6A23C"><Menu /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.menuCount || 0 }}</div>
              <div class="stat-label">菜单数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/system/dept')">
          <div class="stat-card-inner">
            <div class="stat-icon" style="background: #fde8e8;">
              <el-icon :size="28" color="#F56C6C"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.deptCount || 0 }}</div>
              <div class="stat-label">部门数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 内容区域 -->
    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :md="16">
        <el-card v-loading="weatherLoading" class="content-card weather-card">
          <template #header>
            <div class="weather-header">
              <div class="weather-title">
                <el-icon><PartlyCloudy /></el-icon>
                <span>天气预报</span>
              </div>
              <el-tooltip content="刷新天气" placement="top">
                <el-button
                  text
                  circle
                  :icon="Refresh"
                  :loading="weatherLoading"
                  aria-label="刷新天气"
                  @click="loadWeather(true)"
                />
              </el-tooltip>
            </div>
          </template>

          <div v-if="weather.available && weather.current" class="weather-content">
            <div class="weather-overview">
              <div class="current-weather">
                <div class="current-icon" aria-hidden="true">
                  <el-icon :size="66"><component :is="getWeatherIcon(weather.current.iconCode)" /></el-icon>
                </div>
                <div class="current-summary">
                  <div class="location-name">
                    <el-icon><Location /></el-icon>
                    <span>{{ weather.locationName }}</span>
                  </div>
                  <div class="temperature-line">
                    <strong>{{ weather.current.temperature }}</strong><span>°C</span>
                  </div>
                  <div class="condition-text">{{ weather.current.condition }}</div>
                </div>
              </div>

              <div class="weather-metrics">
                <div class="weather-metric">
                  <el-icon class="metric-icon feels"><Odometer /></el-icon>
                  <div>
                    <span>体感温度</span>
                    <strong>{{ weather.current.feelsLike }}°C</strong>
                  </div>
                </div>
                <div class="weather-metric">
                  <el-icon class="metric-icon humidity"><Pouring /></el-icon>
                  <div>
                    <span>相对湿度</span>
                    <strong>{{ weather.current.humidity }}%</strong>
                  </div>
                </div>
                <div class="weather-metric">
                  <el-icon class="metric-icon wind"><WindPower /></el-icon>
                  <div>
                    <span>{{ weather.current.windDirection }}</span>
                    <strong>{{ weather.current.windScale }} 级 · {{ weather.current.windSpeed }} m/s</strong>
                  </div>
                </div>
                <div class="weather-metric">
                  <el-icon class="metric-icon visibility"><View /></el-icon>
                  <div>
                    <span>能见度</span>
                    <strong>{{ weather.current.visibility }} km</strong>
                  </div>
                </div>
              </div>
            </div>

            <div class="forecast-list">
              <div v-for="(day, index) in weather.forecast" :key="day.date" class="forecast-day">
                <span class="forecast-weekday">{{ formatForecastDate(day.date, index) }}</span>
                <el-icon :size="30" class="forecast-icon">
                  <component :is="getWeatherIcon(day.iconCode)" />
                </el-icon>
                <span class="forecast-condition">{{ day.condition }}</span>
                <strong class="forecast-temperature">
                  {{ day.maxTemperature }}° <span>/ {{ day.minTemperature }}°</span>
                </strong>
                <span class="rain-probability">
                  <el-icon><Pouring /></el-icon>{{ day.precipitationProbability }}%
                </span>
              </div>
            </div>

            <div class="weather-footer">
              <span>
                <template v-if="weather.locationSource">定位：{{ weather.locationSource }} · </template>
                更新于 {{ weather.updatedAt }}
              </span>
              <a :href="weather.attributionUrl" target="_blank" rel="noopener noreferrer">
                数据来源：{{ weather.attribution }}
              </a>
            </div>
          </div>

          <el-empty v-else :image-size="72" :description="weather.message || '天气数据暂时不可用'" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8" class="quick-entry-col">
        <el-card class="content-card">
          <template #header>
            <span>快速入口</span>
          </template>
          <div class="quick-links">
            <el-button type="primary" plain class="quick-btn" @click="$router.push('/system/user')">
              <el-icon><User /></el-icon> 用户管理
            </el-button>
            <el-button type="success" plain class="quick-btn" @click="$router.push('/system/role')">
              <el-icon><Avatar /></el-icon> 角色管理
            </el-button>
            <el-button type="warning" plain class="quick-btn" @click="$router.push('/system/menu')">
              <el-icon><Menu /></el-icon> 菜单管理
            </el-button>
            <el-button type="danger" plain class="quick-btn" @click="$router.push('/system/dept')">
              <el-icon><OfficeBuilding /></el-icon> 部门管理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '@/utils/request'
import {
  Avatar,
  Cloudy,
  Drizzling,
  Lightning,
  Location,
  Menu,
  MoonNight,
  MostlyCloudy,
  Odometer,
  OfficeBuilding,
  PartlyCloudy,
  Pouring,
  Refresh,
  Sunny,
  User,
  View,
  WindPower
} from '@element-plus/icons-vue'

const stats = ref({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  deptCount: 0
})
const weatherLoading = ref(false)
const weather = ref({
  configured: false,
  available: false,
  message: '',
  forecast: []
})
const browserLocation = ref(null)

// 加载首页顶部统计数据。
const loadStats = async () => {
  try {
    const res = await request.get('/home/stats')
    if (res.data) {
      stats.value = res.data
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

// 请求浏览器定位；页面不支持定位或用户拒绝授权时返回 null，由后端按 IP 兜底。
const getBrowserLocation = () => new Promise((resolve) => {
  if (!navigator.geolocation) {
    resolve(null)
    return
  }

  navigator.geolocation.getCurrentPosition(
    ({ coords }) => {
      if (Number.isFinite(coords.latitude) && Number.isFinite(coords.longitude)) {
        resolve({ latitude: coords.latitude, longitude: coords.longitude })
      } else {
        resolve(null)
      }
    },
    () => resolve(null),
    {
      enableHighAccuracy: false,
      maximumAge: 15 * 60 * 1000,
      timeout: 5000
    }
  )
})

// 首次加载时获取一次浏览器坐标，后续刷新复用，避免反复弹出定位授权。
const resolveBrowserLocation = async () => {
  if (browserLocation.value) {
    return browserLocation.value
  }
  browserLocation.value = await getBrowserLocation()
  return browserLocation.value
}

// 加载天气数据；forceRefresh 为 true 时通知后端跳过 Redis 缓存。
const loadWeather = async (forceRefresh = false) => {
  weatherLoading.value = true
  try {
    const location = await resolveBrowserLocation()
    const params = { refresh: forceRefresh }
    if (location) {
      params.latitude = location.latitude
      params.longitude = location.longitude
    }
    const res = await request.get('/home/weather', {
      params
    })
    if (res.data) {
      weather.value = res.data
    }
  } catch (e) {
    weather.value = {
      configured: true,
      available: false,
      message: '天气数据暂时不可用',
      forecast: []
    }
    console.error('加载天气失败', e)
  } finally {
    weatherLoading.value = false
  }
}

// 根据和风天气现象代码返回对应的 Element Plus 图标组件。
const getWeatherIcon = (iconCode) => {
  const code = String(iconCode || '')
  if (['302', '303', '304'].includes(code)) return Lightning
  if (code.startsWith('3')) return Pouring
  if (code.startsWith('4')) return Drizzling
  if (code.startsWith('5')) return MostlyCloudy
  if (code.startsWith('2')) return WindPower
  if (code.startsWith('15')) return MoonNight
  if (code === '100') return Sunny
  if (['101', '102', '103'].includes(code)) return PartlyCloudy
  return Cloudy
}

// 将预报日期格式化为“今天”“明天”或星期文本。
const formatForecastDate = (date, index) => {
  if (index === 0) return '今天'
  if (index === 1) return '明天'
  if (!date) return '--'
  return new Intl.DateTimeFormat('zh-CN', { weekday: 'short' }).format(new Date(`${date}T00:00:00`))
}

// 页面挂载时并行加载统计和天气数据。
const loadHomeData = () => {
  loadStats()
  loadWeather()
}

onMounted(loadHomeData)
</script>

<style scoped>
.stats-row {
  margin-bottom: 8px;
}

.stats-row .el-col {
  margin-bottom: 8px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 6px;
}

.content-row {
  margin-top: 8px;
  align-items: stretch;
}

.content-card {
  height: 100%;
}

.weather-card {
  min-height: 340px;
}

.weather-card :deep(.el-card__body) {
  min-height: 276px;
}

.weather-header,
.weather-title {
  display: flex;
  align-items: center;
}

.weather-header {
  justify-content: space-between;
  min-height: 32px;
}

.weather-title {
  gap: 8px;
  color: var(--text-primary);
  font-weight: 600;
}

.weather-title .el-icon {
  color: #f59e0b;
  font-size: 20px;
}

.weather-content {
  min-width: 0;
}

.weather-overview {
  display: grid;
  grid-template-columns: minmax(240px, 0.8fr) minmax(360px, 1.2fr);
  gap: 28px;
  align-items: center;
  min-height: 126px;
}

.current-weather {
  display: flex;
  align-items: center;
  min-width: 0;
}

.current-icon {
  width: 92px;
  height: 92px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 92px;
  border-radius: 8px;
  color: #f59e0b;
  background: #fff8e8;
}

.current-summary {
  min-width: 0;
  margin-left: 18px;
}

.location-name {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--text-regular);
  font-size: 14px;
}

.location-name span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.temperature-line {
  display: flex;
  align-items: flex-start;
  margin-top: 4px;
  color: var(--text-primary);
  line-height: 1;
}

.temperature-line strong {
  font-size: 48px;
  font-weight: 650;
}

.temperature-line span {
  margin-top: 6px;
  font-size: 18px;
}

.condition-text {
  margin-top: 8px;
  color: var(--text-regular);
  font-size: 15px;
}

.weather-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 18px;
}

.weather-metric {
  min-width: 0;
  min-height: 52px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-left: 2px solid var(--border-color);
}

.weather-metric .metric-icon {
  flex: 0 0 auto;
  font-size: 23px;
}

.metric-icon.feels {
  color: #ef4444;
}

.metric-icon.humidity,
.metric-icon.visibility {
  color: #0ea5e9;
}

.metric-icon.wind {
  color: #22c55e;
}

.weather-metric div {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.weather-metric span {
  color: var(--text-secondary);
  font-size: 12px;
}

.weather-metric strong {
  overflow: hidden;
  color: var(--text-regular);
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.forecast-list {
  display: grid;
  grid-template-columns: repeat(5, minmax(92px, 1fr));
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

.forecast-day {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  padding: 0 8px;
  border-right: 1px solid var(--border-light);
  text-align: center;
}

.forecast-day:last-child {
  border-right: 0;
}

.forecast-weekday,
.forecast-condition,
.rain-probability {
  font-size: 12px;
}

.forecast-weekday {
  color: var(--text-regular);
  font-weight: 600;
}

.forecast-icon {
  color: #f59e0b;
}

.forecast-condition {
  width: 100%;
  overflow: hidden;
  color: var(--text-secondary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.forecast-temperature {
  color: var(--text-primary);
  font-size: 14px;
}

.forecast-temperature span {
  color: var(--text-secondary);
  font-weight: 500;
}

.rain-probability {
  display: flex;
  align-items: center;
  gap: 3px;
  min-height: 18px;
  color: #0ea5e9;
}

.weather-footer {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-top: 14px;
  color: var(--text-secondary);
  font-size: 11px;
}

.weather-footer a {
  color: var(--text-secondary);
  text-decoration: none;
}

.weather-footer a:hover {
  color: var(--color-primary);
}

.quick-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-btn {
  width: 100%;
  justify-content: flex-start;
}

.quick-btn .el-icon {
  margin-right: 8px;
}

html.dark .current-icon {
  background: #332b1c;
}

@media (max-width: 1199px) {
  .weather-overview {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .weather-metrics {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 991px) {
  .quick-entry-col {
    margin-top: 16px;
  }
}

@media (max-width: 767px) {
  .weather-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .forecast-list {
    grid-template-columns: repeat(5, minmax(84px, 1fr));
    overflow-x: auto;
    padding-bottom: 8px;
  }

  .weather-footer {
    flex-direction: column;
    gap: 4px;
  }
}

@media (max-width: 479px) {
  .current-icon {
    width: 78px;
    height: 78px;
    flex-basis: 78px;
  }

  .temperature-line strong {
    font-size: 40px;
  }

  .weather-metric {
    padding: 6px;
  }
}
</style>
