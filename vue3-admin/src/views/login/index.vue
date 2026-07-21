<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 左侧 Banner -->
        <div class="login-banner">
        <div class="banner-content">
          <img src="/logo.png" class="banner-logo" alt="logo" />
          <h1 class="banner-title">桔桔波管理系统</h1>
          <p class="banner-desc">基于SpringBoot3 + Vue3 的后台管理系统</p>
          <div class="banner-features">
            <div class="feature-item">
              <el-icon><Check /></el-icon> RBAC 权限模型
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon> Spring Security 6
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon> MyBatis-Plus
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon> JWT + Redis
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-form">
        <div class="form-header">
          <h2>欢迎登录</h2>
          <p>请输入您的账号密码</p>
        </div>
        <el-form ref="loginRef" :model="loginForm" :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="login"
            />
          </el-form-item>
          <el-form-item prop="code">
            <div class="captcha-row">
              <el-input
                v-model="loginForm.code"
                placeholder="请输入验证码"
                :prefix-icon="Picture"
                style="flex: 1"
                @keyup.enter="login"
              />
              <img
                v-if="captchaImg"
                :src="captchaImg"
                class="captcha-img"
                title="点击刷新验证码"
                alt="验证码"
                @click="getCaptcha"
              />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" class="login-btn" @click="login">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="login-footer">
      Copyright © 2026 Orange Wave Management System. All Rights Reserved.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { User, Lock, Check, Picture } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const menuStore = useMenuStore()
const loginRef = ref(null)
const loading = ref(false)
const captchaImg = ref('')

const loginForm = ref({
  username: '',
  password: '',
  code: '',
  uuid: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 获取图形验证码
const getCaptcha = async () => {
  try {
    const res = await request.get('/auth/captcha')
    loginForm.value.uuid = res.data.uuid
    captchaImg.value = res.data.img
    loginForm.value.code = ''
  } catch (e) {
    // 获取失败不影响登录表单，静默处理
  }
}

const login = async () => {
  await loginRef.value.validate()
  loading.value = true
  try {
    const res = await request.post('/auth/login', loginForm.value)
    userStore.setToken(res.data.token)
    // 登录成功后拉取用户菜单 + 权限 + 用户信息
    await Promise.all([menuStore.fetchMenus(), menuStore.fetchPermissions(), userStore.getUserInfo()])
    ElMessage.success('登录成功')
    // 重定向到目标页或首页
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } finally {
    loading.value = false
    // 无论成功失败都刷新验证码（成功后跳转，失败则需重新输入）
    getCaptcha()
  }
}

onMounted(getCaptcha)
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(-45deg, #ee7752, #e73c7e, #7b4397, #23a6d5, #23d5ab, #6a5acd, #ee7752);
  background-size: 500% 500%;
  animation: loginGradientBG 18s ease infinite;
}

@keyframes loginGradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.login-box {
  display: flex;
  width: 850px;
  min-height: 460px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 左侧 Banner */
.login-banner {
  width: 400px;
  background: linear-gradient(-45deg, #667eea, #764ba2, #6a5acd, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  animation: loginGradientBG 14s ease infinite;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.banner-content {
  color: #fff;
  text-align: center;
}

.banner-logo {
  width: 90px;
  height: 90px;
  object-fit: contain;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  padding: 10px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.banner-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.banner-desc {
  font-size: 14px;
  opacity: 0.85;
  margin-bottom: 32px;
}

.banner-features {
  text-align: left;
  display: inline-block;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 10px;
}

/* 右侧登录表单 */
.login-form {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.form-header p {
  font-size: 13px;
  color: #909399;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 4px;
}

/* 验证码 */
.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.captcha-img {
  width: 120px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  cursor: pointer;
}

.login-footer {
  position: fixed;
  bottom: 16px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
}

/* 响应式 */
@media (max-width: 900px) {
  .login-box {
    width: 90vw;
    flex-direction: column;
  }

  .login-banner {
    width: 100%;
    padding: 24px;
  }

  .login-form {
    padding: 32px 24px;
  }
}
</style>
