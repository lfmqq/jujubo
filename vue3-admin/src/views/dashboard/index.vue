<template>
  <div class="dashboard-screen" ref="screenRef">
    <!-- ========== 顶部标题栏 ========== -->
    <header class="screen-header">
      <div class="header-left">
        <div class="header-decoration"></div>
        <h1 class="header-title">智慧机房可视化监控大屏</h1>
      </div>
      <div class="header-center">
        <div class="status-indicators">
          <span class="indicator online">
            <i class="dot"></i>在线设备 {{ onlineCount }}
          </span>
          <span class="indicator warning">
            <i class="dot"></i>告警 {{ warningCount }}
          </span>
          <span class="indicator offline">
            <i class="dot"></i>离线 {{ offlineCount }}
          </span>
        </div>
      </div>
      <div class="header-right">
        <span class="current-time">{{ currentTime }}</span>
        <button class="fullscreen-btn" @click="toggleFullscreen" :title="isFullscreen ? '退出全屏' : '全屏展示'">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <template v-if="!isFullscreen">
              <path d="M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3m0 18h3a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2h3" />
            </template>
            <template v-else>
              <path d="M8 3v3a2 2 0 0 1-2 2H3m18 0h-3a2 2 0 0 1-2-2V3m0 18v-3a2 2 0 0 1 2-2h3M3 16h3a2 2 0 0 1 2 2v3" />
            </template>
          </svg>
        </button>
      </div>
    </header>

    <!-- ========== 主体三栏布局 ========== -->
    <main class="screen-body">
      <!-- 左侧面板 -->
      <aside class="left-panel">
        <div class="panel-box">
          <div class="panel-header">
            <span class="header-icon trend">📈</span>设备在线趋势
          </div>
          <div class="panel-body">
            <v-chart :option="trendOption" style="height: 100%;" autoresize />
          </div>
        </div>
        <div class="panel-box">
          <div class="panel-header">
            <span class="header-icon pie">🍩</span>区域设备分布
          </div>
          <div class="panel-body">
            <v-chart :option="regionOption" style="height: 100%;" autoresize />
          </div>
        </div>
      </aside>

      <!-- 中间 3D 场景 -->
      <section class="center-panel">
        <div class="center-top-stats">
          <div class="mini-stat" v-for="s in topStats" :key="s.label">
            <div class="mini-stat-value" :style="{ color: s.color }">{{ s.value }}</div>
            <div class="mini-stat-label">{{ s.label }}</div>
          </div>
        </div>
        <div class="three-container" ref="threeContainer">
          <div class="three-loading" v-if="!threeReady">3D 场景加载中...</div>
          <div class="device-tooltip" v-if="hoveredDevice" :style="tooltipStyle">
            <div class="tooltip-title">{{ hoveredDevice.name }}</div>
            <div class="tooltip-row"><span>设备ID</span><span>{{ hoveredDevice.id }}</span></div>
            <div class="tooltip-row"><span>状态</span><span :class="'status-' + hoveredDevice.status">{{ statusMap[hoveredDevice.status] }}</span></div>
            <div class="tooltip-row"><span>CPU负载</span><span>{{ hoveredDevice.load }}%</span></div>
            <div class="tooltip-row"><span>内存使用</span><span>{{ hoveredDevice.memory }}%</span></div>
            <div class="tooltip-row"><span>温度</span><span>{{ hoveredDevice.temp }}°C</span></div>
          </div>
        </div>
        <div class="scene-legend">
          <span class="legend-item"><i style="background:#00e676"></i>正常运行</span>
          <span class="legend-item"><i style="background:#ffab00"></i>告警</span>
          <span class="legend-item"><i style="background:#ff1744"></i>故障</span>
        </div>
      </section>

      <!-- 右侧面板 -->
      <aside class="right-panel">
        <div class="panel-box">
          <div class="panel-header">
            <span class="header-icon bar">📊</span>设备能耗排行 (kW·h)
          </div>
          <div class="panel-body">
            <v-chart :option="powerOption" style="height: 100%;" autoresize />
          </div>
        </div>
        <div class="panel-box">
          <div class="panel-header">
            <span class="header-icon alert">🚨</span>实时告警列表
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
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import * as echarts from 'echarts'
import { use } from 'echarts/core'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent,
  GridComponent, DatasetComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'

use([LineChart, PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, DatasetComponent, CanvasRenderer])

// ===================== 模拟数据 =====================
const statusMap = { normal: '正常运行', warning: '告警', error: '故障' }
const statusColorMap = { normal: '#00e676', warning: '#ffab00', error: '#ff1744' }

const devices = ref([
  { id: 'DEV-001', name: '核心交换机-A1', status: 'normal', load: 32, memory: 45, temp: 38, region: '华东', power: 320, x: -4, z: -4 },
  { id: 'DEV-002', name: '数据库服务器-B2', status: 'normal', load: 65, memory: 72, temp: 45, region: '华东', power: 580, x: 0, z: -4 },
  { id: 'DEV-003', name: '应用服务器-C3', status: 'normal', load: 48, memory: 55, temp: 40, region: '华东', power: 410, x: 4, z: -4 },
  { id: 'DEV-004', name: '存储阵列-D1', status: 'normal', load: 22, memory: 35, temp: 32, region: '华南', power: 750, x: -4, z: 0 },
  { id: 'DEV-005', name: '防火墙-E5', status: 'warning', load: 88, memory: 91, temp: 62, region: '华南', power: 280, x: 0, z: 0 },
  { id: 'DEV-006', name: '负载均衡-F2', status: 'normal', load: 55, memory: 60, temp: 42, region: '华南', power: 350, x: 4, z: 0 },
  { id: 'DEV-007', name: '备份服务器-G1', status: 'error', load: 0, memory: 12, temp: 25, region: '华北', power: 120, x: -4, z: 4 },
  { id: 'DEV-008', name: '缓存节点-H3', status: 'normal', load: 71, memory: 80, temp: 48, region: '华北', power: 420, x: 0, z: 4 },
  { id: 'DEV-009', name: '消息队列-I4', status: 'normal', load: 38, memory: 50, temp: 36, region: '华北', power: 290, x: 4, z: 4 },
  { id: 'DEV-010', name: 'GPU集群-J1', status: 'normal', load: 92, memory: 85, temp: 72, region: '西南', power: 980, x: -2, z: -2 },
  { id: 'DEV-011', name: '日志服务器-K2', status: 'warning', load: 83, memory: 78, temp: 55, region: '西南', power: 260, x: 2, z: -2 },
  { id: 'DEV-012', name: '监控节点-L3', status: 'normal', load: 42, memory: 48, temp: 37, region: '西南', power: 310, x: 0, z: 2 },
])

const onlineCount = computed(() => devices.value.filter(d => d.status === 'normal').length)
const warningCount = computed(() => devices.value.filter(d => d.status === 'warning').length)
const offlineCount = computed(() => devices.value.filter(d => d.status === 'error').length)

const topStats = computed(() => [
  { label: '设备总数', value: devices.value.length, color: '#64b5f6' },
  { label: '在线率', value: Math.round(onlineCount.value / devices.value.length * 100) + '%', color: '#00e676' },
  { label: '平均负载', value: Math.round(devices.value.reduce((s, d) => s + d.load, 0) / devices.value.length) + '%', color: '#ffab00' },
  { label: '总功耗', value: devices.value.reduce((s, d) => s + d.power, 0) + 'W', color: '#ce93d8' },
])

// 告警列表
const alertList = ref([
  { id: 1, time: '14:32:15', level: 'error', levelText: '严重', msg: '备份服务器-G1 离线，请立即检查' },
  { id: 2, time: '14:28:06', level: 'warning', levelText: '警告', msg: '防火墙-E5 CPU使用率超过85%' },
  { id: 3, time: '14:15:42', level: 'warning', levelText: '警告', msg: '日志服务器-K2 内存使用率超过75%' },
  { id: 4, time: '13:58:21', level: 'info', levelText: '信息', msg: 'GPU集群-J1 温度偏高 (72°C)，建议检查散热' },
  { id: 5, time: '13:45:10', level: 'info', levelText: '信息', msg: '核心交换机-A1 完成固件自动升级' },
  { id: 6, time: '13:30:00', level: 'info', levelText: '信息', msg: '存储阵列-D1 磁盘空间使用率达70%' },
])

// 当前时间
const currentTime = ref('')
let timeTimer = null

// ===================== ECharts 配置 =====================
const darkTextColor = '#b0bec5'
const darkAxisColor = '#37474f'

// 折线图：月度设备在线数量
const trendOption = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(10,14,39,0.9)',
    borderColor: '#1e3a5f',
    textStyle: { color: '#e0e0e0', fontSize: 12 }
  },
  legend: {
    right: 0, top: 0,
    textStyle: { color: darkTextColor, fontSize: 11 }
  },
  grid: { left: '8%', right: '5%', top: '18%', bottom: '8%' },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
    axisLine: { lineStyle: { color: darkAxisColor } },
    axisTick: { show: false },
    axisLabel: { color: darkTextColor, fontSize: 11 }
  },
  yAxis: {
    type: 'value', min: 0, max: 14,
    splitLine: { lineStyle: { color: 'rgba(55,71,79,0.3)', type: 'dashed' } },
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: darkTextColor, fontSize: 11 }
  },
  series: [
    {
      name: '在线设备',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: [8, 9, 10, 9, 11, 12, 12],
      lineStyle: { color: '#00e5ff', width: 2 },
      itemStyle: { color: '#00e5ff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0,229,255,0.3)' },
          { offset: 1, color: 'rgba(0,229,255,0.02)' }
        ])
      }
    },
    {
      name: '离线设备',
      type: 'line',
      smooth: true,
      symbol: 'diamond',
      symbolSize: 6,
      data: [4, 3, 2, 3, 1, 0, 0],
      lineStyle: { color: '#ff5252', width: 2, type: 'dashed' },
      itemStyle: { color: '#ff5252' }
    }
  ]
}))

// 饼图：区域设备分布
const regionOption = computed(() => {
  const regionCount = {}
  devices.value.forEach(d => {
    regionCount[d.region] = (regionCount[d.region] || 0) + 1
  })
  const data = Object.entries(regionCount).map(([name, value], i) => ({
    name, value,
    itemStyle: { color: ['#00e5ff', '#7c4dff', '#ff6e40', '#69f0ae'][i] || '#64b5f6' }
  }))
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(10,14,39,0.9)',
      borderColor: '#1e3a5f',
      textStyle: { color: '#e0e0e0', fontSize: 12 },
      formatter: '{b}: {c} 台 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%', top: 'center',
      textStyle: { color: darkTextColor, fontSize: 11 }
    },
    series: [{
      type: 'pie',
      radius: ['55%', '80%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 3, borderColor: 'rgba(10,14,39,0.8)', borderWidth: 3 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold', color: '#fff' },
        scaleSize: 10
      },
      data
    }]
  }
})

// 柱状图：能耗排行
const powerOption = computed(() => {
  const sorted = [...devices.value].sort((a, b) => b.power - a.power).slice(0, 8)
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(10,14,39,0.9)',
      borderColor: '#1e3a5f',
      textStyle: { color: '#e0e0e0', fontSize: 12 }
    },
    grid: { left: '5%', right: '10%', top: '5%', bottom: '5%', containLabel: true },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: 'rgba(55,71,79,0.3)', type: 'dashed' } },
      axisLabel: { color: darkTextColor, fontSize: 10 }
    },
    yAxis: {
      type: 'category',
      inverse: true,
      data: sorted.map(d => d.name.length > 10 ? d.name.slice(0, 10) + '...' : d.name),
      axisLine: { lineStyle: { color: darkAxisColor } },
      axisTick: { show: false },
      axisLabel: { color: darkTextColor, fontSize: 10 }
    },
    series: [{
      type: 'bar',
      data: sorted.map(d => ({
        value: d.power,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#1a237e' },
            { offset: 1, color: '#00e5ff' }
          ]),
          borderRadius: [0, 4, 4, 0]
        }
      })),
      barWidth: 16,
      label: {
        show: true, position: 'right',
        color: darkTextColor, fontSize: 10,
        formatter: '{c} W'
      }
    }]
  }
})

// ===================== Three.js 3D 场景 =====================
const threeContainer = ref(null)
const screenRef = ref(null)
const threeReady = ref(false)
const hoveredDevice = ref(null)
const tooltipStyle = ref({})
const isFullscreen = ref(false)

let scene, camera, renderer, controls, raycaster, mouse
let deviceMeshes = []
let animationId = null
let groundGrid = null

function initThreeScene() {
  const container = threeContainer.value
  if (!container) return

  const width = container.clientWidth
  const height = container.clientHeight

  // 场景
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x0a0f23)
  scene.fog = new THREE.FogExp2(0x0a0f23, 0.0008)

  // 相机
  camera = new THREE.PerspectiveCamera(50, width / height, 0.1, 100)
  camera.position.set(10, 8, 12)
  camera.lookAt(0, 0, 0)

  // 渲染器
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.2
  container.appendChild(renderer.domElement)

  // OrbitControls
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.minDistance = 6
  controls.maxDistance = 20
  controls.maxPolarAngle = Math.PI / 2.1
  controls.target.set(0, 0.5, 0)
  controls.update()

  // 射线拾取
  raycaster = new THREE.Raycaster()
  mouse = new THREE.Vector2()

  // 光照
  const ambientLight = new THREE.AmbientLight(0x223344, 1.5)
  scene.add(ambientLight)

  const directionalLight = new THREE.DirectionalLight(0x4488cc, 2.5)
  directionalLight.position.set(8, 15, 5)
  directionalLight.castShadow = true
  directionalLight.shadow.mapSize.width = 1024
  directionalLight.shadow.mapSize.height = 1024
  directionalLight.shadow.camera.near = 0.5
  directionalLight.shadow.camera.far = 50
  directionalLight.shadow.camera.left = -15
  directionalLight.shadow.camera.right = 15
  directionalLight.shadow.camera.top = 15
  directionalLight.shadow.camera.bottom = -15
  directionalLight.shadow.bias = -0.0001
  scene.add(directionalLight)

  // 底部补光
  const bottomLight = new THREE.PointLight(0x0066cc, 3, 15)
  bottomLight.position.set(0, -1, 0)
  scene.add(bottomLight)

  // 地面
  const groundGeometry = new THREE.PlaneGeometry(20, 20)
  const groundMaterial = new THREE.MeshStandardMaterial({
    color: 0x0d1b3e,
    roughness: 0.8,
    metalness: 0.3,
    transparent: true,
    opacity: 0.7
  })
  const ground = new THREE.Mesh(groundGeometry, groundMaterial)
  ground.rotation.x = -Math.PI / 2
  ground.position.y = -2.2
  ground.receiveShadow = true
  scene.add(ground)

  // 网格辅助线
  const gridHelper = new THREE.PolarGridHelper(9, 32, 24, 64, 0x1a3a5c, 0x1a3a5c)
  gridHelper.position.y = -2.18
  scene.add(gridHelper)
  groundGrid = gridHelper

  // 中心发光环
  const ringGeometry = new THREE.TorusGeometry(2.5, 0.03, 16, 100)
  const ringMaterial = new THREE.MeshStandardMaterial({
    color: 0x00e5ff,
    emissive: 0x00e5ff,
    emissiveIntensity: 1.5,
    roughness: 0.3,
    metalness: 0.8
  })
  const ring = new THREE.Mesh(ringGeometry, ringMaterial)
  ring.rotation.x = -Math.PI / 2
  ring.position.y = -2.15
  ring.name = 'centerRing'
  scene.add(ring)

  // 粒子效果
  const particlesGeometry = new THREE.BufferGeometry()
  const particlesCount = 300
  const positions = new Float32Array(particlesCount * 3)
  for (let i = 0; i < particlesCount; i++) {
    positions[i * 3] = (Math.random() - 0.5) * 16
    positions[i * 3 + 1] = Math.random() * 6 - 1
    positions[i * 3 + 2] = (Math.random() - 0.5) * 16
  }
  particlesGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  const particlesMaterial = new THREE.PointsMaterial({
    color: 0x4488cc,
    size: 0.03,
    transparent: true,
    opacity: 0.6,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  })
  const particles = new THREE.Points(particlesGeometry, particlesMaterial)
  particles.name = 'particles'
  scene.add(particles)

  // 创建设备方块
  createDeviceMeshes()

  // 事件监听
  renderer.domElement.addEventListener('mousemove', onMouseMove)
  renderer.domElement.addEventListener('click', onMouseClick)
  window.addEventListener('resize', onSceneResize)

  threeReady.value = true
  animate()
}

function createDeviceMeshes() {
  // 清除旧模型
  deviceMeshes.forEach(m => scene.remove(m))
  deviceMeshes = []

  devices.value.forEach(device => {
    const { x, z } = device
    const colorHex = statusColorMap[device.status]
    const color = new THREE.Color(colorHex)

    // 主体方块
    const boxGeometry = new THREE.BoxGeometry(1.2, 0.7, 0.8)
    const boxMaterial = new THREE.MeshStandardMaterial({
      color,
      roughness: 0.25,
      metalness: 0.7,
      emissive: color,
      emissiveIntensity: device.status === 'error' ? 0.6 : 0.2
    })
    const box = new THREE.Mesh(boxGeometry, boxMaterial)
    box.position.set(x, 0, z)
    box.castShadow = true
    box.receiveShadow = true
    box.userData = { deviceId: device.id, deviceData: device }
    scene.add(box)
    deviceMeshes.push(box)

    // 边框线
    const edgeGeometry = new THREE.EdgesGeometry(boxGeometry)
    const edgeMaterial = new THREE.LineBasicMaterial({
      color: colorHex,
      transparent: true,
      opacity: 0.5
    })
    const edgeLine = new THREE.LineSegments(edgeGeometry, edgeMaterial)
    edgeLine.position.copy(box.position)
    edgeLine.userData = { parentMesh: box }
    edgeLine.name = 'edgeLine'
    scene.add(edgeLine)
    deviceMeshes.push(edgeLine)

    // 底座
    const baseGeometry = new THREE.CylinderGeometry(0.55, 0.65, 0.2, 32)
    const baseMaterial = new THREE.MeshStandardMaterial({
      color: 0x1a3a5c,
      roughness: 0.4,
      metalness: 0.8,
      emissive: 0x0a1a2a,
      emissiveIntensity: 0.3
    })
    const base = new THREE.Mesh(baseGeometry, baseMaterial)
    base.position.set(x, -0.45, z)
    base.receiveShadow = true
    base.userData = { parentId: device.id }
    scene.add(base)
    deviceMeshes.push(base)

    // 顶部状态指示灯
    const indicatorGeometry = new THREE.SphereGeometry(0.12, 16, 16)
    const indicatorMaterial = new THREE.MeshStandardMaterial({
      color,
      roughness: 0.2,
      metalness: 0.3,
      emissive: color,
      emissiveIntensity: 1.2
    })
    const indicator = new THREE.Mesh(indicatorGeometry, indicatorMaterial)
    indicator.position.set(x, 0.5, z)
    indicator.userData = { parentId: device.id, isIndicator: true }
    scene.add(indicator)
    deviceMeshes.push(indicator)
  })
}

function animate() {
  animationId = requestAnimationFrame(animate)
  controls.update()

  // 设备微动效果
  const time = Date.now() * 0.001
  deviceMeshes.forEach(m => {
    if (m.userData?.isIndicator) {
      // 指示灯闪烁
      const intensity = 0.7 + Math.sin(time * 3 + m.position.x) * 0.3
      m.material.emissiveIntensity = intensity
    }
  })

  // 中心环旋转
  const ring = scene.getObjectByName('centerRing')
  if (ring) ring.rotation.z += 0.003

  // 粒子缓慢上升
  const particles = scene.getObjectByName('particles')
  if (particles) {
    particles.rotation.y += 0.0005
    particles.rotation.x += 0.0002
  }

  renderer.render(scene, camera)
}

function onMouseMove(event) {
  const container = threeContainer.value
  if (!container || !raycaster) return

  const rect = container.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(mouse, camera)

  // 只检测方块主体
  const boxes = deviceMeshes.filter(m => m.userData?.deviceId)
  const intersects = raycaster.intersectObjects(boxes, false)

  if (intersects.length > 0) {
    const obj = intersects[0].object
    hoveredDevice.value = obj.userData.deviceData
    tooltipStyle.value = {
      left: event.clientX - container.getBoundingClientRect().left + 20 + 'px',
      top: event.clientY - container.getBoundingClientRect().top - 20 + 'px'
    }
    container.style.cursor = 'pointer'

    // 高亮效果
    deviceMeshes.forEach(m => {
      if (m.userData?.deviceId && m.userData.deviceId !== obj.userData.deviceId) {
        m.material.emissiveIntensity = m.userData.deviceData?.status === 'error' ? 0.6 : 0.2
      }
    })
    boxes.forEach(b => {
      if (b.userData.deviceId === obj.userData.deviceId) {
        b.material.emissiveIntensity = 0.9
      }
    })
  } else {
    hoveredDevice.value = null
    container.style.cursor = 'grab'
    // 恢复所有设备发光
    deviceMeshes.forEach(m => {
      if (m.userData?.deviceId) {
        m.material.emissiveIntensity = m.userData.deviceData?.status === 'error' ? 0.6 : 0.2
      }
    })
  }
}

function onMouseClick(event) {
  if (hoveredDevice.value) {
    // 可以在这里做更多交互，比如弹窗详情
    console.log('点击设备:', hoveredDevice.value)
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
  if (controls) {
    controls.dispose()
  }
  if (scene) {
    scene.clear()
  }
  deviceMeshes = []
  threeReady.value = false
}

// ===================== 全屏 =====================
function toggleFullscreen() {
  if (!isFullscreen.value) {
    const el = screenRef.value || document.documentElement
    if (el.requestFullscreen) {
      el.requestFullscreen()
    }
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    }
  }
}

function onFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
}

// ===================== 时钟 =====================
function updateTime() {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

// ===================== 生命周期 =====================
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
/* ========== 重置与基础 ========== */
.dashboard-screen {
  margin: -16px;
  height: calc(100vh - 50px - 34px);
  min-height: 700px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: radial-gradient(ellipse at center, #0d1b3e 0%, #070c1f 100%);
  color: #e0e0e0;
  font-family: 'Helvetica Neue', 'Microsoft YaHei', sans-serif;
  position: relative;
}

.dashboard-screen:fullscreen {
  height: 100vh;
  margin: 0;
}

.dashboard-screen:-webkit-full-screen {
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

.header-center {
  display: flex;
  align-items: center;
}

.status-indicators {
  display: flex;
  gap: 24px;
}

.indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #90a4ae;
}

.indicator .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.indicator.online .dot { background: #00e676; box-shadow: 0 0 6px rgba(0, 230, 118, 0.6); }
.indicator.warning .dot { background: #ffab00; box-shadow: 0 0 6px rgba(255, 171, 0, 0.6); }
.indicator.offline .dot { background: #ff1744; box-shadow: 0 0 6px rgba(255, 23, 68, 0.6); }

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
  grid-template-columns: 320px 1fr 360px;
  gap: 18px;
  padding: 14px;
  min-height: 0;
  overflow: hidden;
}

/* ========== 左侧/右侧面板 ========== */
.left-panel,
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 0;
  overflow: hidden;
}

.left-panel > :first-child { flex: 1.1; min-height: 0; }
.left-panel > :last-child { flex: 1; min-height: 0; }
.right-panel > :first-child { flex: 1.1; min-height: 0; }
.right-panel > :last-child { flex: 1; min-height: 0; }

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

.panel-header .header-icon.trend { color: #00e5ff; }
.panel-header .header-icon.pie { color: #7c4dff; }
.panel-header .header-icon.bar { color: #ff6e40; }
.panel-header .header-icon.alert { color: #ff5252; }

.panel-body {
  flex: 1;
  padding: 10px;
  min-height: 0;
  overflow: hidden;
}

/* ========== 中间 3D 场景 ========== */
.center-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  overflow: hidden;
}

.center-top-stats {
  display: flex;
  gap: 18px;
  justify-content: center;
}

.mini-stat {
  background: rgba(10, 18, 48, 0.7);
  border: 1px solid rgba(0, 229, 255, 0.1);
  border-radius: 6px;
  padding: 10px 24px;
  text-align: center;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.mini-stat-value {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
}

.mini-stat-label {
  font-size: 11px;
  color: #78909c;
  margin-top: 2px;
}

.three-container {
  flex: 1;
  position: relative;
  border: 1px solid rgba(0, 229, 255, 0.12);
  border-radius: 6px;
  overflow: hidden;
  background: radial-gradient(ellipse at center, #0d1a36 0%, #060d1f 100%);
  min-height: 300px;
}

.three-container::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 6px;
  border: 1px solid transparent;
  background: linear-gradient(135deg, rgba(0,229,255,0.08), transparent 50%, transparent 50%, rgba(0,229,255,0.04)) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
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

.device-tooltip {
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

.tooltip-row span:last-child {
  color: #e0e0e0;
}

.tooltip-row .status-normal { color: #00e676 !important; }
.tooltip-row .status-warning { color: #ffab00 !important; }
.tooltip-row .status-error { color: #ff1744 !important; }

.scene-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding: 4px 0;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #78909c;
}

.legend-item i {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  display: inline-block;
}

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

.alert-item:hover {
  background: rgba(10, 18, 48, 0.8);
}

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
    grid-template-columns: 280px 1fr 320px;
  }
}

@media (max-width: 1200px) {
  .screen-body {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr auto;
  }
  .left-panel, .right-panel {
    flex-direction: row;
    gap: 12px;
  }
  .left-panel > *, .right-panel > * {
    flex: 1;
    min-width: 0;
  }
}
</style>
