<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" size="default" @submit.prevent>
        <el-form-item label="通知标题">
          <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="通知类型">
          <el-select v-model="queryParams.type" placeholder="请选择类型" clearable style="width: 140px;">
            <el-option label="系统通知" :value="1" />
            <el-option label="提醒" :value="2" />
            <el-option label="私信" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="阅读状态">
          <el-select v-model="queryParams.readStatus" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="未读" :value="0" />
            <el-option label="已读" :value="1" />
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
        <el-button type="primary" :icon="Plus" @click="openDialog">新增通知</el-button>
        <el-button :icon="Check" @click="handleReadAll" :disabled="!hasUnread">全部已读</el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="通知标题" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="通知类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="typeTagMap[scope.row.type]" size="small">
              {{ typeMap[scope.row.type] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容" prop="content" min-width="250" show-overflow-tooltip />
        <el-table-column label="阅读状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.readStatus === 1 ? 'success' : 'warning'" size="small">
              {{ scope.row.readStatus === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="阅读时间" prop="readTime" width="170" />
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" :icon="View" @click="viewDetail(scope.row)">查看</el-button>
            <el-button
              v-if="scope.row.readStatus === 0"
              type="success"
              link
              size="small"
              :icon="Check"
              @click="handleRead(scope.row)"
            >已读</el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 新增通知弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增通知" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">系统通知</el-radio>
            <el-radio :value="2">提醒</el-radio>
            <el-radio :value="3">私信</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="接收者" prop="receiverId">
          <el-input-number v-model="form.receiverId" :min="0" placeholder="0=全体用户" controls-position="right" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入通知内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailVisible" title="通知详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="typeTagMap[detail.type]" size="small">{{ typeMap[detail.type] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="内容">{{ detail.content }}</el-descriptions-item>
        <el-descriptions-item label="阅读状态">
          {{ detail.readStatus === 1 ? '已读' : '未读' }}
        </el-descriptions-item>
        <el-descriptions-item label="阅读时间">{{ detail.readTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, View, Check } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref(null)

const queryParams = ref({
  title: '',
  type: null,
  readStatus: null
})

const form = ref({ title: '', type: 1, receiverId: 0, content: '' })
const detail = ref({})

const formRules = {
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
}

const typeMap = { 1: '系统通知', 2: '提醒', 3: '私信' }
const typeTagMap = { 1: '', 2: 'warning', 3: 'success' }

const hasUnread = computed(() => tableData.value.some(r => r.readStatus === 0))

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (queryParams.value.title) params.title = queryParams.value.title
    if (queryParams.value.type) params.type = queryParams.value.type
    if (queryParams.value.readStatus !== null && queryParams.value.readStatus !== '') params.readStatus = queryParams.value.readStatus
    const res = await request.get('/system/notify/page', { params })
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
  queryParams.value = { title: '', type: null, readStatus: null }
  handleQuery()
}

const openDialog = () => {
  form.value = { title: '', type: 1, receiverId: 0, content: '' }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
  } catch {
    // 表单校验不通过，Element Plus 会自动标红提示，无需额外处理
    return
  }
  saving.value = true
  try {
    await request.post('/system/notify', form.value)
    dialogVisible.value = false
    ElMessage.success('发送成功')
    loadData()
  } catch {
    // 错误已在 request 拦截器中统一提示
  } finally {
    saving.value = false
  }
}

const viewDetail = async (row) => {
  detail.value = row
  detailVisible.value = true
  // 查看时自动标记已读
  if (row.readStatus === 0) {
    try {
      await request.put(`/system/notify/read/${row.id}`)
      loadData()
      window.dispatchEvent(new CustomEvent('notify-unread-changed'))
    } catch { /* ignore */ }
  }
}

const handleRead = async (row) => {
  try {
    await request.put(`/system/notify/read/${row.id}`)
    ElMessage.success('已标记为已读')
    // 从服务器重新加载数据，确保已读状态已持久化
    loadData()
    // 通知 Navbar 刷新未读数
    window.dispatchEvent(new CustomEvent('notify-unread-changed'))
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleReadAll = async () => {
  try {
    await request.put('/system/notify/read-all')
    ElMessage.success('全部已读')
    loadData()
    window.dispatchEvent(new CustomEvent('notify-unread-changed'))
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该通知吗？', '提示', { type: 'warning' })
  try {
    await request.delete(`/system/notify/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    ElMessage.error('删除失败')
  }
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

.toolbar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
