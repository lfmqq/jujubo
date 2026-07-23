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
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="16">
        <el-card class="content-card">
          <template #header>
            <span>系统信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="系统名称">桔桔波管理系统</el-descriptions-item>
            <el-descriptions-item label="版本号">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="后端框架">SpringBoot 3 + Spring Security 6</el-descriptions-item>
            <el-descriptions-item label="前端框架">Vue 3 + Vite + Element Plus</el-descriptions-item>
            <el-descriptions-item label="ORM 框架">MyBatis-Plus</el-descriptions-item>
            <el-descriptions-item label="缓存方案">Redis + JWT</el-descriptions-item>
            <el-descriptions-item label="权限模型" :span="2">RBAC（用户 - 角色 - 菜单）三级权限控制</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
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
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { User, Avatar, Menu, OfficeBuilding } from '@element-plus/icons-vue'

const stats = ref({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  deptCount: 0
})

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

onMounted(loadStats)
</script>

<style scoped>
.stats-row {
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
  border-radius: 12px;
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

.content-card {
  height: 100%;
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
</style>
