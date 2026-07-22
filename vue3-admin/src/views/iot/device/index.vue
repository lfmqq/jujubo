<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item label="设备名称">
          <el-input v-model="queryParams.deviceName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="所属产品">
          <el-select v-model="queryParams.productId" placeholder="请选择" clearable style="width: 180px;">
            <el-option v-for="p in productList" :key="p.id" :label="p.productName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 110px;">
            <el-option label="未激活" :value="0" />
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px;">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openDialog" v-has-perm="'iot:device:add'">注册设备</el-button>
      </div>

      <div v-loading="loading">
        <el-empty v-if="!tableData.length && !loading" description="暂无设备数据" />
        <div class="device-grid">
          <div v-for="row in tableData" :key="row.id" class="device-card">
            <div class="card-header">
              <div class="card-title-row">
                <el-icon class="card-icon" :size="22"><Monitor /></el-icon>
                <span class="card-title" :title="row.deviceName">{{ row.deviceName }}</span>
              </div>
              <el-tag :type="statusTag(row.status)" size="small" effect="light">
                {{ statusLabel(row.status) }}
              </el-tag>
            </div>

            <div class="card-body">
              <div class="card-info">
                <div class="info-row">
                  <span class="info-label">所属产品</span>
                  <span class="info-value" :title="row.productName">{{ row.productName || '-' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">设备标识</span>
                  <span class="info-value" :title="row.deviceKey">{{ row.deviceKey }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">最后上线</span>
                  <span class="info-value">{{ row.lastOnlineTime || '未上线' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">注册时间</span>
                  <span class="info-value">{{ row.createTime }}</span>
                </div>
              </div>
              <div class="card-image">
                <img src="@/assets/product.png" alt="product" />
              </div>
            </div>

            <div class="card-footer">
              <el-button type="primary" plain @click="viewData(row)">数据</el-button>
              <el-button type="warning" plain @click="edit(row)" v-has-perm="'iot:device:edit'">编辑</el-button>
              <el-button type="danger" plain  @click="del(row)" v-has-perm="'iot:device:remove'">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" background @change="loadData"
        />
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑设备' : '注册设备'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="如：1号温度传感器" />
        </el-form-item>
        <el-form-item label="设备标识" prop="deviceKey">
          <el-input v-model="form.deviceKey" placeholder="唯一标识，如设备SN" />
        </el-form-item>
        <el-form-item label="设备密钥" prop="deviceSecret" v-if="!form.id">
          <el-input v-model="form.deviceSecret" placeholder="用于设备认证的密钥" show-password />
        </el-form-item>
        <el-form-item label="所属产品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择" style="width: 100%;">
            <el-option v-for="p in productList" :key="p.id" :label="p.productName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">未激活</el-radio>
            <el-radio :value="1">在线</el-radio>
            <el-radio :value="2">离线</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设备描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入设备描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="save" :loading="saving">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 设备数据弹窗 -->
    <el-dialog v-model="dataVisible" :title="`设备数据 · ${currentDevice?.deviceName || ''}`" width="700px" destroy-on-close>
      <el-table :data="deviceDataList" border stripe v-loading="dataLoading" empty-text="暂无上报数据">
        <el-table-column label="属性名" prop="property_name" width="180" />
        <el-table-column label="属性值" prop="property_value" min-width="200" />
        <el-table-column label="上报时间" prop="report_time" width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, View, Monitor } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const productList = ref([])

const queryParams = ref({ deviceName: '', productId: null, status: null })
const form = ref({ status: 0 })
const formRules = {
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceKey: [{ required: true, message: '请输入设备标识', trigger: 'blur' }],
  productId: [{ required: true, message: '请选择所属产品', trigger: 'change' }]
}

const statusLabel = (s) => ({ 0: '未激活', 1: '在线', 2: '离线' })[s] || '未知'
const statusTag = (s) => ({ 0: 'info', 1: 'success', 2: 'danger' })[s] || 'info'

// 设备数据弹窗
const dataVisible = ref(false)
const dataLoading = ref(false)
const currentDevice = ref(null)
const deviceDataList = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    const q = queryParams.value
    if (q.deviceName) params.deviceName = q.deviceName
    if (q.productId) params.productId = q.productId
    if (q.status !== null && q.status !== '') params.status = q.status
    const res = await request.get('/iot/device/page', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const loadProducts = async () => {
  try {
    const res = await request.get('/iot/product/list')
    productList.value = res.data || []
  } catch { /* ignore */ }
}

const handleQuery = () => { pageNum.value = 1; loadData() }
const handleReset = () => { queryParams.value = { deviceName: '', productId: null, status: null }; handleQuery() }

const openDialog = () => {
  form.value = { status: 0 }
  loadProducts()
  dialogVisible.value = true
}

const edit = async (row) => {
  loadProducts()
  const res = await request.get(`/iot/device/${row.id}`)
  form.value = res.data
  dialogVisible.value = true
}

const viewData = async (row) => {
  currentDevice.value = row
  dataVisible.value = true
  dataLoading.value = true
  try {
    const res = await request.get(`/iot/device/${row.id}/data`)
    deviceDataList.value = res.data || []
  } finally { dataLoading.value = false }
}

const save = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await request.put('/iot/device', form.value)
    } else {
      await request.post('/iot/device', form.value)
    }
    dialogVisible.value = false
    ElMessage.success('保存成功')
    loadData()
  } finally { saving.value = false }
}

const del = async (row) => {
  await ElMessageBox.confirm(`确认删除设备「${row.deviceName}」吗？`, '提示', { type: 'warning' })
  await request.delete(`/iot/device/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadProducts()
  loadData()
})
</script>

<style scoped>
.page-container { height: 100%; }
.search-card :deep(.el-card__body) { padding-bottom: 0; }

.toolbar {
  margin-bottom: 16px;
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.device-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  transition: box-shadow 0.2s;
}

.device-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.card-icon {
  color: #409eff;
  flex-shrink: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-body {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.info-row {
  display: flex;
  align-items: center;
  font-size: 13px;
  line-height: 26px;
}

.info-label {
  color: #909399;
  width: 70px;
  flex-shrink: 0;
}

.info-value {
  color: #606266;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-footer {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

.card-footer .el-button {
  flex: 1;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
