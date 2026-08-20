<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 左侧 Banner -->
      <div class="login-banner">
        <div class="banner-orbit orbit-one"></div>
        <div class="banner-orbit orbit-two"></div>
        <div class="banner-content">
          <img src="/logo.png" class="banner-logo" alt="logo" />
          <h1 class="banner-title">桔桔波管理系统</h1>
          <p class="banner-desc">让每一次管理，都更简单高效</p>
          <div class="banner-slogan"><span></span> 专注 · 协作 · 成长 <span></span></div>
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
import { User, Lock, Picture, Phone, Message } from '@element-plus/icons-vue'

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
         验证码：<strong style="font-size:24px;color:var(--color-primary);letter-spacing:4px;">${res.data.code}</strong><br/>
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
         验证码：<strong style="font-size:24px;color:var(--color-primary);letter-spacing:4px;">${res.data.code}</strong><br/>
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
  position: relative;
  overflow: hidden;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(120deg, #111827 0%, #172554 52%, #3730a3 100%);
}

.login-container::before,
.login-container::after {
  content: '';
  position: absolute;
  width: 360px;
  height: 360px;
  border: 1px solid rgba(129, 140, 248, 0.22);
  border-radius: 50%;
  animation: drift 14s ease-in-out infinite;
}
.login-container::before { top: -180px; left: -90px; box-shadow: 0 0 0 34px rgba(129, 140, 248, 0.04), 0 0 0 70px rgba(129, 140, 248, 0.03); }
.login-container::after { right: -140px; bottom: -220px; animation-delay: -7s; }
@keyframes drift { 0%, 100% { transform: translate3d(0, 0, 0) rotate(0deg); } 50% { transform: translate3d(28px, -18px, 0) rotate(12deg); } }

.login-box {
  position: relative;
  z-index: 1;
  display: flex;
  width: 900px;
  min-height: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  animation: card-in 0.75s cubic-bezier(.22, 1, .36, 1) both;
}
@keyframes card-in { from { opacity: 0; transform: translateY(24px) scale(.97); } to { opacity: 1; transform: translateY(0) scale(1); } }

/* 左侧 Banner */
.login-banner {
  position: relative;
  overflow: hidden;
  width: 400px;
  background: linear-gradient(145deg, #4f46e5 0%, #6366f1 48%, #7c3aed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.banner-content {
  position: relative;
  z-index: 1;
  color: #fff;
  text-align: center;
  animation: content-in 0.9s 0.15s both;
}
@keyframes content-in { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }

.banner-logo {
  width: 90px;
  height: 90px;
  object-fit: contain;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  padding: 10px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.banner-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: 2px;
}

.banner-desc {
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 32px;
}

.banner-slogan {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 12px;
  opacity: .78;
  letter-spacing: 2px;
}
.banner-slogan span { width: 24px; height: 1px; background: rgba(255,255,255,.6); }
.banner-orbit { position: absolute; border: 1px solid rgba(255,255,255,.16); border-radius: 50%; animation: orbit 10s linear infinite; }
.orbit-one { width: 280px; height: 280px; right: -150px; top: -100px; }
.orbit-two { width: 180px; height: 180px; left: -100px; bottom: -80px; animation-direction: reverse; }
@keyframes orbit { to { transform: rotate(360deg); } }

/* 右侧登录表单 */
.login-form {
  flex: 1;
  padding: 40px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  animation: form-in 0.8s 0.12s both;
}
@keyframes form-in { from { opacity: 0; transform: translateX(18px); } to { opacity: 1; transform: translateX(0); } }

.form-header {
  margin-bottom: 8px;
}

.form-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.form-header p {
  font-size: 13px;
  color: #94a3b8;
}

/* Tab 切换 */
.login-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  border-bottom: 2px solid #e2e8f0;
}

.tab-item {
  padding: 8px 20px;
  font-size: 14px;
  color: #94a3b8;
  cursor: pointer;
  position: relative;
  transition: color 0.3s;
  user-select: none;
}

.tab-item:hover {
  color: var(--color-primary);
}

.tab-item.active {
  color: var(--color-primary);
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
  background: var(--color-primary);
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
  border: 1px solid #e2e8f0;
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
  color: rgba(255, 255, 255, 0.5);
  z-index: 1;
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

@media (max-width: 600px) {
  .login-box { width: calc(100vw - 32px); min-height: 0; }
  .login-banner { min-height: 190px; padding: 28px 20px; }
  .banner-logo { width: 64px; height: 64px; margin-bottom: 12px; }
  .banner-title { font-size: 25px; }
  .banner-desc { margin-bottom: 14px; }
  .login-form { padding: 28px 22px 24px; }
  .login-footer { font-size: 10px; }
}
</style>
