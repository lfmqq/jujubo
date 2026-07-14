<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openDialog">新增用户</el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="用户名" prop="username" min-width="120" />
        <el-table-column label="昵称" prop="nickname" min-width="120" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="230" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" :icon="Edit" @click="edit(scope.row)">编辑</el-button>
            <el-button type="warning" link size="small" :icon="Refresh" @click="resetPwd(scope.row)">重置密码</el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="del(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @change="loadData"
        />
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%;">
            <el-option v-for="r in roleList" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="save" :loading="saving">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const roleList = ref([])

const queryParams = ref({
  username: '',
  status: null
})

const form = ref({ status: 1, roleIds: [] })
const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (queryParams.value.username) params.username = queryParams.value.username
    if (queryParams.value.status !== null && queryParams.value.status !== '') params.status = queryParams.value.status
    const res = await request.get('/system/user/page', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pageNum.value = 1
  loadData()
}

const handleReset = () => {
  queryParams.value = { username: '', status: null }
  handleQuery()
}

// 加载角色列表
const loadRoleList = async () => {
  try {
    const res = await request.get('/system/role/list')
    roleList.value = res.data || []
  } catch { /* ignore */ }
}

const openDialog = () => {
  form.value = { status: 1, roleIds: [] }
  loadRoleList()
  dialogVisible.value = true
}

const edit = async (row) => {
  loadRoleList()
  try {
    const res = await request.get(`/system/user/${row.id}`)
    const user = res.data
    form.value = {
      ...user,
      roleIds: user.roleIds || []
    }
  } catch {
    // 降级：不带角色信息打开编辑
    form.value = { ...row, roleIds: [] }
  }
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await request.put('/system/user', form.value)
    } else {
      await request.post('/system/user', form.value)
    }
    dialogVisible.value = false
    ElMessage.success('保存成功')
    loadData()
  } finally {
    saving.value = false
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该用户吗？', '提示', { type: 'warning' })
  await request.delete(`/system/user/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

const resetPwd = async (row) => {
  await ElMessageBox.confirm(
    `确认将用户「${row.username}」的密码重置为默认密码 awei123456 吗？`,
    '重置密码',
    { type: 'warning', confirmButtonText: '确认重置' }
  )
  await request.post(`/system/user/reset-password/${row.id}`)
  ElMessage.success('密码已重置为 awei123456')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-container {
  height: 100%;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}
</style>
