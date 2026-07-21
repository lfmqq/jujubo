<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item label="产品名称">
          <el-input v-model="queryParams.productName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="产品标识">
          <el-input v-model="queryParams.productKey" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 100px;">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
        <el-button type="primary" :icon="Plus" @click="openDialog" v-has-perm="'iot:product:add'">新增产品</el-button>
      </div>

      <div v-loading="loading">
        <el-empty v-if="!tableData.length && !loading" description="暂无产品数据" />
        <div class="product-grid">
          <div v-for="row in tableData" :key="row.id" class="product-card">
            <div class="card-header">
              <div class="card-title-row">
                <el-icon class="card-icon" :size="22"><Box /></el-icon>
                <span class="card-title" :title="row.productName">{{ row.productName }}</span>
              </div>
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="light">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </div>

            <div class="card-body">
              <div class="card-info">
                <div class="info-row">
                  <span class="info-label">设备类型</span>
                  <span class="info-value">
                    <el-tag size="small" :type="deviceTypeTag(row.deviceType)" effect="plain">
                      {{ deviceTypeLabel(row.deviceType) }}
                    </el-tag>
                  </span>
                </div>
                <div class="info-row">
                  <span class="info-label">通信协议</span>
                  <span class="info-value">{{ row.protocolType?.toUpperCase() }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">数据格式</span>
                  <span class="info-value">{{ row.dataFormat?.toUpperCase() }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">产品标识</span>
                  <span class="info-value" :title="row.productKey">{{ row.productKey }}</span>
                </div>
              </div>
              <div class="card-image">
                <img src="@/assets/product.png" alt="product" />
              </div>
            </div>

            <div class="card-footer">
              <el-button type="primary" plain size="small" :icon="Edit" @click="edit(row)" v-has-perm="'iot:product:edit'">编辑</el-button>
              <el-button type="danger" plain size="small" :icon="Delete" @click="del(row)" v-has-perm="'iot:product:remove'">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑产品' : '新增产品'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="如：温湿度传感器" />
        </el-form-item>
        <el-form-item label="产品标识" prop="productKey">
          <el-input v-model="form.productKey" placeholder="唯一标识，如 temp_sensor_v1" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="form.deviceType" placeholder="请选择" style="width: 100%;">
            <el-option label="传感器 (sensor)" value="sensor" />
            <el-option label="执行器 (actuator)" value="actuator" />
            <el-option label="网关 (gateway)" value="gateway" />
          </el-select>
        </el-form-item>
        <el-form-item label="通信协议" prop="protocolType">
          <el-select v-model="form.protocolType" placeholder="请选择" style="width: 100%;">
            <el-option label="MQTT" value="mqtt" />
            <el-option label="HTTP" value="http" />
            <el-option label="CoAP" value="coap" />
            <el-option label="TCP" value="tcp" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据格式">
          <el-radio-group v-model="form.dataFormat">
            <el-radio value="json">JSON</el-radio>
            <el-radio value="custom">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="产品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入产品描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
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
import { Search, Refresh, Plus, Edit, Delete, Box } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const queryParams = ref({ productName: '', productKey: '', status: null })

const form = ref({ status: 1, dataFormat: 'json', deviceType: 'sensor', protocolType: 'mqtt' })
const formRules = {
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  productKey: [{ required: true, message: '请输入产品标识', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  protocolType: [{ required: true, message: '请选择通信协议', trigger: 'change' }]
}

const deviceTypeLabel = (t) => ({ sensor: '传感器', actuator: '执行器', gateway: '网关' })[t] || t
const deviceTypeTag = (t) => ({ sensor: 'info', actuator: 'warning', gateway: 'success' })[t] || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    const q = queryParams.value
    if (q.productName) params.productName = q.productName
    if (q.productKey) params.productKey = q.productKey
    if (q.status !== null && q.status !== '') params.status = q.status
    const res = await request.get('/iot/product/page', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleQuery = () => { pageNum.value = 1; loadData() }
const handleReset = () => { queryParams.value = { productName: '', productKey: '', status: null }; handleQuery() }

const openDialog = () => {
  form.value = { status: 1, dataFormat: 'json', deviceType: 'sensor', protocolType: 'mqtt' }
  dialogVisible.value = true
}

const edit = async (row) => {
  const res = await request.get(`/iot/product/${row.id}`)
  form.value = res.data
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await request.put('/iot/product', form.value)
    } else {
      await request.post('/iot/product', form.value)
    }
    dialogVisible.value = false
    ElMessage.success('保存成功')
    loadData()
  } finally { saving.value = false }
}

const del = async (row) => {
  await ElMessageBox.confirm(`确认删除产品「${row.productName}」吗？`, '提示', { type: 'warning' })
  await request.delete(`/iot/product/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-container { height: 100%; }
.search-card :deep(.el-card__body) { padding-bottom: 0; }

.toolbar {
  margin-bottom: 16px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.product-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  transition: box-shadow 0.2s;
}

.product-card:hover {
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
