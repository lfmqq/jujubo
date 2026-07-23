<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item label="任务名称">
          <el-input v-model="queryParams.jobName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 100px;">
            <el-option label="运行" :value="1" />
            <el-option label="暂停" :value="0" />
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
        <el-button type="primary" :icon="Plus" @click="openDialog" v-has-perm="'monitor:job:add'">新增任务</el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="任务名称" prop="jobName" min-width="150" />
        <el-table-column label="任务组" prop="jobGroup" width="100" align="center" />
        <el-table-column label="cron 表达式" prop="cronExpression" width="150" show-overflow-tooltip />
        <el-table-column label="调用目标" prop="invokeTarget" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '运行' : '暂停' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button plain type="success" @click="runJob(row)">执行</el-button>
            <el-button plain v-if="row.status===0" type="primary" @click="resume(row)">恢复</el-button>
            <el-button plain v-else type="warning" @click="pause(row)">暂停</el-button>
            <el-button plain type="primary" @click="edit(row)" v-has-perm="'monitor:job:edit'">编辑</el-button>
            <el-button plain type="danger" @click="del(row)" v-has-perm="'monitor:job:remove'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" background @change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑任务' : '新增任务'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="form.jobName" placeholder="如：每日统计" />
        </el-form-item>
        <el-form-item label="任务组" prop="jobGroup">
          <el-select v-model="form.jobGroup" placeholder="请选择" style="width: 100%;">
            <el-option label="系统组 (SYSTEM)" value="SYSTEM" />
            <el-option label="默认组 (DEFAULT)" value="DEFAULT" />
          </el-select>
        </el-form-item>
        <el-form-item label="cron 表达式" prop="cronExpression">
          <el-input v-model="form.cronExpression" placeholder="如：0 0 2 * * ? (每天凌晨2点)" />
          <div class="cron-hint">
            常用：每5分钟 <code>0 */5 * * * ?</code> | 每小时 <code>0 0 * * * ?</code> | 每天0点 <code>0 0 0 * * ?</code>
          </div>
        </el-form-item>
        <el-form-item label="调用目标" prop="invokeTarget">
          <el-input v-model="form.invokeTarget" placeholder="格式：beanName.methodName" />
          <div class="cron-hint">Spring Bean 名称 + 方法名，如 <code>statisticsService.syncData</code></div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">运行</el-radio>
            <el-radio :value="0">暂停</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
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
import { Search, Refresh, Plus, Edit, Delete, VideoPlay, VideoPause } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const queryParams = ref({ jobName: '', status: null })
const form = ref({ status: 1, jobGroup: 'DEFAULT' })
const formRules = {
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  cronExpression: [{ required: true, message: '请输入 cron 表达式', trigger: 'blur' }],
  invokeTarget: [{ required: true, message: '请输入调用目标', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (queryParams.value.jobName) params.jobName = queryParams.value.jobName
    if (queryParams.value.status !== null && queryParams.value.status !== '') params.status = queryParams.value.status
    const res = await request.get('/monitor/job/page', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleQuery = () => { pageNum.value = 1; loadData() }
const handleReset = () => { queryParams.value = { jobName: '', status: null }; handleQuery() }

const openDialog = () => { form.value = { status: 1, jobGroup: 'DEFAULT' }; dialogVisible.value = true }

const edit = async (row) => {
  const res = await request.get(`/monitor/job/${row.id}`)
  form.value = res.data
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await request.put('/monitor/job', form.value)
    } else {
      await request.post('/monitor/job', form.value)
    }
    dialogVisible.value = false
    ElMessage.success('保存成功')
    loadData()
  } finally { saving.value = false }
}

const pause = async (row) => {
  await request.put(`/monitor/job/pause/${row.id}`)
  ElMessage.success('已暂停')
  loadData()
}

const resume = async (row) => {
  await request.put(`/monitor/job/resume/${row.id}`)
  ElMessage.success('已恢复')
  loadData()
}

const runJob = async (row) => {
  await request.post(`/monitor/job/run/${row.id}`)
  ElMessage.success(`任务「${row.jobName}」已提交执行`)
}

const del = async (row) => {
  await ElMessageBox.confirm(`确认删除任务「${row.jobName}」吗？`, '提示', { type: 'warning' })
  await request.delete(`/monitor/job/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-container { height: 100%; }
.search-card :deep(.el-card__body) { padding-bottom: 0; }
.cron-hint { font-size: 12px; color: #909399; margin-top: 4px; line-height: 1.5; }
.cron-hint code { background: var(--border-light); padding: 1px 5px; border-radius: 3px; font-size: 12px; }
</style>
