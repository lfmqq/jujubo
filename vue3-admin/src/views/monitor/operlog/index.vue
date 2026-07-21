<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item label="模块标题">
          <el-input v-model="queryParams.title" placeholder="请输入模块标题" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="queryParams.operName" placeholder="请输入操作人" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryParams.businessType" placeholder="全部" clearable style="width: 130px;">
            <el-option v-for="item in businessTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作来源">
          <el-select v-model="queryParams.operatorType" placeholder="全部" clearable style="width: 130px;">
            <el-option v-for="item in operatorTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 110px;">
            <el-option label="成功" :value="0" />
            <el-option label="失败" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :default-time="[new Date(2000, 0, 1, 0, 0, 0), new Date(2000, 0, 1, 23, 59, 59)]"
          />
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
        <el-button type="danger" plain :icon="Delete" @click="handleClean">清空日志</el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="模块" prop="title" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="businessTypeTag(scope.row.businessType)" size="small">
              {{ businessTypeLabel(scope.row.businessType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作人" prop="operName" width="120" show-overflow-tooltip />
        <el-table-column label="操作来源" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.operatorType === 1 ? 'warning' : 'info'" size="small">
              {{ operatorTypeLabel(scope.row.operatorType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="IP地址" prop="operIp" width="140" />
        <el-table-column label="归属地" prop="operLocation" width="90" align="center" />
        <el-table-column label="请求方式" prop="requestMethod" width="90" align="center" />
        <el-table-column label="请求URL" prop="operUrl" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作时间" prop="operTime" width="170" />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" :icon="View" @click="openDetail(scope.row)">详情</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="操作日志详情" width="720px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模块标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ businessTypeLabel(detail.businessType) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作来源">{{ operatorTypeLabel(detail.operatorType) }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detail.operIp }}</el-descriptions-item>
        <el-descriptions-item label="归属地">{{ detail.operLocation }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detail.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="detail.status === 0 ? 'success' : 'danger'" size="small">
            {{ detail.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ detail.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">{{ detail.method }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ detail.operTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">请求参数</el-divider>
      <pre class="log-pre">{{ detail.operParam || '无' }}</pre>

      <el-divider content-position="left">返回参数</el-divider>
      <pre class="log-pre">{{ detail.jsonResult || '无' }}</pre>

      <el-divider v-if="detail.status === 1" content-position="left">错误信息</el-divider>
      <pre v-if="detail.status === 1" class="log-pre error">{{ detail.errorMsg || '无' }}</pre>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete, View } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const dateRange = ref([])

const queryParams = reactive({
  title: '',
  operName: '',
  businessType: null,
  operatorType: null,
  status: null
})

const detailVisible = ref(false)
const detail = reactive({})

const businessTypeOptions = [
  { label: '其他', value: 0 },
  { label: '新增', value: 1 },
  { label: '修改', value: 2 },
  { label: '删除', value: 3 },
  { label: '查询', value: 4 },
  { label: '登录', value: 5 },
  { label: '退出', value: 6 },
  { label: '导出', value: 7 },
  { label: '导入', value: 8 },
  { label: '强退', value: 9 },
  { label: '清空', value: 10 }
]
const operatorTypeOptions = [
  { label: '后台用户', value: 0 },
  { label: '前端用户', value: 1 },
  { label: '手机端用户', value: 2 }
]

const businessTypeLabel = (v) => businessTypeOptions.find(i => i.value === v)?.label || '其他'
const operatorTypeLabel = (v) => operatorTypeOptions.find(i => i.value === v)?.label || '未知'
const businessTypeTag = (v) => {
  if (v === 1) return 'success'
  if (v === 2) return 'warning'
  if (v === 3) return 'danger'
  if (v === 5 || v === 6) return 'info'
  return 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (queryParams.title) params.title = queryParams.title
    if (queryParams.operName) params.operName = queryParams.operName
    if (queryParams.businessType !== null && queryParams.businessType !== '') params.businessType = queryParams.businessType
    if (queryParams.operatorType !== null && queryParams.operatorType !== '') params.operatorType = queryParams.operatorType
    if (queryParams.status !== null && queryParams.status !== '') params.status = queryParams.status
    if (dateRange.value && dateRange.value.length === 2) {
      params.beginTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await request.get('/monitor/operlog/page', { params })
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
  queryParams.title = ''
  queryParams.operName = ''
  queryParams.businessType = null
  queryParams.operatorType = null
  queryParams.status = null
  dateRange.value = []
  handleQuery()
}

const openDetail = async (row) => {
  const res = await request.get(`/monitor/operlog/${row.id}`)
  Object.assign(detail, res.data)
  detailVisible.value = true
}

const del = (row) => {
  ElMessageBox.confirm(`确认删除该条操作日志吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await request.delete(`/monitor/operlog/${row.id}`)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

const handleClean = () => {
  ElMessageBox.confirm('确认清空所有操作日志吗？此操作不可恢复！', '警告', { type: 'warning' })
    .then(async () => {
      await request.delete('/monitor/operlog/clean')
      ElMessage.success('清空成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

<style scoped>
.page-container {
  padding: 16px;
}
.log-pre {
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px 12px;
  max-height: 220px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  line-height: 1.6;
  margin: 0;
}
.log-pre.error {
  background: #fef0f0;
  border-color: #fde2e2;
  color: #f56c6c;
}
</style>
