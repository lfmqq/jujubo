<template>
  <div class="dict-page">
    <!-- ========== 左侧：字典类型列表 ========== -->
    <div class="dict-side">
      <div class="side-header">
        <el-input
          v-model="typeKeyword"
          placeholder="搜索名称"
          clearable
          class="side-search"
          @keyup.enter="onTypeSearch"
          @clear="onTypeSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Plus" class="side-add-btn" @click="openTypeDialog()" />
      </div>
      <div class="side-body" v-loading="typeLoading">
        <div
          v-for="item in typeList"
          :key="item.id"
          class="type-item"
          :class="{ active: selectedType?.id === item.id }"
          @click="selectType(item)"
        >
          <div class="type-item-main">
            <div class="type-item-name" :title="item.typeName">{{ item.typeName }}</div>
            <div class="type-item-code" :title="item.typeCode">{{ item.typeCode }}</div>
          </div>
          <div class="type-item-actions" @click.stop>
            <el-button link type="primary" size="small" @click="openTypeDialog(item)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleTypeDelete(item)">删除</el-button>
          </div>
        </div>
        <el-empty v-if="!typeLoading && typeList.length === 0" description="暂无字典类型" :image-size="60" />
      </div>
    </div>

    <!-- ========== 右侧：字典数据 ========== -->
    <div class="dict-main">
      <el-card v-if="selectedType" shadow="never" class="main-card">
        <!-- 顶部筛选 -->
        <el-form :model="dataQuery" inline class="search-bar">
          <el-form-item label="字典标签">
            <el-input
              v-model="dataQuery.label"
              placeholder="输入字典标签"
              clearable
              style="width: 180px"
              @keyup.enter="loadDataPage(1)"
            />
          </el-form-item>
          <el-form-item label="数据状态">
            <el-select v-model="dataQuery.status" placeholder="选择数据状态" clearable style="width: 150px">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadDataPage(1)">搜索</el-button>
            <el-button :icon="Refresh" @click="resetDataQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 工具栏 -->
        <div class="toolbar">
          <el-button type="primary" :icon="Plus" @click="openDataDialog()">新增</el-button>
          <el-button type="danger" :icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
            批量删除
          </el-button>
        </div>

        <!-- 数据表 -->
        <el-table
          :data="dataList"
          v-loading="dataLoading"
          border
          stripe
          @selection-change="onSelectionChange"
        >
          <el-table-column type="selection" width="45" align="center" />
          <el-table-column prop="id" label="字典编码" min-width="120" show-overflow-tooltip />
          <el-table-column prop="label" label="字典标签" min-width="120" show-overflow-tooltip />
          <el-table-column prop="value" label="字典键值" min-width="100" show-overflow-tooltip />
          <el-table-column prop="sort" label="字典排序" width="90" align="center" />
          <el-table-column label="颜色类型" width="120" align="center">
            <template #default="{ row }">
              <span v-if="row.listClass" class="color-tag">
                <span class="color-dot" :class="`color-dot--${row.listClass}`" />
                {{ listClassLabel(row.listClass) }}
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="cssClass" label="CSS Class" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.cssClass">{{ row.cssClass }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="dark">
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" plain @click="openDataDialog(row)">修改</el-button>
              <el-button type="danger" plain @click="handleDataDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div v-if="dataTotal > 0" class="pagination-wrap">
          <span class="total-text">共 {{ dataTotal }} 条</span>
          <el-pagination
            background
            layout="sizes, prev, pager, next, jumper"
            :total="dataTotal"
            v-model:current-page="dataQuery.pageNum"
            v-model:page-size="dataQuery.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadDataPage()"
            @size-change="loadDataPage(1)"
          />
        </div>
      </el-card>

      <el-empty v-else description="请选择左侧字典类型查看数据" :image-size="100" />
    </div>

    <!-- ========== 字典类型弹窗 ========== -->
    <el-dialog
      v-model="typeDialogVisible"
      :title="typeDialogTitle"
      width="500px"
      destroy-on-close
      append-to-body
    >
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="字典名称" prop="typeName">
          <el-input v-model="typeForm.typeName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="typeCode">
          <el-input v-model="typeForm.typeCode" placeholder="请输入字典编码" :disabled="!!typeForm.id" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">开启</el-radio>
            <el-radio :value="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="typeSubmitting" @click="submitTypeForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- ========== 字典数据弹窗 ========== -->
    <el-dialog
      v-model="dataDialogVisible"
      :title="dataDialogTitle"
      width="560px"
      destroy-on-close
      append-to-body
    >
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
        <el-form-item label="字典类型">
          <el-tag>{{ selectedType?.typeName }}（{{ selectedType?.typeCode }}）</el-tag>
        </el-form-item>
        <el-form-item label="字典标签" prop="label">
          <el-input v-model="dataForm.label" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典键值" prop="value">
          <el-input v-model="dataForm.value" placeholder="请输入字典键值" />
        </el-form-item>
        <el-form-item label="字典排序" prop="sort">
          <el-input-number v-model="dataForm.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="颜色类型">
          <el-select v-model="dataForm.listClass" placeholder="请选择颜色类型" clearable style="width: 200px">
            <el-option
              v-for="opt in colorOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            >
              <span class="color-dot" :class="`color-dot--${opt.value}`" />
              <span style="margin-left: 6px">{{ opt.label }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="CSS Class">
          <el-input v-model="dataForm.cssClass" placeholder="请输入 CSS Class" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">开启</el-radio>
            <el-radio :value="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataForm.remark" type="textarea" :rows="3" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dataSubmitting" @click="submitDataForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Delete, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// ========== 字典类型 ==========
const typeLoading = ref(false)
const typeList = ref([])
const typeKeyword = ref('')
const selectedType = ref(null)

const colorOptions = [
  { value: 'primary', label: '主要' },
  { value: 'success', label: '成功' },
  { value: 'warning', label: '警告' },
  { value: 'danger', label: '危险' },
  { value: 'info', label: '信息' },
]
const listClassLabel = (val) => colorOptions.find((o) => o.value === val)?.label || val

const loadTypeList = async () => {
  typeLoading.value = true
  try {
    const res = await request.get('/system/dict/type/list', {
      params: { keyword: typeKeyword.value || undefined },
    })
    typeList.value = res.data || []
    if (typeList.value.length > 0 && !selectedType.value) {
      selectType(typeList.value[0])
    } else if (selectedType.value) {
      const updated = typeList.value.find((t) => t.id === selectedType.value.id)
      if (updated) selectedType.value = updated
      else if (typeList.value.length > 0) selectType(typeList.value[0])
    }
  } finally {
    typeLoading.value = false
  }
}

// 搜索类型
const onTypeSearch = () => {
  selectedType.value = null
  loadTypeList()
}

// 类型弹窗
const typeDialogVisible = ref(false)
const typeDialogTitle = ref('新增')
const typeSubmitting = ref(false)
const typeFormRef = ref(null)
const typeForm = reactive({ id: undefined, typeName: '', typeCode: '', status: 1, remark: '' })
const typeRules = {
  typeName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  typeCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
}

const openTypeDialog = (row) => {
  if (row?.id) {
    typeDialogTitle.value = '修改'
    Object.assign(typeForm, { id: row.id, typeName: row.typeName, typeCode: row.typeCode, status: row.status ?? 1, remark: row.remark })
  } else {
    typeDialogTitle.value = '新增'
    Object.assign(typeForm, { id: undefined, typeName: '', typeCode: '', status: 1, remark: '' })
  }
  typeDialogVisible.value = true
}

const submitTypeForm = async () => {
  const el = typeFormRef.value
  if (!el) return
  const valid = await el.validate().catch(() => false)
  if (!valid) return
  typeSubmitting.value = true
  try {
    const data = { ...typeForm }
    if (data.id) {
      await request.put('/system/dict/type', data)
      ElMessage.success('修改成功')
    } else {
      await request.post('/system/dict/type', data)
      ElMessage.success('新增成功')
    }
    typeDialogVisible.value = false
    await loadTypeList()
  } finally {
    typeSubmitting.value = false
  }
}

const handleTypeDelete = (row) => {
  ElMessageBox.confirm(
    `确认删除字典类型「${row.typeName}」吗？该类型下的所有字典数据也会被删除。`,
    '确认删除',
    { type: 'warning' }
  )
    .then(async () => {
      await request.delete(`/system/dict/type/${row.id}`)
      ElMessage.success('删除成功')
      if (selectedType.value?.id === row.id) {
        selectedType.value = null
        dataList.value = []
        dataTotal.value = 0
      }
      await loadTypeList()
    })
    .catch(() => {})
}

// ========== 选中类型 & 字典数据 ==========
const dataLoading = ref(false)
const dataList = ref([])
const dataTotal = ref(0)
const dataQuery = reactive({ pageNum: 1, pageSize: 10, typeCode: '', label: '', status: null })
const selectedIds = ref([])

const selectType = (row) => {
  selectedType.value = row
  dataQuery.typeCode = row.typeCode
  dataQuery.label = ''
  dataQuery.status = null
  dataQuery.pageNum = 1
  loadDataPage()
}

const loadDataPage = async (page) => {
  if (!dataQuery.typeCode) return
  if (page) dataQuery.pageNum = page
  dataLoading.value = true
  try {
    const res = await request.get('/system/dict/data/page', { params: dataQuery })
    dataList.value = res.data.records || []
    dataTotal.value = res.data.total || 0
  } finally {
    dataLoading.value = false
  }
}

const resetDataQuery = () => {
  dataQuery.label = ''
  dataQuery.status = null
  loadDataPage(1)
}

const onSelectionChange = (rows) => {
  selectedIds.value = rows.map((r) => r.id)
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条字典数据吗？`, '确认删除', {
    type: 'warning',
  })
    .then(async () => {
      await request.delete('/system/dict/data/batch', { data: selectedIds.value })
      ElMessage.success('删除成功')
      selectedIds.value = []
      loadDataPage()
    })
    .catch(() => {})
}

// 数据弹窗
const dataDialogVisible = ref(false)
const dataDialogTitle = ref('新增')
const dataSubmitting = ref(false)
const dataFormRef = ref(null)
const dataForm = reactive({
  id: undefined,
  typeCode: '',
  label: '',
  value: '',
  sort: 0,
  cssClass: '',
  listClass: '',
  status: 1,
  remark: '',
})
const dataRules = {
  label: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  value: [{ required: true, message: '请输入字典键值', trigger: 'blur' }],
}

const openDataDialog = (row) => {
  if (!selectedType.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  if (row?.id) {
    dataDialogTitle.value = '修改'
    Object.assign(dataForm, {
      id: row.id,
      typeCode: row.typeCode,
      label: row.label,
      value: row.value,
      sort: row.sort ?? 0,
      cssClass: row.cssClass,
      listClass: row.listClass,
      status: row.status ?? 1,
      remark: row.remark,
    })
  } else {
    dataDialogTitle.value = '新增'
    Object.assign(dataForm, {
      id: undefined,
      typeCode: selectedType.value.typeCode,
      label: '',
      value: '',
      sort: 0,
      cssClass: '',
      listClass: '',
      status: 1,
      remark: '',
    })
  }
  dataDialogVisible.value = true
}

const submitDataForm = async () => {
  const el = dataFormRef.value
  if (!el) return
  const valid = await el.validate().catch(() => false)
  if (!valid) return
  dataSubmitting.value = true
  try {
    const data = { ...dataForm, typeCode: selectedType.value.typeCode }
    if (data.id) {
      await request.put('/system/dict/data', data)
      ElMessage.success('修改成功')
    } else {
      await request.post('/system/dict/data', data)
      ElMessage.success('新增成功')
    }
    dataDialogVisible.value = false
    loadDataPage()
  } finally {
    dataSubmitting.value = false
  }
}

const handleDataDelete = (row) => {
  ElMessageBox.confirm(`确认删除字典数据「${row.label}」吗？`, '确认删除', { type: 'warning' })
    .then(async () => {
      await request.delete(`/system/dict/data/${row.id}`)
      ElMessage.success('删除成功')
      loadDataPage()
    })
    .catch(() => {})
}

// ========== 初始化 ==========
onMounted(() => {
  loadTypeList()
})
</script>

<style scoped>
.dict-page {
  display: flex;
  gap: 16px;
  align-items: stretch;
}

/* ========== 左侧 ========== */
.dict-side {
  width: 240px;
  flex-shrink: 0;
  position: sticky;
  top: 16px;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 80px);
  background: var(--card-bg);
  border: 1px solid var(--border-light);
  border-radius: 4px;
  overflow: hidden;
  box-sizing: border-box;
}
.side-header {
  display: flex;
  align-items: center;
  margin-top: 10px;
  gap: 8px;
  padding: 0 12px 12px;
  border-bottom: 1px solid var(--border-light);
  box-sizing: border-box;
  width: 100%;
}
.side-header :deep(.side-search) {
  flex: 1 1 0;
  min-width: 0;
  width: 0;
}
.side-header :deep(.side-search .el-input__wrapper) {
  padding: 0 11px;
  box-shadow: 0 0 0 1px var(--border-light) inset;
}
.side-header :deep(.side-search .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}
.side-header :deep(.side-search .el-input__inner) {
  height: 30px;
  line-height: 30px;
}
.side-header :deep(.side-add-btn) {
  flex: 0 0 30px;
  width: 30px !important;
  height: 30px;
  padding: 0;
  min-width: 30px;
}
.side-body {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}
.type-item {
  position: relative;
  padding: 10px 12px;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: background 0.2s, border-color 0.2s;
}
.type-item:hover {
  background: var(--el-fill-color-light);
}
.type-item.active {
  background: var(--el-color-primary-light-9);
  border-left-color: var(--color-primary);
}
.type-item-main {
  padding-right: 4px;
}
.type-item-name {
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}
.type-item.active .type-item-name {
  color: var(--color-primary);
  font-weight: 600;
}
.type-item-code {
  margin-top: 2px;
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}
.type-item-actions {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: none;
  padding-left: 12px;
  background: inherit;
}
.type-item:hover .type-item-actions,
.type-item.active .type-item-actions {
  display: flex;
  gap: 2px;
}

/* ========== 右侧 ========== */
.dict-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.main-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.main-card :deep(.el-card__body) {
  flex: 1;
  padding: 0 16px 16px;
}
.search-bar {
  margin-bottom: 16px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.pagination-wrap {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 16px;
}
.total-text {
  font-size: 13px;
  color: var(--text-secondary);
}
.text-muted {
  color: var(--text-secondary);
}

/* ========== 颜色 ========== */
.color-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  vertical-align: middle;
}
.color-dot--primary { background: var(--el-color-primary); }
.color-dot--success { background: var(--el-color-success); }
.color-dot--warning { background: var(--el-color-warning); }
.color-dot--danger  { background: var(--el-color-danger); }
.color-dot--info    { background: var(--el-color-info); }

.color-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-regular);
}
</style>
