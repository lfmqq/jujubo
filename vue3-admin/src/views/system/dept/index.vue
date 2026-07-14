<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" size="default" @submit.prevent>
        <el-form-item label="部门名称">
          <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable @keyup.enter="handleQuery" />
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
        <el-button type="primary" :icon="Plus" @click="openDialog">新增部门</el-button>
      </div>
      <el-table
        :data="tableData"
        border
        stripe
        row-key="id"
        v-loading="loading"
        :tree-props="{ children: 'children' }"
        default-expand-all
      >
        <el-table-column label="部门名称" prop="deptName" min-width="200" />
        <el-table-column label="排序" prop="sort" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag size="small" :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" :icon="Edit" @click="edit(scope.row)">编辑</el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="del(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑部门' : '新增部门'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择上级部门（空为顶级）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="部门状态">
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
import { ref, onMounted, nextTick } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'

const tableData = ref([])
const deptTree = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const queryParams = ref({
  deptName: ''
})

const form = ref({
  id: null,
  parentId: 0,
  deptName: '',
  sort: 0,
  status: 1
})

const formRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (queryParams.value.deptName) params.deptName = queryParams.value.deptName
    const res = await request.get('/system/dept/tree', { params })
    tableData.value = res.data
    deptTree.value = [{ id: 0, deptName: '顶级部门', children: res.data }]
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  loadData()
}

const handleReset = () => {
  queryParams.value = { deptName: '' }
  loadData()
}

const openDialog = () => {
  form.value = {
    id: null,
    parentId: 0,
    deptName: '',
    sort: 0,
    status: 1
  }
  nextTick(() => formRef.value?.clearValidate())
  dialogVisible.value = true
}

const edit = (row) => {
  form.value = { ...row, parentId: row.parentId || 0 }
  nextTick(() => formRef.value?.clearValidate())
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await request.put('/system/dept', form.value)
    } else {
      await request.post('/system/dept', form.value)
    }
    dialogVisible.value = false
    ElMessage.success('保存成功')
    loadData()
  } finally {
    saving.value = false
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该部门吗？如果有子部门会一并删除。', '提示', { type: 'warning' })
  await request.delete(`/system/dept/${row.id}`)
  ElMessage.success('删除成功')
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

.toolbar {
  margin-bottom: 16px;
}
</style>
