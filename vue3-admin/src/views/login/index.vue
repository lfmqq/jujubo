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
          <p>{{ activeTabDesc }}</p>
        </div>

        <!-- 登录方式切换 Tab -->
        <div class="login-tabs">
          <span
            v-for="tab in loginTabs"
            :key="tab.key"
            :class="['tab-item', { active: activeTab === tab.key }]"
            @click="switchTab(tab.key)"
          >{{ tab.label }}</span>
        </div>

        <!-- ==================== 密码登录 ==================== -->
        <el-form v-show="activeTab === 'password'" ref="pwdRef" :model="pwdForm" :rules="pwdRules" size="large">
          <el-form-item prop="username">
            <el-input v-model="pwdForm.username" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="pwdForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item prop="code">
            <div class="captcha-row">
              <el-input
                v-model="pwdForm.code"
                placeholder="请输入验证码"
                :prefix-icon="Picture"
                style="flex: 1"
                @keyup.enter="handleLogin"
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
            <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- ==================== 短信验证码登录 ==================== -->
        <el-form v-show="activeTab === 'sms'" ref="smsRef" :model="smsForm" :rules="smsRules" size="large">
          <el-alert
            title="当前未接入真实短信服务，验证码将在弹窗中展示"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 16px;"
          />
          <el-form-item prop="phone">
            <el-input v-model="smsForm.phone" placeholder="请输入手机号" :prefix-icon="Phone" />
          </el-form-item>
          <el-form-item prop="code">
            <div class="captcha-row">
              <el-input
                v-model="smsForm.code"
                placeholder="请输入短信验证码"
                :prefix-icon="Message"
                style="flex: 1"
                @keyup.enter="handleLogin"
              />
              <el-button
                class="send-code-btn"
                :disabled="smsCountdown > 0"
                :loading="smsSending"
                @click="sendSmsCode"
              >
                {{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- ==================== 邮箱验证码登录 ==================== -->
        <el-form v-show="activeTab === 'email'" ref="emailRef" :model="emailForm" :rules="emailRules" size="large">
          <el-form-item prop="email">
            <el-input v-model="emailForm.email" placeholder="请输入邮箱地址" :prefix-icon="Message" />
          </el-form-item>
          <el-form-item prop="code">
            <div class="captcha-row">
              <el-input
                v-model="emailForm.code"
                placeholder="请输入邮箱验证码"
                :prefix-icon="Picture"
                style="flex: 1"
                @keyup.enter="handleLogin"
              />
              <el-button
                class="send-code-btn"
                :disabled="emailCountdown > 0"
                :loading="emailSending"
                @click="sendEmailCode"
              >
                {{ emailCountdown > 0 ? emailCountdown + 's' : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
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
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Check, Picture, Phone, Message } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const menuStore = useMenuStore()

const loading = ref(false)
const captchaImg = ref('')

// ==================== Tab 切换 ====================
const activeTab = ref('password')
const loginTabs = [
  { key: 'password', label: '密码登录', desc: '请输入您的账号密码' },
  { key: 'sms', label: '短信登录', desc: '请输入手机号获取验证码' },
  { key: 'email', label: '邮箱登录', desc: '请输入邮箱获取验证码' }
]

const activeTabDesc = computed(() => {
  const tab = loginTabs.find(t => t.key === activeTab.value)
  return tab ? tab.desc : ''
})

const switchTab = (key) => {
  activeTab.value = key
  if (key === 'password') {
    getCaptcha()
  }
}

// ==================== 密码登录 ====================
const pwdRef = ref(null)
const pwdForm = ref({ username: '', password: '', code: '', uuid: '' })
const pwdRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// ==================== 短信登录 ====================
const smsRef = ref(null)
const smsForm = ref({ phone: '', code: '' })
const smsSending = ref(false)
const smsCountdown = ref(0)
let smsTimer = null
const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }]
}

// ==================== 邮箱登录 ====================
const emailRef = ref(null)
const emailForm = ref({ email: '', code: '' })
const emailSending = ref(false)
const emailCountdown = ref(0)
let emailTimer = null
const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' }]
}

// ==================== 获取图形验证码 ====================
const getCaptcha = async () => {
  try {
    const res = await request.get('/auth/captcha')
    pwdForm.value.uuid = res.data.uuid
    captchaImg.value = res.data.img
    pwdForm.value.code = ''
  } catch (e) {
    // 获取失败不影响登录表单，静默处理
  }
}

// ==================== 倒计时工具 ====================
const startCountdown = (countdownRef) => {
  countdownRef.value = 60
  const timerKey = countdownRef === smsCountdown ? 'smsTimer' : 'emailTimer'
  const timerRef = countdownRef === smsCountdown ? smsTimer : emailTimer
  if (timerRef) clearInterval(timerRef)
  const interval = setInterval(() => {
    countdownRef.value--
    if (countdownRef.value <= 0) {
      clearInterval(interval)
      if (timerKey === 'smsTimer') smsTimer = null
      else emailTimer = null
    }
  }, 1000)
  if (timerKey === 'smsTimer') smsTimer = interval
  else emailTimer = interval
}

// ==================== 发送验证码 ====================
const sendSmsCode = async () => {
  // 先校验手机号
  try {
    await smsRef.value.validateField('phone')
  } catch {
    return
  }
  smsSending.value = true
  try {
    const res = await request.post('/auth/send-code', { account: smsForm.value.phone, type: 'sms' })
    if (res.data?.degrade && res.data?.code) {
      // 降级模式：弹窗展示验证码
      ElMessageBox.alert(
        `您的手机号：<strong>${smsForm.value.phone}</strong><br/>
         验证码：<strong style="font-size:24px;color:#409eff;letter-spacing:4px;">${res.data.code}</strong><br/>
         有效期：5 分钟`,
        '短信验证码（降级模式）',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '我知道了',
          type: 'warning',
          center: true
        }
      )
    } else {
      ElMessage.success('验证码已发送')
    }
    startCountdown(smsCountdown)
  } finally {
    smsSending.value = false
  }
}

const sendEmailCode = async () => {
  try {
    await emailRef.value.validateField('email')
  } catch {
    return
  }
  emailSending.value = true
  try {
    const res = await request.post('/auth/send-code', { account: emailForm.value.email, type: 'email' })
    if (res.data?.degrade && res.data?.code) {
      // 降级模式：弹窗展示验证码
      ElMessageBox.alert(
        `您的邮箱：<strong>${emailForm.value.email}</strong><br/>
         验证码：<strong style="font-size:24px;color:#409eff;letter-spacing:4px;">${res.data.code}</strong><br/>
         有效期：5 分钟`,
        '邮箱验证码（降级模式）',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '我知道了',
          type: 'warning',
          center: true
        }
      )
    } else {
      ElMessage.success('验证码已发送')
    }
    startCountdown(emailCountdown)
  } finally {
    emailSending.value = false
  }
}

// ==================== 登录处理 ====================
const doLoginSuccess = async (token) => {
  userStore.setToken(token)
  await Promise.all([menuStore.fetchMenus(), menuStore.fetchPermissions(), userStore.getUserInfo()])
  ElMessage.success('登录成功')
  const redirect = route.query.redirect || '/'
  router.push(redirect)
}

const handleLogin = async () => {
  if (activeTab.value === 'password') {
    // 密码登录
    await pwdRef.value.validate()
    loading.value = true
    try {
      const res = await request.post('/auth/login', pwdForm.value)
      await doLoginSuccess(res.data.token)
    } finally {
      loading.value = false
      getCaptcha()
    }
  } else if (activeTab.value === 'sms') {
    // 短信验证码登录
    await smsRef.value.validate()
    loading.value = true
    try {
      const res = await request.post('/auth/login/code', {
        account: smsForm.value.phone,
        code: smsForm.value.code,
        type: 'sms'
      })
      await doLoginSuccess(res.data.token)
    } finally {
      loading.value = false
    }
  } else if (activeTab.value === 'email') {
    // 邮箱验证码登录
    await emailRef.value.validate()
    loading.value = true
    try {
      const res = await request.post('/auth/login/code', {
        account: emailForm.value.email,
        code: emailForm.value.code,
        type: 'email'
      })
      await doLoginSuccess(res.data.token)
    } finally {
      loading.value = false
    }
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
  width: 900px;
  min-height: 500px;
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
  padding: 40px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 8px;
}

.form-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.form-header p {
  font-size: 13px;
  color: #909399;
}

/* Tab 切换 */
.login-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  border-bottom: 2px solid #ebeef5;
}

.tab-item {
  padding: 8px 20px;
  font-size: 14px;
  color: #909399;
  cursor: pointer;
  position: relative;
  transition: color 0.3s;
  user-select: none;
}

.tab-item:hover {
  color: #409eff;
}

.tab-item.active {
  color: #409eff;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 80%;
  height: 2px;
  background: #409eff;
  border-radius: 1px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 4px;
}

/* 验证码 / 发送按钮行 */
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

.send-code-btn {
  width: 120px;
  height: 40px;
  flex-shrink: 0;
  font-size: 13px;
}

.login-footer {
  position: fixed;
  bottom: 16px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
}

/* 响应式 */
@media (max-width: 950px) {
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

  .send-code-btn {
    width: 110px;
    font-size: 12px;
  }
}
</style>
