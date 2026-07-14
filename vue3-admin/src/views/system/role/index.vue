<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="角色标识">
          <el-input v-model="queryParams.roleCode" placeholder="请输入角色标识" clearable @keyup.enter="handleQuery" />
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
        <el-button type="primary" :icon="Plus" @click="openDialog">新增角色</el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="角色名称" prop="roleName" min-width="120" />
        <el-table-column label="角色标识" prop="roleCode" min-width="120" />
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" :icon="Edit" @click="edit(scope.row)">编辑</el-button>
            <el-button type="success" link size="small" :icon="Key" @click="assignMenu(scope.row)">分配菜单</el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="del(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '新增角色'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色标识" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="save" :loading="saving">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单弹窗 -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="500px" destroy-on-close>
      <el-tree
        ref="treeRef"
        :key="treeKey"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="defaultCheckedKeys"
        :props="{ children: 'children', label: 'menuName' }"
        highlight-current
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveMenuAssign" :loading="menuSaving">保存分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Key } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const saving = ref(false)
const menuSaving = ref(false)
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const formRef = ref(null)
const treeRef = ref(null)
const currentRoleId = ref(null)
const menuTree = ref([])
const defaultCheckedKeys = ref([])
const treeKey = ref(0)

const queryParams = ref({
  roleName: '',
  roleCode: ''
})

const form = ref({ id: null, roleName: '', roleCode: '' })
const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (queryParams.value.roleName) params.roleName = queryParams.value.roleName
    if (queryParams.value.roleCode) params.roleCode = queryParams.value.roleCode
    const res = await request.get('/system/role/page', { params })
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
  queryParams.value = { roleName: '', roleCode: '' }
  handleQuery()
}

const openDialog = () => {
  form.value = { id: null, roleName: '', roleCode: '' }
  dialogVisible.value = true
}

const edit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await request.put('/system/role', form.value)
    } else {
      await request.post('/system/role', form.value)
    }
    dialogVisible.value = false
    ElMessage.success('保存成功')
    loadData()
  } finally {
    saving.value = false
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该角色吗？', '提示', { type: 'warning' })
  await request.delete(`/system/role/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

const assignMenu = async (row) => {
  currentRoleId.value = row.id
  menuDialogVisible.value = false
  defaultCheckedKeys.value = []
  menuTree.value = []
  treeKey.value++
  await nextTick()
  const [treeRes, checkRes] = await Promise.all([
    request.get('/system/menu/tree'),
    request.get(`/system/role/getMenuIds/${row.id}`)
  ])
  menuTree.value = treeRes.data
  defaultCheckedKeys.value = checkRes.data || []
  menuDialogVisible.value = true
}

const saveMenuAssign = async () => {
  // 保存所有已勾选的节点 ID（含父目录/菜单 + 子按钮权限）
  const selectedIds = treeRef.value.getCheckedKeys()
  menuSaving.value = true
  try {
    await request.post(`/system/role/assignMenu?roleId=${currentRoleId.value}`, selectedIds)
    menuDialogVisible.value = false
    ElMessage.success('权限分配成功')
  } finally {
    menuSaving.value = false
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
</style>
