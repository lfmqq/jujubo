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
          <div class="earth-tooltip" v-if="hoveredCity" :style="tooltipStyle">
            <div class="tooltip-title">{{ hoveredCity.name }}</div>
            <div class="tooltip-row"><span>类型</span><span>{{ hoveredCity.type }}</span></div>
            <div class="tooltip-row"><span>进出港</span><span>{{ hoveredCity.inout }}</span></div>
            <div class="tooltip-row"><span>准点率</span><span>{{ hoveredCity.punctuality }}%</span></div>
          </div>
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

let scene, camera, renderer, labelRenderer, controls, animationId, earthGroup
let raycaster, mouse
let cityMeshes = []
let cityLabels = []
const cityMarkers = []

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

// ===================== Three.js 地球场景 =====================
function initThreeScene() {
  const container = threeContainer.value
  if (!container) return

  const width = container.clientWidth
  const height = container.clientHeight

  scene = new THREE.Scene()
  scene.fog = new THREE.FogExp2(0x05080f, 0.002)

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
  labelRenderer.domElement.style.pointerEvents = 'none' // 不阻挡鼠标事件
  container.appendChild(labelRenderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.minDistance = 15
  controls.maxDistance = 50
  controls.autoRotate = true
  controls.autoRotateSpeed = 0.6
  controls.enablePan = false

  // 灯光
  const ambientLight = new THREE.AmbientLight(0x404040, 2.5)
  scene.add(ambientLight)

  const sunLight = new THREE.DirectionalLight(0xffffff, 2)
  sunLight.position.set(20, 10, 20)
  scene.add(sunLight)

  const blueLight = new THREE.DirectionalLight(0x00aaff, 1.2)
  blueLight.position.set(-20, -5, -10)
  scene.add(blueLight)

  // 星空
  createStars()

  earthGroup = new THREE.Group()
  scene.add(earthGroup)

  // 地球 - 先占位材质，异步加载地图纹理
  const earthGeometry = new THREE.SphereGeometry(8, 64, 64)
  const earthMaterial = new THREE.MeshPhongMaterial({
    color: 0x0a2a4a,
    emissive: 0x051020,
    specular: 0x222222,
    shininess: 15,
    transparent: true,
    opacity: 0.95
  })
  const earth = new THREE.Mesh(earthGeometry, earthMaterial)
  earthGroup.add(earth)

  // 加载地图纹理
  const textureLoader = new THREE.TextureLoader()
  const mapUrls = [
    'https://threejs.org/examples/textures/planets/earth_atmos_2048.jpg',
    'https://unpkg.com/three-globe/example/img/earth-blue-marble.jpg'
  ]
  let urlIndex = 0
  function tryLoadTexture() {
    if (urlIndex >= mapUrls.length) {
      console.warn('所有地图纹理源加载失败，使用纯色地球')
      return
    }
    textureLoader.load(
      mapUrls[urlIndex],
      (texture) => {
        texture.colorSpace = THREE.SRGBColorSpace
        earth.material.map = texture
        earth.material.emissiveMap = texture
        earth.material.color.set(0xffffff)
        earth.material.emissive.set(0x112233)
        earth.material.emissiveIntensity = 0.35
        earth.material.needsUpdate = true
      },
      undefined,
      () => {
        urlIndex++
        tryLoadTexture()
      }
    )
  }
  tryLoadTexture()

  // 地球经纬线
  const gridHelper = new THREE.PolarGridHelper(8.05, 16, 8, 64, 0x00aaff, 0x004488)
  gridHelper.rotation.x = Math.PI / 2
  earthGroup.add(gridHelper)

  // 经纬线
  const wireGeometry = new THREE.WireframeGeometry(new THREE.SphereGeometry(8.08, 24, 24))
  const wireMaterial = new THREE.LineBasicMaterial({ color: 0x1a5a8a, transparent: true, opacity: 0.15 })
  const wireframe = new THREE.LineSegments(wireGeometry, wireMaterial)
  earthGroup.add(wireframe)

  // 大气层光晕
  const atmosGeometry = new THREE.SphereGeometry(8.5, 64, 64)
  const atmosMaterial = new THREE.MeshBasicMaterial({
    color: 0x00aaff,
    transparent: true,
    opacity: 0.08,
    side: THREE.BackSide
  })
  const atmosphere = new THREE.Mesh(atmosGeometry, atmosMaterial)
  earthGroup.add(atmosphere)

  // 外圈光晕
  const glowGeometry = new THREE.SphereGeometry(9.2, 64, 64)
  const glowMaterial = new THREE.MeshBasicMaterial({
    color: 0x0066ff,
    transparent: true,
    opacity: 0.03,
    side: THREE.BackSide
  })
  const glow = new THREE.Mesh(glowGeometry, glowMaterial)
  earthGroup.add(glow)

  // 城市与航线
  const cities = [
    { name: '上海', type: '港口', inout: '进 86 / 出 92', punctuality: 94.2, lat: 31.23, lon: 121.47 },
    { name: '新加坡', type: '枢纽港', inout: '进 120 / 出 115', punctuality: 93.8, lat: 1.35, lon: 103.82 },
    { name: '洛杉矶', type: '港口', inout: '进 68 / 出 72', punctuality: 89.5, lat: 34.05, lon: -118.25 },
    { name: '鹿特丹', type: '港口', inout: '进 45 / 出 48', punctuality: 91.2, lat: 51.92, lon: 4.48 },
    { name: '悉尼', type: '港口', inout: '进 32 / 出 35', punctuality: 90.1, lat: -33.87, lon: 151.21 },
    { name: '迪拜', type: '中转港', inout: '进 56 / 出 58', punctuality: 92.4, lat: 25.20, lon: 55.27 },
    { name: '釜山', type: '港口', inout: '进 42 / 出 40', punctuality: 93.1, lat: 35.18, lon: 129.08 },
    { name: '汉堡', type: '港口', inout: '进 38 / 出 36', punctuality: 88.7, lat: 53.55, lon: 9.99 },
    { name: '香港', type: '枢纽港', inout: '进 78 / 出 80', punctuality: 95.3, lat: 22.32, lon: 114.17 },
    { name: '东京', type: '港口', inout: '进 48 / 出 50', punctuality: 91.8, lat: 35.68, lon: 139.69 }
  ]

  cities.forEach(city => {
    createCityMarker(city)
  })

  const routes = [
    ['上海', '洛杉矶'], ['上海', '鹿特丹'], ['深圳', '新加坡'],
    ['新加坡', '迪拜'], ['迪拜', '汉堡'], ['洛杉矶', '悉尼'],
    ['釜山', '东京'], ['香港', '新加坡'], ['上海', '釜山'],
    ['鹿特丹', '汉堡'], ['新加坡', '悉尼'], ['洛杉矶', '香港']
  ]

  routes.forEach(([from, to]) => {
    const fromCity = cities.find(c => c.name === from)
    const toCity = cities.find(c => c.name === to)
    if (fromCity && toCity) createRoute(fromCity, toCity)
  })

  // 射线交互
  raycaster = new THREE.Raycaster()
  mouse = new THREE.Vector2()
  renderer.domElement.addEventListener('mousemove', onMouseMove)
  renderer.domElement.addEventListener('click', onMouseClick)
  window.addEventListener('resize', onSceneResize)

  threeReady.value = true
  animate()
}

function createStars() {
  const starGeometry = new THREE.BufferGeometry()
  const starCount = 2000
  const positions = new Float32Array(starCount * 3)
  for (let i = 0; i < starCount * 3; i++) {
    positions[i] = (Math.random() - 0.5) * 200
  }
  starGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  const starMaterial = new THREE.PointsMaterial({
    color: 0xffffff,
    size: 0.15,
    transparent: true,
    opacity: 0.8
  })
  const stars = new THREE.Points(starGeometry, starMaterial)
  scene.add(stars)
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
    color: 0x00e5ff,
    emissive: 0x00aaff,
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
    color: 0x00e5ff,
    emissive: 0x00e5ff,
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

function createRoute(from, to) {
  const v1 = latLonToVector3(from.lat, from.lon, 8.1)
  const v2 = latLonToVector3(to.lat, to.lon, 8.1)

  const distance = v1.distanceTo(v2)
  const mid = v1.clone().add(v2).multiplyScalar(0.5).normalize().multiplyScalar(8.1 + distance * 0.35)

  const curve = new THREE.QuadraticBezierCurve3(v1, mid, v2)
  const points = curve.getPoints(60)
  const geometry = new THREE.BufferGeometry().setFromPoints(points)
  const material = new THREE.LineBasicMaterial({
    color: 0x00e5ff,
    transparent: true,
    opacity: 0.35
  })
  const line = new THREE.Line(geometry, material)
  earthGroup.add(line)

  // 流动点
  const dotGeometry = new THREE.SphereGeometry(0.08, 12, 12)
  const dotMaterial = new THREE.MeshBasicMaterial({ color: 0xffffff })
  const flowDot = new THREE.Mesh(dotGeometry, dotMaterial)
  flowDot.userData = { curve: curve, t: Math.random() }
  earthGroup.add(flowDot)

  if (!earthGroup.userData.flowDots) earthGroup.userData.flowDots = []
  earthGroup.userData.flowDots.push(flowDot)
}

function animate() {
  animationId = requestAnimationFrame(animate)

  const time = Date.now() * 0.001

  if (earthGroup) {
    earthGroup.rotation.y += 0.0008
  }

  // 流动点动画
  if (earthGroup && earthGroup.userData.flowDots) {
    earthGroup.userData.flowDots.forEach(dot => {
      dot.userData.t += 0.003
      if (dot.userData.t > 1) dot.userData.t = 0
      const point = dot.userData.curve.getPoint(dot.userData.t)
      dot.position.copy(point)
    })
  }

  // 城市标记点闪烁
  cityMarkers.forEach((marker, idx) => {
    const intensity = 0.5 + Math.sin(time * 2 + idx) * 0.3
    marker.material.emissiveIntensity = intensity
  })

  controls.update()
  renderer.render(scene, camera)
  labelRenderer.render(scene, camera)
}

function onMouseMove(event) {
  const container = threeContainer.value
  if (!container || !raycaster) return

  const rect = container.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(mouse, camera)
  const intersects = raycaster.intersectObjects(cityMeshes, false)

  if (intersects.length > 0) {
    const obj = intersects[0].object
    if (obj.userData.city) {
      hoveredCity.value = obj.userData.city
      tooltipStyle.value = {
        left: event.clientX - rect.left + 20 + 'px',
        top: event.clientY - rect.top - 20 + 'px'
      }
      container.style.cursor = 'pointer'
      // 高亮
      cityMeshes.forEach(m => {
        if (m.userData.city && m.userData.city.name !== obj.userData.city.name) {
          m.material.color && m.material.color.setHex(0x00aaff)
          m.material.emissive && m.material.emissive.setHex(0x004488)
        }
      })
      obj.material.color && obj.material.color.setHex(0xffd600)
      obj.material.emissive && obj.material.emissive.setHex(0xff6d00)
    }
  } else {
    hoveredCity.value = null
    container.style.cursor = 'grab'
    cityMeshes.forEach(m => {
      if (m.userData.city) {
        m.material.color && m.material.color.setHex(0x00e5ff)
        m.material.emissive && m.material.emissive.setHex(0x00aaff)
      }
    })
  }
}

function onMouseClick() {
  if (hoveredCity.value) {
    console.log('选中城市:', hoveredCity.value)
  }
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
  threeReady.value = false
}

// ===================== 全屏 / 时钟 =====================
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
/* 3D 地球城市名称标签（CSS2DRenderer 生成的 DOM 在组件树外部，需要全局样式） */
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
</style>
