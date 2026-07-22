<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>代码生成</span>
          <el-button type="primary" :icon="Plus" @click="showDbDialog">导入表</el-button>
        </div>
      </template>

      <el-table :data="tableList" border stripe v-loading="loading" empty-text="暂无已导入的表，请先导入数据库表">
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="表名" prop="tableName" width="200" />
        <el-table-column label="表描述" prop="tableComment" min-width="180" show-overflow-tooltip />
        <el-table-column label="实体类名" prop="className" width="160" />
        <el-table-column label="功能名" prop="functionName" width="140" />
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button plain type="primary" @click="preview(row)">预览</el-button>
            <el-button plain type="success" @click="download(row)">下载</el-button>
            <el-button plain type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 导入数据库表弹窗 -->
    <el-dialog v-model="dbDialogVisible" title="导入数据库表" width="750px" destroy-on-close>
      <el-alert title="仅显示非系统表（不含 sys_ / gen_ 前缀）" type="info" :closable="false" style="margin-bottom: 12px;" />
      <el-table :data="dbTableList" border stripe v-loading="dbLoading" max-height="400" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="表名" prop="table_name" width="220" show-overflow-tooltip />
        <el-table-column label="表描述" prop="table_comment" min-width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" prop="create_time" width="170" />
      </el-table>
      <template #footer>
        <el-button @click="dbDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="importTables" :loading="importing" :disabled="selectedTables.length === 0">
          导入选中表 ({{ selectedTables.length }})
        </el-button>
      </template>
    </el-dialog>

    <!-- 预览代码弹窗 -->
    <el-dialog v-model="previewVisible" title="代码预览" width="900px" destroy-on-close>
      <el-tabs v-model="previewTab" type="card">
        <el-tab-pane v-for="(code, name) in previewData" :key="name" :label="name" :name="name">
          <div class="code-preview">
            <pre><code>{{ code }}</code></pre>
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="previewVisible = false">关 闭</el-button>
        <el-button type="success" :icon="Download" @click="download(previewTable)">下载代码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Download, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const tableList = ref([])

// 数据库表弹窗
const dbDialogVisible = ref(false)
const dbLoading = ref(false)
const dbTableList = ref([])
const selectedTables = ref([])
const importing = ref(false)

// 预览弹窗
const previewVisible = ref(false)
const previewTab = ref('')
const previewData = ref({})
const previewTable = ref(null)

const loadTableList = async () => {
  loading.value = true
  try {
    const res = await request.get('/tool/gen/list')
    tableList.value = res.data || []
  } finally { loading.value = false }
}

const showDbDialog = async () => {
  dbDialogVisible.value = true
  dbLoading.value = true
  try {
    const res = await request.get('/tool/gen/db/list')
    dbTableList.value = res.data || []
  } finally { dbLoading.value = false }
}

const handleSelectionChange = (val) => {
  selectedTables.value = val
}

const importTables = async () => {
  importing.value = true
  let success = 0
  try {
    for (const row of selectedTables.value) {
      try {
        await request.post('/tool/gen/import', { tableName: row.table_name })
        success++
      } catch { /* 重复导入跳过 */ }
    }
    dbDialogVisible.value = false
    ElMessage.success(`成功导入 ${success} 张表`)
    loadTableList()
  } finally { importing.value = false }
}

const preview = async (row) => {
  previewTable.value = row
  previewVisible.value = true
  try {
    const res = await request.get(`/tool/gen/preview/${row.id}`)
    previewData.value = res.data || {}
    const keys = Object.keys(res.data || {})
    if (keys.length > 0) previewTab.value = keys[0]
  } catch { ElMessage.error('预览失败') }
}

const download = async (row) => {
  try {
    const res = await request.get(`/tool/gen/download/${row.id}`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = `${row.className || 'code'}.zip`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch { ElMessage.error('下载失败') }
}

const del = async (row) => {
  await ElMessageBox.confirm(`确认删除「${row.tableName}」吗？`, '提示', { type: 'warning' })
  await request.delete(`/tool/gen/${row.id}`)
  ElMessage.success('删除成功')
  loadTableList()
}

onMounted(loadTableList)
</script>

<style scoped>
.page-container { height: 100%; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.code-preview { background: #1e1e1e; border-radius: 6px; padding: 16px; max-height: 480px; overflow: auto; }
.code-preview pre { margin: 0; }
.code-preview code { color: #d4d4d4; font-size: 13px; line-height: 1.6; font-family: 'Cascadia Code', 'Fira Code', monospace; white-space: pre; }
</style>
