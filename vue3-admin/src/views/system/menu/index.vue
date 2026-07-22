<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" size="default" @submit.prevent>
        <el-form-item label="菜单名称">
          <el-input v-model="queryParams.menuName" placeholder="请输入菜单名称" clearable @keyup.enter="handleQuery" />
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
        <el-button type="primary" :icon="Plus" @click="openDialog">新增菜单</el-button>
        <el-button :icon="isExpandAll ? Fold : Expand" @click="toggleExpandAll">
          {{ isExpandAll ? '折叠全部' : '展开全部' }}
        </el-button>
      </div>
      <el-table
        ref="tableRef"
        :data="tableData"
        border
        stripe
        row-key="id"
        v-loading="loading"
        :tree-props="{ children: 'children' }"
      >
        <el-table-column label="菜单名称" prop="menuName" min-width="180" />
        <el-table-column label="图标" width="70" align="center">
          <template #default="scope">
            <el-icon v-if="scope.row.icon" :size="18">
              <component :is="getIcon(scope.row.icon)" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column label="路由地址" prop="path" width="180" />
        <el-table-column label="组件路径" prop="component" width="200" show-overflow-tooltip />
        <el-table-column label="权限标识" prop="perms" width="150" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="scope">
            <el-tag size="small" :type="scope.row.type === 0 ? 'primary' : scope.row.type === 1 ? 'success' : 'info'">
              {{ scope.row.type === 0 ? '目录' : scope.row.type === 1 ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-switch
              :model-value="scope.row.status == null || scope.row.status !== 0"
              active-text="启用"
              inactive-text="关闭"
              inline-prompt
              size="small"
              @change="toggleStatus(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="scope">
            <el-button plain type="primary" @click="addChild(scope.row)">新增</el-button>
            <el-button plain type="warning" @click="edit(scope.row)">编辑</el-button>
            <el-button plain type="danger" @click="del(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜单' : '新增菜单'" width="580px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="父级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTree"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            placeholder="请选择父级菜单（空为顶级）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="0">目录</el-radio>
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">开启</el-radio>
            <el-radio :value="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路由地址" v-if="form.type !== 2">
          <el-input v-model="form.path" placeholder="请输入路由地址" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.type === 1">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="权限标识" v-if="form.type !== 0">
          <el-input v-model="form.perms" placeholder="如: system:user:list" />
        </el-form-item>
        <el-form-item label="菜单图标" v-if="form.type !== 2">
          <IconSelect v-model="form.icon" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item v-if="form.type !== 2">
          <template #label>
            <span>
              显示状态
              <el-tooltip content="选择隐藏时，路由将不会出现在侧边栏，但仍然可以访问" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </span>
          </template>
          <el-radio-group v-model="form.visible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.type !== 2">
          <template #label>
            <span>
              总是显示
              <el-tooltip content="选择不是时，当该菜单只有一个子菜单时，不展示自己，直接展示子菜单" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </span>
          </template>
          <el-radio-group v-model="form.alwaysShow">
            <el-radio :value="1">总是</el-radio>
            <el-radio :value="0">不是</el-radio>
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
import { Search, Refresh, Plus, Edit, Delete, Fold, Expand, QuestionFilled } from '@element-plus/icons-vue'
import IconSelect from '@/components/IconSelect.vue'
import { useMenuStore } from '@/stores/menu'
import { getIcon } from '@/utils/icons'

const menuStore = useMenuStore()



const tableData = ref([])
const tableRef = ref(null)
const isExpandAll = ref(false)
const menuTree = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const queryParams = ref({
  menuName: ''
})
const form = ref({
  id: null,
  parentId: 0,
  menuName: '',
  path: '',
  component: '',
  perms: '',
  type: 1,
  icon: '',
  sort: 0,
  visible: 1,
  alwaysShow: 1,
  status: 1
})

const formRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

const loadData = async () => {
  isExpandAll.value = false
  loading.value = true
  try {
    const params = {}
    if (queryParams.value.menuName) params.menuName = queryParams.value.menuName
    const res = await request.get('/system/menu/tree', { params })
    tableData.value = res.data
    menuTree.value = [{ id: 0, menuName: '顶级菜单', children: res.data }]
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  loadData()
}

const handleReset = () => {
  queryParams.value = { menuName: '' }
  loadData()
}

const expandRows = (rows, expanded) => {
  rows.forEach(row => {
    if (row.children?.length) {
      tableRef.value?.toggleRowExpansion(row, expanded)
      expandRows(row.children, expanded)
    }
  })
}

const toggleExpandAll = () => {
  expandRows(tableData.value, !isExpandAll.value)
  isExpandAll.value = !isExpandAll.value
}

const openDialog = () => {
  form.value = {
    id: null,
    parentId: 0,
    menuName: '',
    path: '',
    component: '',
    perms: '',
    type: 1,
    icon: '',
    sort: 0,
    visible: 1,
    alwaysShow: 1,
    status: 1
  }
  nextTick(() => formRef.value?.clearValidate())
  dialogVisible.value = true
}

const addChild = (row) => {
  // 如果当前是按钮，新增的父级就是它的上一级
  // 如果是目录/菜单，新增的父级就是当前行
  const parentId = row.type === 2 ? row.parentId : row.id
  form.value = {
    id: null,
    parentId: parentId,
    menuName: '',
    path: '',
    component: '',
    perms: '',
    type: 1,
    icon: '',
    sort: 0,
    visible: 1,
    alwaysShow: 1,
    status: 1
  }
  nextTick(() => formRef.value?.clearValidate())
  dialogVisible.value = true
}

const edit = (row) => {
  form.value = { ...row, parentId: row.parentId || 0, status: row.status ?? 1 }
  nextTick(() => formRef.value?.clearValidate())
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await request.put('/system/menu', form.value)
    } else {
      await request.post('/system/menu', form.value)
    }
    dialogVisible.value = false
    ElMessage.success('保存成功')
    await loadData()
    await menuStore.fetchMenus()
  } finally {
    saving.value = false
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该菜单吗？如果有子菜单会一并删除。', '提示', { type: 'warning' })
  await request.delete(`/system/menu/${row.id}`)
  ElMessage.success('删除成功')
  await loadData()
  await menuStore.fetchMenus()
}

/** 快捷切换菜单启用/禁用状态（null 视为启用，走专用接口确保更新） */
const toggleStatus = async (row) => {
  const newStatus = (row.status == null || row.status !== 0) ? 0 : 1
  try {
    await request.put('/system/menu/toggle-status', { id: row.id, status: newStatus })
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已关闭')
    await menuStore.fetchMenus()
  } catch (e) {
    console.error('切换菜单状态失败', e)
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
}
</style>
