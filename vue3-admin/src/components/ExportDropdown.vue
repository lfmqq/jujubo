<template>
  <el-dropdown trigger="click" @command="handleCommand" :loading="loading">
    <el-button type="success" plain :icon="Download">
      导出 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="xlsx">
          <el-icon><Document /></el-icon> Excel (.xlsx)
        </el-dropdown-item>
        <el-dropdown-item command="pdf">
          <el-icon><Document /></el-icon> PDF (.pdf)
        </el-dropdown-item>
        <el-dropdown-item command="docx">
          <el-icon><Document /></el-icon> Word (.docx)
        </el-dropdown-item>
        <el-dropdown-item command="zip" divided>
          <el-icon><FolderOpened /></el-icon> 打包下载 (.zip)
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, ArrowDown, Document, FolderOpened } from '@element-plus/icons-vue'
import { exportExcel, exportPDF, exportWord, exportZip } from '@/utils/export'

const props = defineProps({
  /** 列定义：[{ prop: 'field', label: '列名' }] */
  columns: { type: Array, required: true },
  /** 导出文件名（不含扩展名） */
  fileName: { type: String, default: 'export' },
  /** 报表标题（PDF/Word 中显示） */
  title: { type: String, default: '数据报表' },
  /**
   * 获取数据的函数，返回 Promise<Array>
   * 若未提供，则使用外部传入的 data prop
   */
  fetchData: { type: Function, default: null },
  /** 如果不需要异步获取，可直接传入 data */
  data: { type: Array, default: () => [] }
})

const emit = defineEmits(['before-export', 'after-export'])

const loading = ref(false)

const handleCommand = async (type) => {
  loading.value = true
  emit('before-export', type)
  try {
    // 优先使用 fetchData 异步获取，否则用 props.data
    const exportData = props.fetchData ? await props.fetchData() : props.data

    if (!exportData || exportData.length === 0) {
      ElMessage.warning('没有可导出的数据')
      return
    }

    const title = `${props.title}（${exportData.length}条）`

    switch (type) {
      case 'xlsx':
        await exportExcel({ columns: props.columns, data: exportData, fileName: props.fileName })
        break
      case 'pdf':
        await exportPDF({ columns: props.columns, data: exportData, fileName: props.fileName, title })
        break
      case 'docx':
        await exportWord({ columns: props.columns, data: exportData, fileName: props.fileName, title })
        break
      case 'zip':
        await exportZip({ columns: props.columns, data: exportData, fileName: props.fileName, title })
        break
    }

    ElMessage.success(`${type.toUpperCase()} 导出成功`)
    emit('after-export', type)
  } catch (err) {
    ElMessage.error('导出失败：' + (err.message || '未知错误'))
    console.error('导出失败：', err)
  } finally {
    loading.value = false
  }
}
</script>
