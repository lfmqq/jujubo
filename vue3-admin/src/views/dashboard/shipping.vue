<template>
  <div class="shipping-screen" ref="screenRef">
    <!-- 顶部标题栏 -->
    <header class="screen-header">
      <div class="header-left">
        <div class="header-decoration"></div>
        <h1 class="header-title">全球航运可视化保障大屏</h1>
      </div>
      <div class="header-right">
        <div class="current-time">{{ currentTime }}</div>
        <button class="fullscreen-btn" @click="toggleFullscreen" :title="isFullscreen ? '退出全屏' : '全屏'">
          <span v-if="isFullscreen">⊗</span>
          <span v-else>⛶</span>
        </button>
      </div>
    </header>

    <!-- 主体 -->
    <main class="screen-body">
      <!-- 左侧面板 -->
      <aside class="left-panel">
        <div class="panel-box">
          <div class="panel-header">
            <span class="header-icon">🌐</span>实时航运动态
          </div>
          <div class="panel-body">
            <div class="ship-metrics">
              <div class="metric-card" v-for="m in shipMetrics" :key="m.label">
                <div class="metric-value" :style="{ color: m.color }">{{ m.value }}</div>
                <div class="metric-label">{{ m.label }}</div>
                <div class="metric-trend" :class="m.trend > 0 ? 'up' : 'down'">
                  {{ m.trend > 0 ? '↑' : '↓' }} {{ Math.abs(m.trend) }}%
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="panel-box">
          <div class="panel-header">
            <span class="header-icon">📈</span>航线运行趋势
          </div>
          <div class="panel-body">
            <v-chart :option="trendOption" style="height: 100%;" autoresize />
          </div>
        </div>

        <div class="panel-box">
          <div class="panel-header">
            <span class="header-icon">🏆</span>热门航线 TOP5
          </div>
          <div class="panel-body">
            <div class="route-table">
              <div class="route-header">
                <span>排名</span>
                <span>航线</span>
                <span>班次</span>
                <span>准点率</span>
              </div>
              <div class="route-row" v-for="(route, idx) in topRoutes" :key="route.name">
                <span class="route-rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
                <span class="route-name">{{ route.name }}</span>
                <span class="route-count">{{ route.count }}</span>
                <span class="route-rate" :style="{ color: getRateColor(route.rate) }">{{ route.rate }}%</span>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 中间 3D 地球 -->
      <section class="center-panel">
        <div class="three-container" ref="threeContainer">
          <div class="three-loading" v-if="!threeReady">3D 地球加载中...</div>
          <button class="rotation-toggle" @click="toggleRotation" :title="isAutoRotating ? '暂停自转' : '开始自转'">
            <span>{{ isAutoRotating ? '⏸' : '▶' }}</span>
          </button>
          <!-- 悬浮提示 -->
          <div class="earth-tooltip" v-if="hoveredCity" :style="tooltipStyle">
            <div class="tooltip-title">{{ hoveredCity.name }}</div>
            <div class="tooltip-row"><span>经纬度</span><span>{{ hoveredCity.lat }}°, {{ hoveredCity.lon }}°</span></div>
            <div class="tooltip-row"><span>类型</span><span>{{ hoveredCity.type }}</span></div>
            <div class="tooltip-row"><span>所属区域</span><span>{{ hoveredCity.region }}</span></div>
            <div class="tooltip-row"><span>进出港</span><span>{{ hoveredCity.inout }}</span></div>
            <div class="tooltip-row"><span>准点率</span><span>{{ hoveredCity.punctuality }}%</span></div>
            <div class="tooltip-row"><span>业务数据</span><span>{{ hoveredCity.business }}</span></div>
          </div>
          <!-- 下钻详情面板 -->
          <transition name="drill-fade">
            <div class="drill-panel" v-if="drillDownCity" @click.stop>
              <div class="drill-header">
                <span class="drill-title">{{ drillDownCity.name }} - 区域详情</span>
                <button class="drill-close" @click="backToGlobal">✕</button>
              </div>
              <div class="drill-body">
                <div class="drill-info-grid">
                  <div class="drill-info-item">
                    <span class="drill-label">所属大洲</span>
                    <span class="drill-value">{{ drillDownCity.continent }}</span>
                  </div>
                  <div class="drill-info-item">
                    <span class="drill-label">国家/地区</span>
                    <span class="drill-value">{{ drillDownCity.country }}</span>
                  </div>
                  <div class="drill-info-item">
                    <span class="drill-label">港口类型</span>
                    <span class="drill-value">{{ drillDownCity.type }}</span>
                  </div>
                  <div class="drill-info-item">
                    <span class="drill-label">年吞吐量</span>
                    <span class="drill-value">{{ drillDownCity.annualVolume }}</span>
                  </div>
                  <div class="drill-info-item">
                    <span class="drill-label">准点率</span>
                    <span class="drill-value" style="color:#00e676">{{ drillDownCity.punctuality }}%</span>
                  </div>
                  <div class="drill-info-item">
                    <span class="drill-label">在港船舶</span>
                    <span class="drill-value">{{ drillDownCity.activeShips }}</span>
                  </div>
                </div>
                <div class="drill-section-title">周边港口</div>
                <div class="drill-nearby">
                  <div class="drill-nearby-item" v-for="n in drillDownCity.nearbyPorts" :key="n">
                    <span class="nearby-dot"></span>{{ n }}
                  </div>
                </div>
                <div class="drill-section-title">主要航线</div>
                <div class="drill-routes">
                  <span class="drill-route-tag" v-for="r in drillDownCity.mainRoutes" :key="r">{{ r }}</span>
                </div>
              </div>
            </div>
          </transition>
        </div>
        <div class="center-bottom-bar">
          <div class="bottom-stat" v-for="s in bottomStats" :key="s.title">
            <div class="bottom-icon" :style="{ color: s.color }">{{ s.icon }}</div>
            <div class="bottom-info">
              <div class="bottom-title">{{ s.title }}</div>
              <div class="bottom-value" :style="{ color: s.color }">{{ s.value }}</div>
              <div class="bottom-sub">{{ s.sub }}</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 右侧面板 -->
      <aside class="right-panel">
        <div class="panel-box">
          <div class="panel-header">
            <span class="header-icon">🌍</span>航线覆盖分布
          </div>
          <div class="panel-body">
            <v-chart :option="regionOption" style="height: 100%;" autoresize />
          </div>
        </div>

        <div class="panel-box">
          <div class="panel-header">
            <span class="header-icon">📊</span>运行态势分析
          </div>
          <div class="panel-body gauge-body">
            <div class="gauge-wrap" v-for="g in gauges" :key="g.name">
              <v-chart :option="g.option" style="width: 100%; height: 100%;" autoresize />
            </div>
          </div>
        </div>

        <div class="panel-box">
          <div class="panel-header">
            <span class="header-icon">🚨</span>实时告警
          </div>
          <div class="panel-body">
            <div class="alert-list">
              <div
                class="alert-item"
                v-for="alert in alertList"
                :key="alert.id"
                :class="'alert-' + alert.level"
              >
                <div class="alert-time">{{ alert.time }}</div>
                <div class="alert-content">
                  <span class="alert-tag" :class="'tag-' + alert.level">{{ alert.levelText }}</span>
                  <span class="alert-msg">{{ alert.msg }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { CSS2DRenderer, CSS2DObject } from 'three/examples/jsm/renderers/CSS2DRenderer.js'
import * as echarts from 'echarts'
import { use } from 'echarts/core'
import { LineChart, PieChart, GaugeChart, BarChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent,
  GridComponent, DatasetComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'

use([LineChart, PieChart, GaugeChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, DatasetComponent, CanvasRenderer])

// ===================== 状态 =====================
const currentTime = ref('')
const isFullscreen = ref(false)
const threeReady = ref(false)
const hoveredCity = ref(null)
const tooltipStyle = ref({ left: '0px', top: '0px' })
const screenRef = ref(null)
const threeContainer = ref(null)
const isAutoRotating = ref(true)
const drillDownCity = ref(null)
const isDrilling = ref(false)

let scene, camera, renderer, labelRenderer, controls, animationId, earthGroup
let raycaster, mouse
let cityMeshes = []
let cityLabels = []
const cityMarkers = []
const regionLabels = []
let earthMesh = null
let cloudMesh = null
let nightMesh = null
let citiesData = []
let allRoutes = []
let allFlowDots = []
let earthGroupRef = null
let originalCameraPos = null
let originalControlsTarget = null
let zoomAnimating = false
let zoomAnimStart = 0
let zoomAnimDuration = 2000
let zoomAnimFrom = null
let zoomAnimTo = null
let zoomTargetFrom = null
let zoomTargetTo = null

const shipMetrics = ref([
  { label: '今日航班', value: '1,263', color: '#00e5ff', trend: 5.2 },
  { label: '在途船舶', value: '865', color: '#7c4dff', trend: 3.8 },
  { label: '到港班次', value: '398', color: '#ffab00', trend: -1.2 },
  { label: '延误预警', value: '12', color: '#ff5252', trend: -8.5 }
])

const topRoutes = ref([
  { name: '上海 → 洛杉矶', count: 286, rate: 94.2 },
  { name: '深圳 → 新加坡', count: 238, rate: 92.5 },
  { name: '宁波 → 鹿特丹', count: 195, rate: 89.7 },
  { name: '青岛 → 釜山', count: 186, rate: 95.3 },
  { name: '天津 → 悉尼', count: 162, rate: 88.4 }
])

const bottomStats = ref([
  { icon: '📦', title: '货运吞吐量', value: '87.6%', sub: '完成月度目标', color: '#00e5ff' },
  { icon: '🛫', title: '机场运行状况', value: '正常', sub: '航班正常率 96.2%', color: '#00e676' },
  { icon: '🌦️', title: '天气影响概况', value: '轻微', sub: '影响航线 3%', color: '#ffab00' },
  { icon: '⛽', title: '燃油使用情况', value: '12.5k', sub: '今日消耗 吨', color: '#ff6e40' },
  { icon: '⚠️', title: '关键告警概览', value: '23', sub: '待处理 5 条', color: '#ff1744' }
])

const alertList = ref([
  { id: 1, time: '11:42:18', level: 'error', levelText: '严重', msg: '马六甲海峡航线受台风影响暂停' },
  { id: 2, time: '11:38:05', level: 'warning', levelText: '告警', msg: '上海港泊位紧张，建议调整靠港时间' },
  { id: 3, time: '11:25:33', level: 'info', levelText: '提示', msg: '新加坡港完成今日货物吞吐量目标' },
  { id: 4, time: '11:10:12', level: 'warning', levelText: '告警', msg: '太平洋航线平均延误 45 分钟' },
  { id: 5, time: '10:58:49', level: 'info', levelText: '提示', msg: '欧洲航线准点率提升至 93.5%' }
])

let timeTimer

// ===================== ECharts 配置 =====================
const trendOption = ref({
  backgroundColor: 'transparent',
  grid: { left: '10%', right: '5%', top: '15%', bottom: '15%' },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
    axisLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
    axisLabel: { color: '#90a4ae', fontSize: 10 }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } },
    axisLabel: { color: '#90a4ae', fontSize: 10 }
  },
  series: [{
    name: '航班数',
    type: 'line',
    smooth: true,
    data: [320, 480, 920, 1180, 1050, 860, 560],
    lineStyle: { width: 2, color: '#00e5ff' },
    areaStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(0,229,255,0.4)' },
        { offset: 1, color: 'rgba(0,229,255,0.02)' }
      ])
    },
    symbol: 'none'
  }]
})

const regionOption = ref({
  backgroundColor: 'transparent',
  tooltip: { trigger: 'item' },
  legend: {
    orient: 'vertical',
    right: '5%',
    top: 'center',
    textStyle: { color: '#b0bec5', fontSize: 11 }
  },
  series: [{
    type: 'pie',
    radius: ['40%', '65%'],
    center: ['35%', '50%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 4, borderColor: '#0a1020', borderWidth: 2 },
    label: { show: false },
    data: [
      { value: 420, name: '亚洲', itemStyle: { color: '#00e5ff' } },
      { value: 280, name: '欧洲', itemStyle: { color: '#7c4dff' } },
      { value: 195, name: '北美', itemStyle: { color: '#ffab00' } },
      { value: 85, name: '南美', itemStyle: { color: '#ff6e40' } },
      { value: 60, name: '其他', itemStyle: { color: '#78909c' } }
    ]
  }]
})

const gauges = ref([
  { name: '准点率', option: makeGaugeOption('准点率', 92.3, '#00e5ff') },
  { name: '满载率', option: makeGaugeOption('满载率', 78.6, '#7c4dff') },
  { name: '运行率', option: makeGaugeOption('运行率', 88.4, '#00e676') },
  { name: '安全率', option: makeGaugeOption('安全率', 99.2, '#ffab00') }
])

function makeGaugeOption(name, value, color) {
  return {
    backgroundColor: 'transparent',
    series: [{
      type: 'gauge',
      startAngle: 90,
      endAngle: -270,
      pointer: { show: false },
      progress: {
        show: true,
        overlap: false,
        roundCap: true,
        clip: false,
        itemStyle: { color: color }
      },
      axisLine: { lineStyle: { width: 6, color: [[1, 'rgba(255,255,255,0.08)']] } },
      splitLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false },
      data: [{ value: value, name: name }],
      title: { offsetCenter: ['0%', '35%'], fontSize: 10, color: '#90a4ae' },
      detail: {
        valueAnimation: true,
        offsetCenter: ['0%', '-10%'],
        fontSize: 16,
        fontWeight: 'bold',
        formatter: '{value}%',
        color: color
      }
    }]
  }
}

function getRateColor(rate) {
  if (rate >= 95) return '#00e676'
  if (rate >= 90) return '#00e5ff'
  if (rate >= 85) return '#ffab00'
  return '#ff5252'
}

// ===================== 城市数据（50+全球城市，按大洲分层） =====================
function buildCitiesData() {
  return [
    // ===== 亚洲 =====
    { name: '上海', continent: '亚洲', country: '中国', type: '港口', region: '华东', inout: '进86/出92', punctuality: 94.2, business: '吞吐量 12.4M TEU', annualVolume: '4,730万TEU', activeShips: '342', nearbyPorts: ['宁波(150km)', '连云港(480km)', '温州(420km)'], mainRoutes: ['上海→洛杉矶', '上海→鹿特丹', '上海→釜山'], lat: 31.23, lon: 121.47 },
    { name: '深圳', continent: '亚洲', country: '中国', type: '港口', region: '华南', inout: '进64/出68', punctuality: 93.5, business: '华南门户 10.2M TEU', annualVolume: '2,870万TEU', activeShips: '256', nearbyPorts: ['香港(30km)', '广州(120km)', '珠海(80km)'], mainRoutes: ['深圳→新加坡', '深圳→洛杉矶', '深圳→悉尼'], lat: 22.54, lon: 114.06 },
    { name: '香港', continent: '亚洲', country: '中国', type: '枢纽港', region: '华南', inout: '进78/出80', punctuality: 95.3, business: '国际中转 8.9M TEU', annualVolume: '1,830万TEU', activeShips: '298', nearbyPorts: ['深圳(30km)', '广州(150km)', '澳门(60km)'], mainRoutes: ['香港→新加坡', '香港→洛杉矶', '香港→鹿特丹'], lat: 22.32, lon: 114.17 },
    { name: '宁波', continent: '亚洲', country: '中国', type: '港口', region: '华东', inout: '进52/出56', punctuality: 92.8, business: '深水良港 8.1M TEU', annualVolume: '3,100万TEU', activeShips: '187', nearbyPorts: ['上海(150km)', '舟山(20km)', '温州(260km)'], mainRoutes: ['宁波→鹿特丹', '宁波→洛杉矶', '宁波→釜山'], lat: 29.87, lon: 121.55 },
    { name: '青岛', continent: '亚洲', country: '中国', type: '港口', region: '华北', inout: '进38/出42', punctuality: 93.1, business: '北方枢纽 6.8M TEU', annualVolume: '2,200万TEU', activeShips: '145', nearbyPorts: ['天津(520km)', '大连(380km)', '烟台(190km)'], mainRoutes: ['青岛→釜山', '青岛→洛杉矶', '青岛→汉堡'], lat: 36.07, lon: 120.38 },
    { name: '天津', continent: '亚洲', country: '中国', type: '港口', region: '华北', inout: '进34/出36', punctuality: 90.5, business: '京津冀门户 5.6M TEU', annualVolume: '2,100万TEU', activeShips: '132', nearbyPorts: ['青岛(520km)', '大连(380km)', '秦皇岛(280km)'], mainRoutes: ['天津→悉尼', '天津→洛杉矶', '天津→新加坡'], lat: 39.00, lon: 117.72 },
    { name: '厦门', continent: '亚洲', country: '中国', type: '港口', region: '华东', inout: '进28/出30', punctuality: 91.7, business: '海峡西岸 3.2M TEU', annualVolume: '1,240万TEU', activeShips: '98', nearbyPorts: ['福州(270km)', '泉州(90km)', '汕头(220km)'], mainRoutes: ['厦门→新加坡', '厦门→洛杉矶', '厦门→鹿特丹'], lat: 24.48, lon: 118.09 },
    { name: '大连', continent: '亚洲', country: '中国', type: '港口', region: '东北', inout: '进24/出26', punctuality: 89.9, business: '东北门户 2.8M TEU', annualVolume: '950万TEU', activeShips: '87', nearbyPorts: ['天津(380km)', '青岛(380km)', '营口(220km)'], mainRoutes: ['大连→釜山', '大连→东京', '大连→鹿特丹'], lat: 38.91, lon: 121.61 },
    { name: '广州', continent: '亚洲', country: '中国', type: '港口', region: '华南', inout: '进42/出44', punctuality: 91.3, business: '华南基地 7.5M TEU', annualVolume: '2,480万TEU', activeShips: '178', nearbyPorts: ['深圳(120km)', '香港(150km)', '珠海(140km)'], mainRoutes: ['广州→新加坡', '广州→洛杉矶', '广州→迪拜'], lat: 23.13, lon: 113.26 },
    { name: '东京', continent: '亚洲', country: '日本', type: '港口', region: '东亚', inout: '进48/出50', punctuality: 91.8, business: '亚太航线 4.3M TEU', annualVolume: '460万TEU', activeShips: '156', nearbyPorts: ['横滨(30km)', '名古屋(270km)', '大阪(400km)'], mainRoutes: ['东京→洛杉矶', '东京→上海', '东京→新加坡'], lat: 35.68, lon: 139.69 },
    { name: '横滨', continent: '亚洲', country: '日本', type: '港口', region: '东亚', inout: '进32/出34', punctuality: 92.5, business: '京滨工业带 3.1M TEU', annualVolume: '290万TEU', activeShips: '112', nearbyPorts: ['东京(30km)', '名古屋(300km)', '川崎(10km)'], mainRoutes: ['横滨→洛杉矶', '横滨→上海', '横滨→釜山'], lat: 35.45, lon: 139.64 },
    { name: '釜山', continent: '亚洲', country: '韩国', type: '港口', region: '东亚', inout: '进42/出40', punctuality: 93.1, business: '东北亚中转 5.2M TEU', annualVolume: '2,270万TEU', activeShips: '168', nearbyPorts: ['光阳(160km)', '仁川(340km)', '蔚山(70km)'], mainRoutes: ['釜山→上海', '釜山→洛杉矶', '釜山→新加坡'], lat: 35.18, lon: 129.08 },
    { name: '仁川', continent: '亚洲', country: '韩国', type: '港口', region: '东亚', inout: '进24/出26', punctuality: 90.7, business: '首尔门户 2.8M TEU', annualVolume: '320万TEU', activeShips: '94', nearbyPorts: ['釜山(340km)', '平泽(50km)', '群山(180km)'], mainRoutes: ['仁川→上海', '仁川→天津', '仁川→新加坡'], lat: 37.46, lon: 126.62 },
    { name: '新加坡', continent: '亚洲', country: '新加坡', type: '枢纽港', region: '东南亚', inout: '进120/出115', punctuality: 93.8, business: '转运量 9.8M TEU', annualVolume: '3,900万TEU', activeShips: '480', nearbyPorts: ['丹戎帕拉帕斯(50km)', '巴生港(350km)', '柔佛(30km)'], mainRoutes: ['新加坡→上海', '新加坡→迪拜', '新加坡→洛杉矶'], lat: 1.35, lon: 103.82 },
    { name: '巴生港', continent: '亚洲', country: '马来西亚', type: '港口', region: '东南亚', inout: '进28/出30', punctuality: 88.6, business: '马六甲要港 4.1M TEU', annualVolume: '1,360万TEU', activeShips: '124', nearbyPorts: ['新加坡(350km)', '槟城(380km)', '丹戎帕拉帕斯(330km)'], mainRoutes: ['巴生港→迪拜', '巴生港→上海', '巴生港→鹿特丹'], lat: 3.00, lon: 101.40 },
    { name: '曼谷', continent: '亚洲', country: '泰国', type: '港口', region: '东南亚', inout: '进20/出22', punctuality: 86.4, business: '中南半岛 2.3M TEU', annualVolume: '870万TEU', activeShips: '78', nearbyPorts: ['林查班(110km)', '宋卡(750km)', '拉廊(500km)'], mainRoutes: ['曼谷→新加坡', '曼谷→香港', '曼谷→上海'], lat: 13.76, lon: 100.50 },
    { name: '雅加达', continent: '亚洲', country: '印尼', type: '港口', region: '东南亚', inout: '进18/出19', punctuality: 85.8, business: '千岛之国枢纽 1.8M TEU', annualVolume: '750万TEU', activeShips: '67', nearbyPorts: ['泗水(660km)', '三宝垄(400km)', '棉兰(1400km)'], mainRoutes: ['雅加达→新加坡', '雅加达→上海', '雅加达→迪拜'], lat: -6.21, lon: 106.85 },
    { name: '胡志明', continent: '亚洲', country: '越南', type: '港口', region: '东南亚', inout: '进16/出17', punctuality: 87.3, business: '湄公河门户 1.5M TEU', annualVolume: '630万TEU', activeShips: '56', nearbyPorts: ['头顿(80km)', '岘港(600km)', '海防(1100km)'], mainRoutes: ['胡志明→新加坡', '胡志明→上海', '胡志明→香港'], lat: 10.82, lon: 106.63 },
    { name: '迪拜', continent: '亚洲', country: '阿联酋', type: '中转港', region: '中东', inout: '进56/出58', punctuality: 92.4, business: '中东枢纽 4.6M TEU', annualVolume: '1,530万TEU', activeShips: '210', nearbyPorts: ['阿布扎比(130km)', '沙迦(20km)', '富查伊拉(120km)'], mainRoutes: ['迪拜→鹿特丹', '迪拜→新加坡', '迪拜→上海'], lat: 25.20, lon: 55.27 },
    { name: '孟买', continent: '亚洲', country: '印度', type: '港口', region: '南亚', inout: '进22/出24', punctuality: 84.5, business: '印度洋门户 3.2M TEU', annualVolume: '530万TEU', activeShips: '85', nearbyPorts: ['那瓦舍瓦(10km)', '果阿(400km)', '科钦(1000km)'], mainRoutes: ['孟买→迪拜', '孟买→新加坡', '孟买→鹿特丹'], lat: 18.95, lon: 72.84 },
    { name: '科伦坡', continent: '亚洲', country: '斯里兰卡', type: '中转港', region: '南亚', inout: '进14/出15', punctuality: 88.2, business: '印度洋十字路口 2.1M TEU', annualVolume: '720万TEU', activeShips: '58', nearbyPorts: ['汉班托塔(160km)', '金奈(680km)', '特里凡得琅(400km)'], mainRoutes: ['科伦坡→迪拜', '科伦坡→新加坡', '科伦坡→鹿特丹'], lat: 6.93, lon: 79.85 },

    // ===== 欧洲 =====
    { name: '鹿特丹', continent: '欧洲', country: '荷兰', type: '港口', region: '西欧', inout: '进45/出48', punctuality: 91.2, business: '欧陆门户 6.5M TEU', annualVolume: '1,470万TEU', activeShips: '186', nearbyPorts: ['安特卫普(80km)', '阿姆斯特丹(75km)', '汉堡(410km)'], mainRoutes: ['鹿特丹→上海', '鹿特丹→新加坡', '鹿特丹→迪拜'], lat: 51.92, lon: 4.48 },
    { name: '汉堡', continent: '欧洲', country: '德国', type: '港口', region: '西欧', inout: '进38/出36', punctuality: 88.7, business: '波罗的海门户 2.8M TEU', annualVolume: '870万TEU', activeShips: '145', nearbyPorts: ['不来梅(120km)', '鹿特丹(410km)', '基尔(90km)'], mainRoutes: ['汉堡→上海', '汉堡→新加坡', '汉堡→洛杉矶'], lat: 53.55, lon: 9.99 },
    { name: '安特卫普', continent: '欧洲', country: '比利时', type: '港口', region: '西欧', inout: '进34/出36', punctuality: 90.3, business: '欧洲第二大港 5.8M TEU', annualVolume: '1,250万TEU', activeShips: '152', nearbyPorts: ['鹿特丹(80km)', '泽布吕赫(90km)', '根特(55km)'], mainRoutes: ['安特卫普→上海', '安特卫普→新加坡', '安特卫普→迪拜'], lat: 51.26, lon: 4.37 },
    { name: '伦敦', continent: '欧洲', country: '英国', type: '港口', region: '西欧', inout: '进28/出30', punctuality: 87.6, business: '英伦三岛门户 3.6M TEU', annualVolume: '430万TEU', activeShips: '108', nearbyPorts: ['南安普顿(120km)', '费利克斯托(130km)', '多佛(120km)'], mainRoutes: ['伦敦→鹿特丹', '伦敦→上海', '伦敦→纽约'], lat: 51.51, lon: 0.06 },
    { name: '伊斯坦布尔', continent: '欧洲', country: '土耳其', type: '港口', region: '东南欧', inout: '进26/出28', punctuality: 86.9, business: '欧亚交汇 2.4M TEU', annualVolume: '360万TEU', activeShips: '95', nearbyPorts: ['伊兹密尔(330km)', '萨姆松(600km)', '布尔加斯(250km)'], mainRoutes: ['伊斯坦布尔→鹿特丹', '伊斯坦布尔→迪拜', '伊斯坦布尔→新加坡'], lat: 41.01, lon: 28.98 },
    { name: '比雷埃夫斯', continent: '欧洲', country: '希腊', type: '港口', region: '南欧', inout: '进22/出23', punctuality: 88.1, business: '地中海枢纽 2.1M TEU', annualVolume: '560万TEU', activeShips: '82', nearbyPorts: ['塞萨洛尼基(500km)', '伊兹密尔(300km)', '瓦莱塔(850km)'], mainRoutes: ['比雷埃夫斯→上海', '比雷埃夫斯→鹿特丹', '比雷埃夫斯→迪拜'], lat: 37.94, lon: 23.64 },
    { name: '巴塞罗那', continent: '欧洲', country: '西班牙', type: '港口', region: '南欧', inout: '进18/出19', punctuality: 89.5, business: '西地中海枢纽 1.8M TEU', annualVolume: '350万TEU', activeShips: '68', nearbyPorts: ['瓦伦西亚(350km)', '塔拉戈纳(100km)', '马赛(350km)'], mainRoutes: ['巴塞罗那→上海', '巴塞罗那→鹿特丹', '巴塞罗那→迪拜'], lat: 41.39, lon: 2.19 },

    // ===== 北美 =====
    { name: '洛杉矶', continent: '北美', country: '美国', type: '港口', region: '西海岸', inout: '进68/出72', punctuality: 89.5, business: '进口量 7.2M TEU', annualVolume: '990万TEU', activeShips: '245', nearbyPorts: ['长滩(5km)', '奥克兰(600km)', '圣地亚哥(190km)'], mainRoutes: ['洛杉矶→上海', '洛杉矶→东京', '洛杉矶→釜山'], lat: 34.05, lon: -118.25 },
    { name: '长滩', continent: '北美', country: '美国', type: '港口', region: '西海岸', inout: '进42/出44', punctuality: 90.2, business: '美西最大港群 5.8M TEU', annualVolume: '810万TEU', activeShips: '186', nearbyPorts: ['洛杉矶(5km)', '奥克兰(600km)', '圣地亚哥(185km)'], mainRoutes: ['长滩→上海', '长滩→深圳', '长滩→东京'], lat: 33.77, lon: -118.19 },
    { name: '纽约', continent: '北美', country: '美国', type: '港口', region: '东海岸', inout: '进38/出40', punctuality: 91.3, business: '美东枢纽 4.5M TEU', annualVolume: '760万TEU', activeShips: '165', nearbyPorts: ['波士顿(350km)', '费城(150km)', '巴尔的摩(300km)'], mainRoutes: ['纽约→鹿特丹', '纽约→上海', '纽约→汉堡'], lat: 40.71, lon: -74.01 },
    { name: '温哥华', continent: '北美', country: '加拿大', type: '港口', region: '西海岸', inout: '进20/出22', punctuality: 92.5, business: '北美西北门户 2.1M TEU', annualVolume: '350万TEU', activeShips: '78', nearbyPorts: ['西雅图(190km)', '鲁珀特王子港(700km)', '塔科马(210km)'], mainRoutes: ['温哥华→上海', '温哥华→东京', '温哥华→釜山'], lat: 49.28, lon: -123.12 },
    { name: '休斯顿', continent: '北美', country: '美国', type: '港口', region: '墨西哥湾', inout: '进24/出25', punctuality: 88.9, business: '墨西哥湾枢纽 2.8M TEU', annualVolume: '310万TEU', activeShips: '92', nearbyPorts: ['新奥尔良(510km)', '科珀斯克里斯蒂(320km)', '加尔维斯顿(80km)'], mainRoutes: ['休斯顿→鹿特丹', '休斯顿→上海', '休斯顿→巴拿马'], lat: 29.76, lon: -95.37 },

    // ===== 南美 =====
    { name: '桑托斯', continent: '南美', country: '巴西', type: '港口', region: '东南海岸', inout: '进20/出21', punctuality: 84.7, business: '南美最大港 3.4M TEU', annualVolume: '480万TEU', activeShips: '76', nearbyPorts: ['里约(350km)', '巴拉那瓜(280km)', '伊塔雅伊(160km)'], mainRoutes: ['桑托斯→上海', '桑托斯→鹿特丹', '桑托斯→新加坡'], lat: -23.96, lon: -46.33 },
    { name: '布宜诺斯艾利斯', continent: '南美', country: '阿根廷', type: '港口', region: '东南海岸', inout: '进14/出15', punctuality: 83.2, business: '拉普拉塔河门户 1.8M TEU', annualVolume: '180万TEU', activeShips: '54', nearbyPorts: ['蒙得维的亚(220km)', '拉普拉塔(60km)', '布兰卡港(300km)'], mainRoutes: ['布宜诺斯艾利斯→上海', '布宜诺斯艾利斯→桑托斯', '布宜诺斯艾利斯→鹿特丹'], lat: -34.61, lon: -58.38 },
    { name: '卡亚俄', continent: '南美', country: '秘鲁', type: '港口', region: '西海岸', inout: '进10/出11', punctuality: 85.6, business: '南美西岸枢纽 1.2M TEU', annualVolume: '230万TEU', activeShips: '42', nearbyPorts: ['派塔(880km)', '马塔拉尼(600km)', '伊基克(900km)'], mainRoutes: ['卡亚俄→上海', '卡亚俄→洛杉矶', '卡亚俄→釜山'], lat: -12.05, lon: -77.15 },

    // ===== 非洲 =====
    { name: '德班', continent: '非洲', country: '南非', type: '港口', region: '南部非洲', inout: '进16/出17', punctuality: 86.8, business: '非洲最繁忙港 2.5M TEU', annualVolume: '290万TEU', activeShips: '62', nearbyPorts: ['理查兹湾(160km)', '东伦敦(460km)', '开普敦(1400km)'], mainRoutes: ['德班→迪拜', '德班→新加坡', '德班→鹿特丹'], lat: -29.86, lon: 31.03 },
    { name: '开普敦', continent: '非洲', country: '南非', type: '港口', region: '南部非洲', inout: '进12/出13', punctuality: 87.5, business: '好望角门户 1.6M TEU', annualVolume: '210万TEU', activeShips: '48', nearbyPorts: ['德班(1400km)', '伊丽莎白港(660km)', '萨尔达尼亚湾(120km)'], mainRoutes: ['开普敦→鹿特丹', '开普敦→迪拜', '开普敦→新加坡'], lat: -33.92, lon: 18.42 },
    { name: '蒙巴萨', continent: '非洲', country: '肯尼亚', type: '港口', region: '东非', inout: '进10/出10', punctuality: 83.9, business: '东非门户 1.1M TEU', annualVolume: '140万TEU', activeShips: '38', nearbyPorts: ['达累斯萨拉姆(330km)', '桑给巴尔(250km)', '拉穆(240km)'], mainRoutes: ['蒙巴萨→迪拜', '蒙巴萨→新加坡', '蒙巴萨→鹿特丹'], lat: -4.04, lon: 39.67 },

    // ===== 大洋洲 =====
    { name: '悉尼', continent: '大洋洲', country: '澳大利亚', type: '港口', region: '东南海岸', inout: '进32/出35', punctuality: 90.1, business: '大洋航线 3.1M TEU', annualVolume: '260万TEU', activeShips: '115', nearbyPorts: ['墨尔本(870km)', '布里斯班(930km)', '纽卡斯尔(160km)'], mainRoutes: ['悉尼→上海', '悉尼→新加坡', '悉尼→洛杉矶'], lat: -33.87, lon: 151.21 },
    { name: '墨尔本', continent: '大洋洲', country: '澳大利亚', type: '港口', region: '东南海岸', inout: '进24/出26', punctuality: 89.8, business: '澳洲最大港 3.5M TEU', annualVolume: '300万TEU', activeShips: '98', nearbyPorts: ['悉尼(870km)', '阿德莱德(730km)', '吉朗(75km)'], mainRoutes: ['墨尔本→上海', '墨尔本→新加坡', '墨尔本→洛杉矶'], lat: -37.81, lon: 144.96 },
    { name: '奥克兰', continent: '大洋洲', country: '新西兰', type: '港口', region: '北岛', inout: '进12/出13', punctuality: 91.6, business: '新西兰门户 1.1M TEU', annualVolume: '150万TEU', activeShips: '42', nearbyPorts: ['陶朗加(210km)', '惠灵顿(650km)', '基督城(1070km)'], mainRoutes: ['奥克兰→悉尼', '奥克兰→上海', '奥克兰→洛杉矶'], lat: -36.85, lon: 174.76 }
  ]
}

// ===================== 地图表面地名标注（天地图风格） =====================
function buildRegionLabels() {
  return [
    // === 大洲 ===
    { name: '亚  洲', lat: 45, lon: 100, fontSize: 16, color: 'rgba(255,255,255,0.3)', fontWeight: 'bold' },
    { name: '欧  洲', lat: 52, lon: 15, fontSize: 16, color: 'rgba(255,255,255,0.3)', fontWeight: 'bold' },
    { name: '北 美 洲', lat: 48, lon: -100, fontSize: 16, color: 'rgba(255,255,255,0.3)', fontWeight: 'bold' },
    { name: '南 美 洲', lat: -15, lon: -60, fontSize: 16, color: 'rgba(255,255,255,0.3)', fontWeight: 'bold' },
    { name: '非  洲', lat: 2, lon: 25, fontSize: 16, color: 'rgba(255,255,255,0.3)', fontWeight: 'bold' },
    { name: '大 洋 洲', lat: -23, lon: 135, fontSize: 14, color: 'rgba(255,255,255,0.3)', fontWeight: 'bold' },

    // === 海洋 ===
    { name: '太  平  洋', lat: -10, lon: -140, fontSize: 18, color: 'rgba(100,180,255,0.22)', fontWeight: 'bold' },
    { name: '大  西  洋', lat: -5, lon: -30, fontSize: 18, color: 'rgba(100,180,255,0.22)', fontWeight: 'bold' },
    { name: '印  度  洋', lat: -20, lon: 65, fontSize: 16, color: 'rgba(100,180,255,0.22)', fontWeight: 'bold' },
    { name: '北  冰  洋', lat: 82, lon: 20, fontSize: 14, color: 'rgba(120,200,255,0.2)', fontWeight: 'bold' },

    // === 海域 ===
    { name: '南海', lat: 14, lon: 115, fontSize: 12, color: 'rgba(100,180,255,0.25)' },
    { name: '东海', lat: 28, lon: 127, fontSize: 11, color: 'rgba(100,180,255,0.25)' },
    { name: '黄海', lat: 35, lon: 124, fontSize: 11, color: 'rgba(100,180,255,0.25)' },
    { name: '日本海', lat: 40, lon: 135, fontSize: 12, color: 'rgba(100,180,255,0.25)' },
    { name: '孟加拉湾', lat: 14, lon: 88, fontSize: 11, color: 'rgba(100,180,255,0.25)' },
    { name: '阿拉伯海', lat: 16, lon: 65, fontSize: 12, color: 'rgba(100,180,255,0.25)' },
    { name: '地中海', lat: 36, lon: 16, fontSize: 12, color: 'rgba(100,180,255,0.25)' },
    { name: '北海', lat: 56, lon: 4, fontSize: 10, color: 'rgba(100,180,255,0.25)' },
    { name: '波罗的海', lat: 58, lon: 20, fontSize: 11, color: 'rgba(100,180,255,0.25)' },
    { name: '加勒比海', lat: 15, lon: -75, fontSize: 11, color: 'rgba(100,180,255,0.25)' },
    { name: '墨西哥湾', lat: 25, lon: -90, fontSize: 11, color: 'rgba(100,180,255,0.25)' },
    { name: '珊瑚海', lat: -17, lon: 156, fontSize: 11, color: 'rgba(100,180,255,0.25)' },
    { name: '红海', lat: 22, lon: 38, fontSize: 10, color: 'rgba(100,180,255,0.25)' },

    // === 国家/地区 ===
    { name: '中国', lat: 36, lon: 104, fontSize: 14, color: 'rgba(255,255,255,0.28)' },
    { name: '日本', lat: 37, lon: 138, fontSize: 11, color: 'rgba(255,255,255,0.25)' },
    { name: '韩国', lat: 36.5, lon: 127.5, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '印度', lat: 22, lon: 79, fontSize: 12, color: 'rgba(255,255,255,0.25)' },
    { name: '越南', lat: 16, lon: 107, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '泰国', lat: 15, lon: 101, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '印尼', lat: -3, lon: 115, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '马来西亚', lat: 4, lon: 109, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '俄罗斯', lat: 62, lon: 95, fontSize: 13, color: 'rgba(255,255,255,0.25)' },
    { name: '德国', lat: 51, lon: 10, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '法国', lat: 47, lon: 3, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '英国', lat: 54, lon: -3, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '西班牙', lat: 40, lon: -3, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '意大利', lat: 42, lon: 12, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '土耳其', lat: 39, lon: 35, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '美国', lat: 38, lon: -97, fontSize: 13, color: 'rgba(255,255,255,0.25)' },
    { name: '加拿大', lat: 56, lon: -106, fontSize: 12, color: 'rgba(255,255,255,0.25)' },
    { name: '墨西哥', lat: 23, lon: -102, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '巴西', lat: -8, lon: -55, fontSize: 12, color: 'rgba(255,255,255,0.25)' },
    { name: '阿根廷', lat: -38, lon: -64, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '澳大利亚', lat: -26, lon: 134, fontSize: 12, color: 'rgba(255,255,255,0.25)' },
    { name: '南非', lat: -30, lon: 25, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '埃及', lat: 27, lon: 30, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '尼日利亚', lat: 9, lon: 8, fontSize: 10, color: 'rgba(255,255,255,0.25)' },
    { name: '肯尼亚', lat: 1, lon: 38, fontSize: 9, color: 'rgba(255,255,255,0.25)' },

    // === 重要地理区域/海峡 ===
    { name: '马六甲海峡', lat: 2.5, lon: 103, fontSize: 10, color: 'rgba(255,200,100,0.22)' },
    { name: '苏伊士运河', lat: 30.5, lon: 32.5, fontSize: 10, color: 'rgba(255,200,100,0.22)' },
    { name: '巴拿马运河', lat: 9, lon: -79.5, fontSize: 10, color: 'rgba(255,200,100,0.22)' },
    { name: '波斯湾', lat: 26.5, lon: 51, fontSize: 10, color: 'rgba(255,200,100,0.22)' },
    { name: '英吉利海峡', lat: 50.5, lon: 1, fontSize: 9, color: 'rgba(255,200,100,0.22)' },

    // === 中国主要城市/地区 ===
    { name: '北京', lat: 39.9, lon: 116.4, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '成都', lat: 30.6, lon: 104.1, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '重庆', lat: 29.6, lon: 106.5, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '武汉', lat: 30.6, lon: 114.3, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '西安', lat: 34.3, lon: 108.9, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '拉萨', lat: 29.7, lon: 91.1, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '乌鲁木齐', lat: 43.8, lon: 87.6, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '哈尔滨', lat: 45.8, lon: 126.5, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '昆明', lat: 25.0, lon: 102.7, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '台湾', lat: 23.7, lon: 121.0, fontSize: 9, color: 'rgba(255,255,255,0.2)' },

    // === 世界主要城市/地区 ===
    { name: '旧金山', lat: 37.77, lon: -122.42, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '芝加哥', lat: 41.88, lon: -87.63, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '莫斯科', lat: 55.75, lon: 37.62, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '巴黎', lat: 48.86, lon: 2.35, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '德黑兰', lat: 35.69, lon: 51.39, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '开罗', lat: 30.04, lon: 31.24, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '拉各斯', lat: 6.45, lon: 3.40, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '内罗毕', lat: -1.29, lon: 36.82, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '利马', lat: -12.05, lon: -77.04, fontSize: 9, color: 'rgba(255,255,255,0.2)' },
    { name: '圣地亚哥', lat: -33.45, lon: -70.67, fontSize: 9, color: 'rgba(255,255,255,0.2)' }
  ]
}

// ===================== Three.js 地球场景 =====================
function initThreeScene() {
  const container = threeContainer.value
  if (!container) return

  const width = container.clientWidth
  const height = container.clientHeight

  scene = new THREE.Scene()
  scene.fog = new THREE.FogExp2(0x05080f, 0.0015)

  camera = new THREE.PerspectiveCamera(55, width / height, 0.1, 2000)
  camera.position.set(0, 2, 30)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.1
  container.appendChild(renderer.domElement)

  // CSS2D 文字标签渲染器
  labelRenderer = new CSS2DRenderer()
  labelRenderer.setSize(width, height)
  labelRenderer.domElement.style.position = 'absolute'
  labelRenderer.domElement.style.top = '0'
  labelRenderer.domElement.style.left = '0'
  labelRenderer.domElement.style.pointerEvents = 'none'
  container.appendChild(labelRenderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.minDistance = 12
  controls.maxDistance = 60
  controls.autoRotate = true
  controls.autoRotateSpeed = 0.6
  controls.enablePan = false

  // 灯光
  const ambientLight = new THREE.AmbientLight(0x404040, 2.5)
  scene.add(ambientLight)

  const sunLight = new THREE.DirectionalLight(0xffffff, 2.2)
  sunLight.position.set(20, 10, 20)
  scene.add(sunLight)

  const blueLight = new THREE.DirectionalLight(0x00aaff, 1.2)
  blueLight.position.set(-20, -5, -10)
  scene.add(blueLight)

  // 星空
  createStars()

  earthGroup = new THREE.Group()
  earthGroupRef = earthGroup
  scene.add(earthGroup)

  // 地球
  const earthGeometry = new THREE.SphereGeometry(8, 128, 128)
  const earthMaterial = new THREE.MeshPhongMaterial({
    color: 0x0a2a4a,
    emissive: 0x051020,
    specular: 0x333333,
    shininess: 25,
    transparent: true,
    opacity: 0.98
  })
  earthMesh = new THREE.Mesh(earthGeometry, earthMaterial)
  earthGroup.add(earthMesh)

  // 加载高清地球纹理
  const textureLoader = new THREE.TextureLoader()
  const mapSources = [
    { map: 'https://unpkg.com/three-globe/example/img/earth-blue-marble.jpg', bump: 'https://unpkg.com/three-globe/example/img/earth-topology.png', night: 'https://unpkg.com/three-globe/example/img/earth-dark.jpg' },
    { map: 'https://threejs.org/examples/textures/planets/earth_atmos_2048.jpg', bump: 'https://threejs.org/examples/textures/planets/earth_normal_2048.jpg', night: 'https://threejs.org/examples/textures/planets/earth_lights_2048.png' }
  ]
  let sourceIndex = 0
  function tryLoadEarthTextures() {
    if (sourceIndex >= mapSources.length) {
      console.warn('所有地球纹理源加载失败，使用纯色地球')
      return
    }
    const src = mapSources[sourceIndex]
    textureLoader.load(src.map,
      (mapTexture) => {
        mapTexture.colorSpace = THREE.SRGBColorSpace
        earthMesh.material.map = mapTexture
        earthMesh.material.color.set(0xffffff)
        earthMesh.material.emissive.set(0x112233)
        earthMesh.material.emissiveIntensity = 0.25
        earthMesh.material.needsUpdate = true
        textureLoader.load(src.bump,
          (bumpTexture) => {
            earthMesh.material.bumpMap = bumpTexture
            earthMesh.material.bumpScale = 0.06
            earthMesh.material.needsUpdate = true
          },
          undefined,
          () => { /* 可选 */ }
        )
      },
      undefined,
      () => {
        sourceIndex++
        tryLoadEarthTextures()
      }
    )
  }
  tryLoadEarthTextures()

  // 夜间灯光图层 - 始终以低透明度叠加在地球上显示城市灯光
  const nightGeometry = new THREE.SphereGeometry(8.02, 128, 128)
  const nightMaterial = new THREE.MeshBasicMaterial({
    color: 0x000000,
    transparent: true,
    opacity: 0,
    depthWrite: false
  })
  nightMesh = new THREE.Mesh(nightGeometry, nightMaterial)
  earthGroup.add(nightMesh)

  function tryLoadNightTexture() {
    const src = mapSources[0]
    textureLoader.load(src.night,
      (nightTex) => {
        nightTex.colorSpace = THREE.SRGBColorSpace
        nightMesh.material.map = nightTex
        nightMesh.material.opacity = 0.45
        nightMesh.material.needsUpdate = true
      },
      undefined,
      () => {
        // 夜间图加载失败，用程序化点阵替代
        createProceduralNightLights()
      }
    )
  }
  tryLoadNightTexture()

  // 云层
  const cloudGeometry = new THREE.SphereGeometry(8.12, 128, 128)
  const cloudMaterial = new THREE.MeshPhongMaterial({
    color: 0xffffff,
    transparent: true,
    opacity: 0,
    side: THREE.DoubleSide,
    depthWrite: false
  })
  cloudMesh = new THREE.Mesh(cloudGeometry, cloudMaterial)
  earthGroup.add(cloudMesh)
  textureLoader.load('https://unpkg.com/three-globe/example/img/earth-clouds.png',
    (cloudTexture) => {
      cloudTexture.colorSpace = THREE.SRGBColorSpace
      cloudMesh.material.map = cloudTexture
      cloudMesh.material.opacity = 0.35
      cloudMesh.material.needsUpdate = true
    },
    undefined,
    () => { /* 可选 */ }
  )

  // 地球经纬线网格
  const wireGeometry = new THREE.WireframeGeometry(new THREE.SphereGeometry(8.08, 24, 24))
  const wireMaterial = new THREE.LineBasicMaterial({ color: 0x1a5a8a, transparent: true, opacity: 0.12 })
  const wireframe = new THREE.LineSegments(wireGeometry, wireMaterial)
  earthGroup.add(wireframe)

  // 大气层光晕（两层）
  const atmos1Geom = new THREE.SphereGeometry(8.5, 64, 64)
  const atmos1Mat = new THREE.MeshBasicMaterial({
    color: 0x00aaff, transparent: true, opacity: 0.07, side: THREE.BackSide
  })
  earthGroup.add(new THREE.Mesh(atmos1Geom, atmos1Mat))

  const atmos2Geom = new THREE.SphereGeometry(9.2, 64, 64)
  const atmos2Mat = new THREE.MeshBasicMaterial({
    color: 0x0066ff, transparent: true, opacity: 0.025, side: THREE.BackSide
  })
  earthGroup.add(new THREE.Mesh(atmos2Geom, atmos2Mat))

  // ===== 城市标记 =====
  citiesData = buildCitiesData()
  citiesData.forEach(city => {
    createCityMarker(city)
  })

  // ===== 地图表面地名标注（天地图风格） =====
  const regionLabelsData = buildRegionLabels()
  regionLabelsData.forEach((label, idx) => {
    createRegionLabel(label, idx)
  })

  // ===== 航线 =====
  allRoutes = []
  allFlowDots = []
  if (!earthGroup.userData) earthGroup.userData = {}
  earthGroup.userData.flowDots = []

  const routes = buildFlightRoutes()
  routes.forEach(([fromName, toName]) => {
    const fromCity = citiesData.find(c => c.name === fromName)
    const toCity = citiesData.find(c => c.name === toName)
    if (fromCity && toCity) createRoute(fromCity, toCity)
  })

  // 射线交互
  raycaster = new THREE.Raycaster()
  mouse = new THREE.Vector2()
  renderer.domElement.addEventListener('mousemove', onMouseMove)
  renderer.domElement.addEventListener('click', onMouseClick)
  window.addEventListener('resize', onSceneResize)

  // 保存初始相机状态
  originalCameraPos = camera.position.clone()
  originalControlsTarget = controls.target.clone()

  threeReady.value = true
  animate()
}

function buildFlightRoutes() {
  return [
    // 亚洲内部
    ['上海', '东京'], ['上海', '釜山'], ['上海', '新加坡'], ['上海', '香港'],
    ['深圳', '新加坡'], ['深圳', '东京'], ['深圳', '曼谷'],
    ['香港', '新加坡'], ['香港', '东京'], ['香港', '釜山'],
    ['宁波', '釜山'], ['宁波', '东京'], ['宁波', '新加坡'],
    ['青岛', '釜山'], ['青岛', '东京'], ['青岛', '仁川'],
    ['天津', '仁川'], ['天津', '东京'], ['大连', '釜山'], ['大连', '东京'],
    ['厦门', '新加坡'], ['厦门', '胡志明'], ['广州', '新加坡'], ['广州', '曼谷'],
    ['釜山', '东京'], ['新加坡', '巴生港'], ['新加坡', '雅加达'],
    ['新加坡', '曼谷'], ['新加坡', '科伦坡'], ['新加坡', '孟买'],
    ['迪拜', '孟买'], ['迪拜', '科伦坡'],

    // 亚洲 → 欧洲
    ['上海', '鹿特丹'], ['上海', '汉堡'], ['深圳', '鹿特丹'],
    ['宁波', '鹿特丹'], ['青岛', '汉堡'], ['香港', '鹿特丹'],
    ['新加坡', '鹿特丹'], ['新加坡', '汉堡'], ['新加坡', '安特卫普'],
    ['迪拜', '鹿特丹'], ['迪拜', '汉堡'], ['迪拜', '伊斯坦布尔'],
    ['孟买', '鹿特丹'], ['科伦坡', '鹿特丹'],

    // 亚洲 → 北美
    ['上海', '洛杉矶'], ['上海', '长滩'], ['深圳', '洛杉矶'],
    ['香港', '洛杉矶'], ['宁波', '洛杉矶'], ['青岛', '洛杉矶'],
    ['天津', '洛杉矶'], ['东京', '洛杉矶'], ['釜山', '洛杉矶'],
    ['新加坡', '洛杉矶'], ['上海', '纽约'], ['深圳', '纽约'],
    ['上海', '温哥华'], ['东京', '温哥华'],

    // 亚洲 → 大洋洲
    ['上海', '悉尼'], ['深圳', '悉尼'], ['香港', '悉尼'],
    ['新加坡', '悉尼'], ['新加坡', '墨尔本'], ['上海', '墨尔本'],
    ['香港', '墨尔本'], ['悉尼', '奥克兰'], ['上海', '奥克兰'],

    // 亚洲 → 南美
    ['上海', '桑托斯'], ['新加坡', '桑托斯'], ['上海', '卡亚俄'],
    ['深圳', '布宜诺斯艾利斯'],

    // 亚洲 → 非洲
    ['上海', '德班'], ['新加坡', '德班'], ['迪拜', '德班'],
    ['迪拜', '蒙巴萨'], ['新加坡', '开普敦'], ['上海', '开普敦'],

    // 欧洲内部
    ['鹿特丹', '汉堡'], ['鹿特丹', '安特卫普'], ['鹿特丹', '伦敦'],
    ['汉堡', '伦敦'], ['鹿特丹', '比雷埃夫斯'], ['汉堡', '伊斯坦布尔'],
    ['安特卫普', '巴塞罗那'], ['比雷埃夫斯', '伊斯坦布尔'],

    // 北美内部
    ['洛杉矶', '长滩'], ['洛杉矶', '纽约'], ['洛杉矶', '休斯顿'],
    ['纽约', '休斯顿'], ['洛杉矶', '温哥华'],

    // 欧洲 → 北美
    ['鹿特丹', '纽约'], ['汉堡', '纽约'], ['鹿特丹', '洛杉矶'],
    ['伦敦', '纽约'], ['安特卫普', '纽约'],

    // 欧洲 → 南美
    ['鹿特丹', '桑托斯'], ['鹿特丹', '布宜诺斯艾利斯'], ['汉堡', '桑托斯'],

    // 欧洲 → 非洲
    ['鹿特丹', '德班'], ['鹿特丹', '开普敦'], ['汉堡', '开普敦'],
    ['比雷埃夫斯', '德班'], ['伊斯坦布尔', '蒙巴萨'],

    // 北美 → 南美
    ['洛杉矶', '卡亚俄'], ['休斯顿', '桑托斯'], ['纽约', '布宜诺斯艾利斯'],

    // 北美 → 大洋洲
    ['洛杉矶', '悉尼'], ['洛杉矶', '墨尔本'], ['洛杉矶', '奥克兰'],

    // 南美内部
    ['桑托斯', '布宜诺斯艾利斯'],

    // 非洲内部
    ['德班', '开普敦'], ['德班', '蒙巴萨']
  ]
}

function createStars() {
  const starGeometry = new THREE.BufferGeometry()
  const starCount = 2500
  const positions = new Float32Array(starCount * 3)
  const sizes = new Float32Array(starCount)
  for (let i = 0; i < starCount * 3; i += 3) {
    positions[i] = (Math.random() - 0.5) * 200
    positions[i + 1] = (Math.random() - 0.5) * 200
    positions[i + 2] = (Math.random() - 0.5) * 200
    sizes[i / 3] = Math.random() * 0.25 + 0.05
  }
  starGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  starGeometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1))
  const starMaterial = new THREE.PointsMaterial({
    color: 0xffffff, size: 0.15, transparent: true, opacity: 0.8, sizeAttenuation: true
  })
  const stars = new THREE.Points(starGeometry, starMaterial)
  scene.add(stars)
}

function createProceduralNightLights() {
  // 程序化生成夜间灯光点阵，模拟城市灯光分布
  if (!nightMesh || !nightMesh.material.map) return
  const canvas = document.createElement('canvas')
  canvas.width = 2048
  canvas.height = 1024
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#000000'
  ctx.fillRect(0, 0, canvas.width, canvas.height)

  // 基于经纬度投射灯光点到 canvas
  const nightPoints = [
    // 东亚
    { lng: 121.5, lat: 31.2, r: 8 }, { lng: 114.1, lat: 22.5, r: 7 },
    { lng: 120.4, lat: 36.1, r: 6 }, { lng: 113.3, lat: 23.1, r: 6 },
    { lng: 117.7, lat: 39.0, r: 6 }, { lng: 121.6, lat: 38.9, r: 5 },
    { lng: 118.1, lat: 24.5, r: 5 }, { lng: 116.4, lat: 39.9, r: 7 },
    { lng: 104.1, lat: 30.6, r: 5 }, { lng: 106.5, lat: 29.6, r: 5 },
    { lng: 139.7, lat: 35.7, r: 7 }, { lng: 139.6, lat: 35.4, r: 5 },
    { lng: 129.1, lat: 35.2, r: 6 }, { lng: 126.6, lat: 37.5, r: 5 },
    { lng: 103.8, lat: 1.3, r: 6 },
    // 南亚
    { lng: 72.8, lat: 19.0, r: 6 }, { lng: 79.9, lat: 6.9, r: 4 },
    { lng: 88.4, lat: 22.6, r: 5 },
    // 中东
    { lng: 55.3, lat: 25.2, r: 6 },
    // 欧洲
    { lng: 4.5, lat: 51.9, r: 6 }, { lng: 10.0, lat: 53.6, r: 6 },
    { lng: 4.4, lat: 51.3, r: 5 }, { lng: 0.1, lat: 51.5, r: 6 },
    { lng: 2.2, lat: 41.4, r: 5 }, { lng: 23.6, lat: 37.9, r: 5 },
    { lng: 29.0, lat: 41.0, r: 5 }, { lng: 37.6, lat: 55.8, r: 6 },
    { lng: 2.3, lat: 48.9, r: 6 }, { lng: 12.5, lat: 41.9, r: 5 },
    // 北美
    { lng: -118.3, lat: 34.1, r: 8 }, { lng: -74.0, lat: 40.7, r: 8 },
    { lng: -123.1, lat: 49.3, r: 5 }, { lng: -95.4, lat: 29.8, r: 6 },
    { lng: -87.6, lat: 41.9, r: 6 }, { lng: -122.4, lat: 37.8, r: 5 },
    // 南美
    { lng: -46.3, lat: -24.0, r: 6 }, { lng: -58.4, lat: -34.6, r: 5 },
    { lng: -77.0, lat: -12.1, r: 4 },
    // 非洲
    { lng: 31.0, lat: -29.9, r: 5 }, { lng: 18.4, lat: -33.9, r: 4 },
    { lng: 39.7, lat: -4.0, r: 4 }, { lng: 30.0, lat: 27.0, r: 5 },
    // 大洋洲
    { lng: 151.2, lat: -33.9, r: 6 }, { lng: 145.0, lat: -37.8, r: 5 },
    { lng: 174.8, lat: -36.8, r: 4 }
  ]

  nightPoints.forEach(p => {
    const x = (p.lng + 180) / 360 * canvas.width
    const y = (90 - p.lat) / 180 * canvas.height
    const gradient = ctx.createRadialGradient(x, y, 0, x, y, p.r * 5)
    gradient.addColorStop(0, 'rgba(255,220,150,0.8)')
    gradient.addColorStop(0.4, 'rgba(255,180,80,0.4)')
    gradient.addColorStop(1, 'rgba(0,0,0,0)')
    ctx.fillStyle = gradient
    ctx.fillRect(x - p.r * 5, y - p.r * 5, p.r * 10, p.r * 10)
  })

  const nightTex = new THREE.CanvasTexture(canvas)
  nightTex.colorSpace = THREE.SRGBColorSpace
  nightMesh.material.map = nightTex
  nightMesh.material.opacity = 0.4
  nightMesh.material.needsUpdate = true
}

function latLonToVector3(lat, lon, radius) {
  const phi = (90 - lat) * (Math.PI / 180)
  const theta = (lon + 180) * (Math.PI / 180)
  const x = -(radius * Math.sin(phi) * Math.cos(theta))
  const z = radius * Math.sin(phi) * Math.sin(theta)
  const y = radius * Math.cos(phi)
  return new THREE.Vector3(x, y, z)
}

function createCityMarker(city) {
  const pos = latLonToVector3(city.lat, city.lon, 8.05)

  // 柱状标记
  const markerGeometry = new THREE.CylinderGeometry(0.06, 0.06, 0.8, 12)
  const markerMaterial = new THREE.MeshPhongMaterial({
    color: 0xff1744,
    emissive: 0xb71c1c,
    emissiveIntensity: 0.6
  })
  const marker = new THREE.Mesh(markerGeometry, markerMaterial)
  marker.position.copy(pos.clone().normalize().multiplyScalar(8.3))
  marker.lookAt(new THREE.Vector3(0, 0, 0))
  marker.rotateX(-Math.PI / 2)
  marker.userData = { city: city }
  earthGroup.add(marker)
  cityMeshes.push(marker)
  cityMarkers.push(marker)

  // 顶部发光点
  const dotGeometry = new THREE.SphereGeometry(0.15, 16, 16)
  const dotMaterial = new THREE.MeshPhongMaterial({
    color: 0xff1744,
    emissive: 0xff5252,
    emissiveIntensity: 0.8,
    specular: 0xffffff,
    shininess: 80
  })
  const dot = new THREE.Mesh(dotGeometry, dotMaterial)
  const dotPos = pos.clone().normalize().multiplyScalar(8.7)
  dot.position.copy(dotPos)
  dot.userData = { city: city, isDot: true }
  earthGroup.add(dot)
  cityMeshes.push(dot)

  // 城市名称标签
  const labelDiv = document.createElement('div')
  labelDiv.className = 'city-label'
  labelDiv.textContent = city.name
  const label = new CSS2DObject(labelDiv)
  const labelPos = pos.clone().normalize().multiplyScalar(9.2)
  label.position.copy(labelPos)
  label.userData = { city: city }
  earthGroup.add(label)
  cityLabels.push(label)
}

// 创建地图表面地名标注（天地图风格）
function createRegionLabel(labelData, index) {
  const div = document.createElement('div')
  div.className = 'region-label'
  div.textContent = labelData.name
  if (labelData.fontWeight === 'bold') div.style.fontWeight = 'bold'
  div.style.fontSize = (labelData.fontSize || 9) + 'px'
  div.style.color = labelData.color || 'rgba(255,255,255,0.22)'
  // 海洋标签用蓝色调
  if (labelData.name.includes('洋') || labelData.name.includes('海')) {
    div.style.color = 'rgba(120,200,255,0.22)'
  }

  const label = new CSS2DObject(div)
  const pos = latLonToVector3(labelData.lat, labelData.lon, 8.03)
  label.position.copy(pos)
  label.userData = { regionLabel: true, index: index }
  earthGroup.add(label)
  regionLabels.push(label)
}

function createRoute(from, to) {
  const v1 = latLonToVector3(from.lat, from.lon, 8.1)
  const v2 = latLonToVector3(to.lat, to.lon, 8.1)

  const distance = v1.distanceTo(v2)
  const mid = v1.clone().add(v2).multiplyScalar(0.5).normalize().multiplyScalar(8.1 + distance * 0.35)

  const curve = new THREE.QuadraticBezierCurve3(v1, mid, v2)
  const points = curve.getPoints(80)
  const geometry = new THREE.BufferGeometry().setFromPoints(points)
  const material = new THREE.LineBasicMaterial({
    color: 0x00e5ff,
    transparent: true,
    opacity: 0.25
  })
  const line = new THREE.Line(geometry, material)
  line.userData = { from: from.name, to: to.name }
  earthGroup.add(line)
  allRoutes.push({ line, curve, from, to })

  // 流动粒子（每条线 3 个粒子）
  for (let i = 0; i < 3; i++) {
    const dotGeometry = new THREE.SphereGeometry(0.07, 8, 8)
    const dotMaterial = new THREE.MeshBasicMaterial({
      color: i === 0 ? 0xffffff : (i === 1 ? 0x00e5ff : 0x7c4dff)
    })
    const flowDot = new THREE.Mesh(dotGeometry, dotMaterial)
    flowDot.userData = { curve: curve, t: (i / 3 + Math.random() * 0.1), speed: 0.002 + Math.random() * 0.003 }
    earthGroup.add(flowDot)
    earthGroup.userData.flowDots.push(flowDot)
    allFlowDots.push(flowDot)
  }
}

function animate() {
  animationId = requestAnimationFrame(animate)

  const time = Date.now() * 0.001

  // 自转
  if (earthGroup && isAutoRotating.value) {
    earthGroup.rotation.y += 0.0008
    if (cloudMesh) cloudMesh.rotation.y += 0.001
  }

  // 流光线粒子动画
  if (allFlowDots.length) {
    allFlowDots.forEach(dot => {
      dot.userData.t += dot.userData.speed
      if (dot.userData.t > 1) dot.userData.t -= 1
      const point = dot.userData.curve.getPoint(dot.userData.t)
      dot.position.copy(point)
      // 粒子大小脉动
      const scale = 0.6 + 0.4 * Math.sin(time * 8 + dot.userData.t * 20)
      dot.scale.setScalar(scale)
    })
  }

  // 城市标记点闪烁
  cityMarkers.forEach((marker, idx) => {
    const intensity = 0.5 + Math.sin(time * 2 + idx) * 0.3
    marker.material.emissiveIntensity = intensity
  })

  // 选中城市高亮脉冲
  if (drillDownCity.value) {
    const selectedMarkers = cityMeshes.filter(m =>
      m.userData.city && m.userData.city.name === drillDownCity.value.name
    )
    selectedMarkers.forEach(m => {
      m.material.color && m.material.color.setHex(0xffd600)
      m.material.emissive && m.material.emissive.setHex(
        Math.sin(time * 5) > 0 ? 0xff6d00 : 0xffab00
      )
    })
  }

  // 下钻相机动画
  if (zoomAnimating) {
    const elapsed = Date.now() - zoomAnimStart
    const t = Math.min(elapsed / zoomAnimDuration, 1.0)
    // easeInOutCubic
    const eased = t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
    camera.position.lerpVectors(zoomAnimFrom, zoomAnimTo, eased)
    controls.target.lerpVectors(zoomTargetFrom, zoomTargetTo, eased)
    if (t >= 1) {
      zoomAnimating = false
      camera.position.copy(zoomAnimTo)
      controls.target.copy(zoomTargetTo)
    }
  }

  controls.update()
  renderer.render(scene, camera)
  labelRenderer.render(scene, camera)
}

function onMouseMove(event) {
  const container = threeContainer.value
  if (!container || !raycaster || !earthMesh || zoomAnimating) return

  const rect = container.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(mouse, camera)

  // 优先检测城市标记
  const cityIntersects = raycaster.intersectObjects(cityMeshes, false)
  if (cityIntersects.length > 0) {
    const obj = cityIntersects[0].object
    if (obj.userData.city) {
      showCityTooltip(obj.userData.city, event, rect)
      return
    }
  }

  // 悬浮空白球面时，就近匹配最近城市
  const earthIntersects = raycaster.intersectObject(earthMesh, false)
  if (earthIntersects.length > 0) {
    const point = earthIntersects[0].point
    const nearest = findNearestCity(point)
    if (nearest) {
      showCityTooltip(nearest, event, rect, true)
      return
    }
  }

  // 无命中时清除
  hoveredCity.value = null
  container.style.cursor = 'grab'
  resetCityColors()
}

function showCityTooltip(city, event, rect, isNearest = false) {
  const container = threeContainer.value
  hoveredCity.value = city
  tooltipStyle.value = {
    left: event.clientX - rect.left + 20 + 'px',
    top: event.clientY - rect.top - 20 + 'px'
  }
  container.style.cursor = 'pointer'
  cityMeshes.forEach(m => {
    if (!m.userData.city) return
    if (m.userData.city.name === city.name) {
      m.material.color && m.material.color.setHex(0xffd600)
      m.material.emissive && m.material.emissive.setHex(0xff6d00)
    } else {
      m.material.color && m.material.color.setHex(0xff1744)
      m.material.emissive && m.material.emissive.setHex(0xb71c1c)
    }
  })
}

function resetCityColors() {
  cityMeshes.forEach(m => {
    if (m.userData.city) {
      m.material.color && m.material.color.setHex(0xff1744)
      m.material.emissive && m.material.emissive.setHex(0xb71c1c)
    }
  })
}

function findNearestCity(point) {
  let nearest = null
  let minDistance = Infinity
  const threshold = 3.5
  citiesData.forEach(city => {
    const cityPos = latLonToVector3(city.lat, city.lon, 8)
    const dist = point.distanceTo(cityPos)
    if (dist < minDistance && dist < threshold) {
      minDistance = dist
      nearest = city
    }
  })
  return nearest
}

// ===================== 点击下钻 =====================
function onMouseClick(event) {
  if (zoomAnimating || !hoveredCity.value) return

  if (drillDownCity.value && drillDownCity.value.name === hoveredCity.value.name) {
    // 再次点击同一城市，返回全局
    backToGlobal()
    return
  }

  if (!drillDownCity.value) {
    // 进入下钻模式
    zoomToCity(hoveredCity.value)
  } else {
    // 下钻到另一个城市
    zoomToCity(hoveredCity.value)
  }
}

function zoomToCity(city) {
  // 暂停自动旋转
  isAutoRotating.value = false
  if (controls) controls.autoRotate = false

  drillDownCity.value = city
  isDrilling.value = true

  // 目标：相机聚焦该城市
  const cityPos = latLonToVector3(city.lat, city.lon, 8)

  // 计算城市在世界坐标中的位置（考虑 earthGroup 旋转）
  const worldPos = cityPos.clone()
  if (earthGroup) {
    worldPos.applyMatrix4(earthGroup.matrixWorld)
  }

  // 相机目标：城市位置
  zoomTargetTo = worldPos.clone()
  // 相机位置：城市上方偏外
  const direction = cityPos.clone().normalize()
  zoomAnimTo = direction.clone().multiplyScalar(18)

  zoomAnimFrom = camera.position.clone()
  zoomTargetFrom = controls.target.clone()
  zoomAnimStart = Date.now()
  zoomAnimDuration = 1500
  zoomAnimating = true
}

function backToGlobal() {
  zoomAnimFrom = camera.position.clone()
  zoomTargetFrom = controls.target.clone()
  zoomAnimTo = originalCameraPos.clone()
  zoomTargetTo = originalControlsTarget.clone()
  zoomAnimStart = Date.now()
  zoomAnimDuration = 1200
  zoomAnimating = true

  drillDownCity.value = null
  isDrilling.value = false
  isAutoRotating.value = true
  if (controls) controls.autoRotate = true

  // 恢复所有城市标记颜色
  resetCityColors()
}

function onSceneResize() {
  const container = threeContainer.value
  if (!container || !camera || !renderer) return
  const width = container.clientWidth
  const height = container.clientHeight
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
  labelRenderer.setSize(width, height)
}

function destroyThreeScene() {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
  if (renderer) {
    renderer.domElement.removeEventListener('mousemove', onMouseMove)
    renderer.domElement.removeEventListener('click', onMouseClick)
    window.removeEventListener('resize', onSceneResize)
    renderer.dispose()
    if (renderer.domElement.parentElement) {
      renderer.domElement.parentElement.removeChild(renderer.domElement)
    }
  }
  if (labelRenderer) {
    if (labelRenderer.domElement.parentElement) {
      labelRenderer.domElement.parentElement.removeChild(labelRenderer.domElement)
    }
    labelRenderer = null
  }
  if (controls) controls.dispose()
  if (scene) scene.clear()
  cityMeshes = []
  cityLabels = []
  regionLabels.length = 0
  allRoutes = []
  allFlowDots = []
  threeReady.value = false
}

// ===================== 全屏 / 时钟 =====================
function toggleRotation() {
  isAutoRotating.value = !isAutoRotating.value
  if (controls) {
    controls.autoRotate = isAutoRotating.value
  }
}

function toggleFullscreen() {
  if (!isFullscreen.value) {
    const el = screenRef.value || document.documentElement
    if (el.requestFullscreen) el.requestFullscreen()
  } else {
    if (document.exitFullscreen) document.exitFullscreen()
  }
}

function onFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
}

function updateTime() {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

onMounted(async () => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
  document.addEventListener('fullscreenchange', onFullscreenChange)
  document.addEventListener('webkitfullscreenchange', onFullscreenChange)
  await nextTick()
  initThreeScene()
})

onBeforeUnmount(() => {
  clearInterval(timeTimer)
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  document.removeEventListener('webkitfullscreenchange', onFullscreenChange)
  destroyThreeScene()
})
</script>

<style scoped>
/* ========== 基础 ========== */
.shipping-screen {
  margin: -16px;
  height: calc(100vh - 50px - 34px);
  min-height: 700px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: radial-gradient(ellipse at center, #0a1220 0%, #05080f 100%);
  color: #e0e0e0;
  font-family: 'Helvetica Neue', 'Microsoft YaHei', sans-serif;
  position: relative;
}

.shipping-screen:fullscreen,
.shipping-screen:-webkit-full-screen {
  height: 100vh;
  margin: 0;
}

/* ========== 顶部标题栏 ========== */
.screen-header {
  height: 58px;
  min-height: 58px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: linear-gradient(180deg, rgba(10, 25, 55, 0.95) 0%, rgba(10, 20, 45, 0.8) 100%);
  border-bottom: 1px solid rgba(0, 229, 255, 0.15);
  position: relative;
  z-index: 10;
}

.screen-header::after {
  content: '';
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 229, 255, 0.4), transparent);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-decoration {
  width: 4px;
  height: 22px;
  background: linear-gradient(180deg, #00e5ff, #0066cc);
  border-radius: 2px;
  box-shadow: 0 0 10px rgba(0, 229, 255, 0.5);
}

.header-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #e0f7fa, #00e5ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-time {
  font-size: 15px;
  font-family: 'Consolas', 'Courier New', monospace;
  color: #00e5ff;
  letter-spacing: 1px;
  text-shadow: 0 0 10px rgba(0, 229, 255, 0.3);
}

.fullscreen-btn {
  background: rgba(0, 229, 255, 0.08);
  border: 1px solid rgba(0, 229, 255, 0.25);
  color: #00e5ff;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.fullscreen-btn:hover {
  background: rgba(0, 229, 255, 0.2);
  box-shadow: 0 0 12px rgba(0, 229, 255, 0.3);
}

/* ========== 主体三栏 ========== */
.screen-body {
  flex: 1;
  display: grid;
  grid-template-columns: 320px 1fr 320px;
  gap: 18px;
  padding: 14px;
  min-height: 0;
  overflow: hidden;
}

.left-panel,
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 0;
  overflow: hidden;
}

.left-panel > :nth-child(1) { flex: 0 0 110px; }
.left-panel > :nth-child(2) { flex: 1.1; }
.left-panel > :nth-child(3) { flex: 1; }
.right-panel > :nth-child(1) { flex: 1; }
.right-panel > :nth-child(2) { flex: 1.1; }
.right-panel > :nth-child(3) { flex: 1; }

/* ========== 面板盒子 ========== */
.panel-box {
  background: rgba(10, 18, 48, 0.7);
  border: 1px solid rgba(0, 229, 255, 0.12);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  position: relative;
}

.panel-box::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  border-radius: 6px;
  border: 1px solid transparent;
  background: linear-gradient(135deg, rgba(0,229,255,0.1), transparent 40%, transparent 60%, rgba(0,229,255,0.05)) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}

.panel-header {
  height: 36px;
  min-height: 36px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  border-bottom: 1px solid rgba(0, 229, 255, 0.08);
  font-size: 13px;
  font-weight: 600;
  color: #b0c8e0;
}

.panel-header .header-icon {
  font-size: 15px;
}

.panel-body {
  flex: 1;
  padding: 10px;
  min-height: 0;
  overflow: hidden;
}

/* ========== 实时指标 ========== */
.ship-metrics {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  height: 100%;
}

.metric-card {
  background: rgba(0, 229, 255, 0.05);
  border: 1px solid rgba(0, 229, 255, 0.08);
  border-radius: 6px;
  padding: 6px 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  gap: 1px;
}

.metric-value {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
}

.metric-label {
  font-size: 10px;
  color: #78909c;
}

.metric-trend {
  font-size: 9px;
  margin-top: 1px;
  font-family: 'Consolas', monospace;
}

.metric-trend.up { color: #00e676; }
.metric-trend.down { color: #ff5252; }

/* ========== 中间地球 ========== */
.center-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  overflow: hidden;
}

.three-container {
  flex: 1;
  position: relative;
  border: 1px solid rgba(0, 229, 255, 0.12);
  border-radius: 6px;
  overflow: hidden;
  background: radial-gradient(ellipse at center, #0a1830 0%, #050810 100%);
  min-height: 320px;
}

.three-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00e5ff;
  font-size: 15px;
  letter-spacing: 2px;
  z-index: 2;
}

.rotation-toggle {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid rgba(0, 229, 255, 0.35);
  background: rgba(5, 10, 30, 0.75);
  color: #00e5ff;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(4px);
}

.rotation-toggle:hover {
  background: rgba(0, 229, 255, 0.2);
  box-shadow: 0 0 12px rgba(0, 229, 255, 0.3);
}

/* ========== 悬浮提示 ========== */
.earth-tooltip {
  position: absolute;
  z-index: 20;
  background: rgba(5, 10, 30, 0.95);
  border: 1px solid rgba(0, 229, 255, 0.35);
  border-radius: 6px;
  padding: 10px 14px;
  min-width: 180px;
  pointer-events: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5), 0 0 15px rgba(0, 229, 255, 0.1);
}

.tooltip-title {
  font-size: 14px;
  font-weight: 700;
  color: #00e5ff;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid rgba(0, 229, 255, 0.15);
}

.tooltip-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  padding: 3px 0;
  color: #90a4ae;
}

.tooltip-row span:last-child { color: #e0e0e0; }

/* ========== 下钻详情面板 ========== */
.drill-panel {
  position: absolute;
  top: 60px;
  right: 16px;
  z-index: 25;
  width: 300px;
  max-height: calc(100% - 80px);
  background: rgba(5, 10, 30, 0.96);
  border: 1px solid rgba(0, 229, 255, 0.3);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.6), 0 0 25px rgba(0, 229, 255, 0.12);
  backdrop-filter: blur(12px);
}

.drill-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(0, 229, 255, 0.15);
  background: rgba(0, 229, 255, 0.06);
}

.drill-title {
  font-size: 14px;
  font-weight: 700;
  color: #00e5ff;
  letter-spacing: 1px;
}

.drill-close {
  background: none;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #90a4ae;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.drill-close:hover {
  color: #ff5252;
  border-color: #ff5252;
  background: rgba(255, 82, 82, 0.1);
}

.drill-body {
  padding: 14px 16px;
  overflow-y: auto;
  max-height: calc(100vh - 300px);
}

.drill-body::-webkit-scrollbar { width: 4px; }
.drill-body::-webkit-scrollbar-thumb { background: rgba(0,229,255,0.2); border-radius: 2px; }

.drill-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.drill-info-item {
  background: rgba(0, 229, 255, 0.04);
  border: 1px solid rgba(0, 229, 255, 0.08);
  border-radius: 4px;
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.drill-label {
  font-size: 10px;
  color: #78909c;
}

.drill-value {
  font-size: 13px;
  font-weight: 600;
  color: #e0e0e0;
}

.drill-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #b0bec5;
  margin: 14px 0 8px;
  padding-left: 8px;
  border-left: 2px solid #00e5ff;
}

.drill-nearby {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.drill-nearby-item {
  font-size: 11px;
  color: #90a4ae;
  background: rgba(0, 229, 255, 0.05);
  border: 1px solid rgba(0, 229, 255, 0.1);
  border-radius: 4px;
  padding: 4px 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.nearby-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #00e5ff;
}

.drill-routes {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.drill-route-tag {
  font-size: 10px;
  color: #b0bec5;
  background: rgba(124, 77, 255, 0.15);
  border: 1px solid rgba(124, 77, 255, 0.25);
  border-radius: 3px;
  padding: 3px 8px;
}

/* drill 过渡动画 */
.drill-fade-enter-active { transition: all 0.3s ease-out; }
.drill-fade-leave-active { transition: all 0.2s ease-in; }
.drill-fade-enter-from,
.drill-fade-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* ========== 底部统计条 ========== */
.center-bottom-bar {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  height: 70px;
  min-height: 70px;
}

.bottom-stat {
  background: rgba(10, 18, 48, 0.7);
  border: 1px solid rgba(0, 229, 255, 0.1);
  border-radius: 6px;
  padding: 6px 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  overflow: hidden;
}

.bottom-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.bottom-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.bottom-title {
  font-size: 10px;
  color: #78909c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bottom-value {
  font-size: 14px;
  font-weight: 700;
  margin: 1px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bottom-sub {
  font-size: 9px;
  color: #546e7a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ========== 热门航线表格 ========== */
.route-table {
  display: flex;
  flex-direction: column;
  gap: 6px;
  height: 100%;
  overflow-y: auto;
}

.route-header,
.route-row {
  display: grid;
  grid-template-columns: 40px 1fr 50px 60px;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  padding: 6px 8px;
  border-radius: 4px;
}

.route-header {
  color: #78909c;
  background: rgba(0, 229, 255, 0.05);
  font-weight: 600;
}

.route-row {
  color: #b0bec5;
  background: rgba(10, 18, 48, 0.5);
  transition: background 0.3s;
}

.route-row:hover { background: rgba(10, 18, 48, 0.9); }

.route-rank {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  background: #546e7a;
}

.route-rank.rank-1 { background: linear-gradient(135deg, #ffd600, #ffab00); }
.route-rank.rank-2 { background: linear-gradient(135deg, #c0c0c0, #78909c); }
.route-rank.rank-3 { background: linear-gradient(135deg, #cd7f32, #8d6e63); }

.route-name { font-size: 11px; }
.route-count { color: #00e5ff; text-align: right; }
.route-rate { text-align: right; font-weight: 600; }

/* ========== 仪表盘布局 ========== */
.gauge-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 6px;
}

.gauge-wrap { min-height: 0; }

/* ========== 告警列表 ========== */
.alert-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  height: 100%;
}

.alert-list::-webkit-scrollbar { width: 4px; }
.alert-list::-webkit-scrollbar-thumb { background: rgba(0,229,255,0.2); border-radius: 2px; }

.alert-item {
  background: rgba(10, 18, 48, 0.5);
  border-left: 3px solid;
  border-radius: 0 4px 4px 0;
  padding: 10px 12px;
  transition: background 0.3s;
}

.alert-item:hover { background: rgba(10, 18, 48, 0.8); }

.alert-item.alert-error { border-left-color: #ff1744; }
.alert-item.alert-warning { border-left-color: #ffab00; }
.alert-item.alert-info { border-left-color: #448aff; }

.alert-time {
  font-size: 11px;
  color: #546e7a;
  margin-bottom: 5px;
  font-family: 'Consolas', monospace;
}

.alert-content {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.alert-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 2px;
  white-space: nowrap;
  flex-shrink: 0;
  margin-top: 1px;
}

.alert-tag.tag-error { background: rgba(255, 23, 68, 0.2); color: #ff1744; }
.alert-tag.tag-warning { background: rgba(255, 171, 0, 0.2); color: #ffab00; }
.alert-tag.tag-info { background: rgba(68, 138, 255, 0.2); color: #448aff; }

.alert-msg {
  font-size: 12px;
  color: #b0bec5;
  line-height: 1.4;
}

/* ========== 响应式适配 ========== */
@media (max-width: 1600px) {
  .screen-body {
    grid-template-columns: 280px 1fr 280px;
  }
  .center-bottom-bar {
    grid-template-columns: repeat(3, 1fr);
    height: 70px;
  }
}

@media (max-width: 1200px) {
  .screen-body {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr auto;
    overflow-y: auto;
  }
  .left-panel, .right-panel {
    flex-direction: row;
    gap: 12px;
  }
  .left-panel > *, .right-panel > * {
    flex: 1;
    min-width: 0;
  }
  .center-bottom-bar {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

<style>
/* 3D 地球城市名称标签 */
.city-label {
  color: #00e5ff;
  font-size: 11px;
  font-weight: 600;
  text-shadow: 0 0 6px rgba(0, 229, 255, 0.8), 0 0 12px rgba(0, 170, 255, 0.5);
  white-space: nowrap;
  pointer-events: none;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  letter-spacing: 1px;
  transform: translateY(-12px);
}

/* 地图表面地名标注（天地图风格） */
.region-label {
  color: rgba(255, 255, 255, 0.22);
  font-size: 9px;
  font-weight: normal;
  white-space: nowrap;
  pointer-events: none;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  letter-spacing: 1px;
  text-shadow: none;
  opacity: 0.7;
}
</style>
