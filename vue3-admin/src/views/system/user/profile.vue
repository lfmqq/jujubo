<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 左侧：用户信息卡片 -->
      <el-col :span="8">
        <el-card shadow="never">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              action=""
              :show-file-list="false"
              :http-request="handleAvatarUpload"
              :before-upload="beforeAvatarUpload"
            >
              <el-avatar :size="100" :src="form.avatar">
                <el-icon :size="40"><UserFilled /></el-icon>
              </el-avatar>
            </el-upload>
            <h3 class="profile-nickname">{{ form.nickname || form.username }}</h3>
            <p class="profile-username">{{ form.username }}</p>
          </div>
          <el-divider />
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-label">角色</span>
              <span class="stat-value">{{ roles.length > 0 ? roles.join('、') : '-' }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">权限数</span>
              <span class="stat-value">{{ permissions?.length || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：编辑信息 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="80px"
            size="default"
          >
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card shadow="never" class="password-card">
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-width="100px"
            size="default"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="pwdForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="pwdForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码（至少6位）"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="pwdForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changingPwd" @click="handleChangePwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const pwdFormRef = ref(null)
const saving = ref(false)
const changingPwd = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  avatar: ''
})

const roles = ref([])
const permissions = ref([])

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }]
}

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 加载用户信息
const loadProfile = async () => {
  try {
    const res = await request.get('/system/user/profile')
    const user = res.data?.user || res.data || {}
    form.username = user.username || ''
    form.nickname = user.nickname || ''
    form.email = user.email || ''
    form.avatar = user.avatar || ''
    roles.value = res.data?.roles || []
    permissions.value = res.data?.permissions || []
    // 同步更新 store 缓存
    userStore.setUserInfo(user)
  } catch {
    ElMessage.error('获取用户信息失败')
  }
}

// 重置表单为原始值
const handleReset = () => {
  loadProfile()
}

// 保存基本信息
const handleSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    await request.put('/system/user/profile', {
      nickname: form.nickname,
      email: form.email,
      avatar: form.avatar
    })
    // 更新本地 store 缓存
    userStore.setUserInfo({
      nickname: form.nickname,
      email: form.email,
      avatar: form.avatar
    })
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

// 修改密码
const handleChangePwd = async () => {
  await pwdFormRef.value.validate()
  changingPwd.value = true
  try {
    await request.put('/system/user/profile/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    // 清除 token 并跳转登录页
    userStore.logout()
    router.push('/login')
  } finally {
    changingPwd.value = false
  }
}

// 头像上传
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

const handleAvatarUpload = async ({ file }) => {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/common/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const avatarUrl = res.data?.url || res.data
    // 上传成功后立即保存到用户表
    await request.put('/system/user/profile', { avatar: avatarUrl })
    form.avatar = avatarUrl
    // 同步更新 store 缓存
    userStore.setUserInfo({ avatar: avatarUrl })
    ElMessage.success('头像更新成功')
  } catch {
    ElMessage.error('头像上传失败')
  }
}

onMounted(() => {
  // 优先使用 store 缓存，否则从接口拉取
  if (userStore.userInfo?.id) {
    form.username = userStore.userInfo.username || ''
    form.nickname = userStore.userInfo.nickname || ''
    form.email = userStore.userInfo.email || ''
    form.avatar = userStore.userInfo.avatar || ''
  }
  // 始终从接口拉取最新数据，同时更新权限/角色信息
  loadProfile()
})
</script>

<style scoped>
.profile-container {
  padding: 16px;
  max-width: 1100px;
}

.profile-container .el-card {
  margin-bottom: 16px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0 10px;
}

.avatar-uploader {
  cursor: pointer;
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
}

.avatar-uploader :deep(.el-avatar):hover {
  opacity: 0.8;
}

.avatar-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 8px;
  text-align: center;
  white-space: nowrap;
}

.profile-nickname {
  margin: 12px 0 4px;
  font-size: 18px;
  font-weight: 600;
}

.profile-username {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin: 0;
}

.profile-stats {
  padding: 4px 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.stat-label {
  color: var(--el-text-color-secondary);
}

.stat-value {
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.password-card {
  margin-top: 16px;
}
</style>
