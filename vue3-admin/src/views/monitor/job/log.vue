<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>调度日志</span>
          <el-button type="danger" :icon="Delete" @click="cleanLog" v-has-perm="'monitor:job:remove'">清空日志</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="任务名称" prop="jobName" min-width="150" />
        <el-table-column label="调用目标" prop="invokeTarget" min-width="180" show-overflow-tooltip />
        <el-table-column label="执行状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" prop="duration" width="100" align="center">
          <template #default="{ row }">{{ row.duration }}ms</template>
        </el-table-column>
        <el-table-column label="执行时间" prop="execTime" width="170" />
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button plain type="primary" @click="viewDetail(row)">详情</el-button>
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

    <!-- 执行详情弹窗 -->
    <el-dialog v-model="detailVisible" title="执行详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="任务名称">{{ currentLog?.jobName }}</el-descriptions-item>
        <el-descriptions-item label="调用目标">{{ currentLog?.invokeTarget }}</el-descriptions-item>
        <el-descriptions-item label="执行状态">
          <el-tag :type="currentLog?.status === 0 ? 'success' : 'danger'" size="small">
            {{ currentLog?.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog?.duration }}ms</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ currentLog?.execTime }}</el-descriptions-item>
        <el-descriptions-item label="异常信息" v-if="currentLog?.errorMsg">
          <span style="color: red;">{{ currentLog.errorMsg }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Delete } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const detailVisible = ref(false)
const currentLog = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    const res = await request.get('/monitor/job/log/page', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const viewDetail = (row) => {
  currentLog.value = row
  detailVisible.value = true
}

const cleanLog = async () => {
  await ElMessageBox.confirm('确认清空所有日志吗？此操作不可恢复。', '警告', { type: 'warning', confirmButtonText: '确认清空' })
  await request.delete('/monitor/job/log/clean')
  ElMessage.success('日志已清空')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.page-container { height: 100%; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
