<template>
  <div class="statistics-dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner">
            <div class="stat-icon" style="background: linear-gradient(135deg, #e8f4fd, #c8e4fb);">
              <el-icon :size="26" color="#4f6ef7"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.userCount ?? 0 }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner">
            <div class="stat-icon" style="background: linear-gradient(135deg, #e8f8e8, #c8f0c8);">
              <el-icon :size="26" color="#67C23A"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.activeUsers ?? 0 }}</div>
              <div class="stat-label">启用用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fdf6ec, #fae8c8);">
              <el-icon :size="26" color="#E6A23C"><Avatar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.roleCount ?? 0 }}</div>
              <div class="stat-label">角色数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fde8e8, #fbc8c8);">
              <el-icon :size="26" color="#F56C6C"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.deptCount ?? 0 }}</div>
              <div class="stat-label">部门数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f0e8fd, #dcc8fb);">
              <el-icon :size="26" color="#9B59B6"><Menu /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.menuCount ?? 0 }}</div>
              <div class="stat-label">菜单数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-inner">
            <div class="stat-icon" style="background: linear-gradient(135deg, #ede8e8, #d8c8c8);">
              <el-icon :size="26" color="#909399"><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.disabledUsers ?? 0 }}</div>
              <div class="stat-label">禁用用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <!-- 用户增长趋势 -->
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">用户增长趋势</span>
              <el-tag size="small" type="info">近 7 天</el-tag>
            </div>
          </template>
          <v-chart :option="trendOption" style="height: 360px;" autoresize />
        </el-card>
      </el-col>

      <!-- 用户状态分布 -->
      <el-col :xs="24" :lg="10">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">用户状态分布</span>
              <el-tag size="small" type="info">实时</el-tag>
            </div>
          </template>
          <v-chart :option="statusOption" style="height: 360px;" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新用户 -->
    <el-card shadow="hover" class="table-card" style="margin-top: 16px;">
      <template #header>
        <div class="card-header">
          <span class="card-title">最近注册用户</span>
          <el-tag size="small" type="success">TOP 10</el-tag>
        </div>
      </template>
      <el-table :data="latestUsers" stripe size="small" v-loading="tableLoading">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120">
          <template #default="{ row }">
            <el-avatar :size="24" style="vertical-align: middle; margin-right: 8px;">
              {{ row.nickname?.charAt(0) || row.username?.charAt(0) }}
            </el-avatar>
            {{ row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="注册时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { use } from 'echarts/core'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import {
  UserFilled, User, Avatar, OfficeBuilding, Menu, WarningFilled
} from '@element-plus/icons-vue'

// 按需注册 ECharts 模块
use([LineChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const overview = ref({})
const userTrend = ref([])
const latestUsers = ref([])
const tableLoading = ref(false)

/** 用户增长趋势图 */
const trendOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' }
  },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: userTrend.value.map(i => i.day?.slice(5) || i.day),
    axisLabel: { rotate: 30, fontSize: 11 },
    boundaryGap: false
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    axisLabel: { fontSize: 11 }
  },
  series: [{
    name: '新增用户',
    type: 'line',
    smooth: true,
    symbol: 'circle',
    symbolSize: 6,
    data: userTrend.value.map(i => i.count),
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(79, 110, 247, 0.35)' },
          { offset: 1, color: 'rgba(79, 110, 247, 0.02)' }
        ]
      }
    },
    lineStyle: { color: '#4f6ef7', width: 2 },
    itemStyle: { color: '#4f6ef7' }
  }]
}))

/** 用户状态饼图 */
const statusOption = computed(() => {
  const active = overview.value.activeUsers ?? 0
  const disabled = overview.value.disabledUsers ?? 0
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      textStyle: { fontSize: 12 }
    },
    series: [{
      name: '用户状态',
      type: 'pie',
      radius: ['50%', '75%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 3 },
      label: {
        show: true,
        position: 'center',
        formatter: () => `{total|${active + disabled}}\n{label|总用户}`,
        rich: {
          total: { fontSize: 28, fontWeight: 'bold', color: '#303133', lineHeight: 32 },
          label: { fontSize: 12, color: '#909399', lineHeight: 18 }
        }
      },
      emphasis: {
        label: { fontSize: 18, fontWeight: 'bold' }
      },
      labelLine: { show: false },
      data: [
        {
          value: active,
          name: '启用',
          itemStyle: { color: '#67C23A' }
        },
        {
          value: disabled,
          name: '禁用',
          itemStyle: { color: '#F56C6C' }
        }
      ]
    }]
  }
})

/** 格式化时间 */
const formatTime = (val) => {
  if (!val) return '-'
  const d = new Date(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** 加载概览数据 */
const loadOverview = async () => {
  try {
    const res = await request.get('/statistics/overview')
    if (res.data) overview.value = res.data
  } catch (e) {
    console.error('加载概览数据失败', e)
  }
}

/** 加载用户趋势 */
const loadTrend = async () => {
  try {
    const res = await request.get('/statistics/user-trend-week')
    if (res.data) userTrend.value = res.data
  } catch (e) {
    console.error('加载用户趋势失败', e)
  }
}

/** 加载最新用户 */
const loadLatestUsers = async () => {
  tableLoading.value = true
  try {
    const res = await request.get('/statistics/latest-users')
    if (res.data) latestUsers.value = res.data
  } catch (e) {
    console.error('加载最新用户失败', e)
  } finally {
    tableLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadOverview(), loadTrend(), loadLatestUsers()])
})
</script>

<style scoped>
.statistics-dashboard {
  padding: 4px 0;
}

/* ---- 统计卡片 ---- */
.stats-row {
  margin-bottom: 0;
}

.stat-card {
  cursor: default;
  transition: transform 0.25s, box-shadow 0.25s;
  border-radius: 8px;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.stat-card :deep(.el-card__body) {
  padding: 18px 20px;
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  line-height: 1.1;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* ---- 图表卡片 ---- */
.chart-card {
  border-radius: 8px;
}

.chart-card :deep(.el-card__body) {
  padding: 12px 16px 4px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  position: relative;
  padding-left: 12px;
}

.card-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 16px;
  background: var(--color-primary);
  border-radius: 2px;
}

/* ---- 表格卡片 ---- */
.table-card {
  border-radius: 8px;
}

.table-card :deep(.el-card__body) {
  padding: 12px 16px 4px;
}
</style>
