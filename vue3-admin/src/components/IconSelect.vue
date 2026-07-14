<template>
  <div class="icon-select">
    <el-input
      v-model="selectedIcon"
      readonly
      placeholder="点击选择图标"
      @click="visible = true"
    >
      <template #prefix>
        <el-icon v-if="selectedIcon">
          <component :is="getIcon(selectedIcon)" />
        </el-icon>
      </template>
      <template #suffix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <el-dialog
      v-model="visible"
      title="选择图标"
      width="760px"
      append-to-body
      destroy-on-close
    >
      <div class="icon-search">
        <el-input
          v-model="search"
          placeholder="搜索图标"
          clearable
          :prefix-icon="Search"
        />
      </div>
      <div class="icon-list">
        <div
          v-for="icon in pagedIcons"
          :key="icon"
          class="icon-item"
          :class="{ active: selectedIcon === toKebabCase(icon) }"
          :title="toKebabCase(icon)"
          @click="select(icon)"
        >
          <el-icon><component :is="icon" /></el-icon>
        </div>
      </div>
      <div class="icon-pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="filteredIcons.length"
          layout="total, prev, pager, next"
          small
        />
      </div>
      <template #footer>
        <el-button @click="visible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { iconNames, getIcon, toKebabCase } from '@/utils/icons'

const props = defineProps({
  modelValue: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const search = ref('')
const page = ref(1)
const pageSize = 70

const selectedIcon = computed({
  get: () => props.modelValue || '',
  set: (val) => emit('update:modelValue', val)
})

const filteredIcons = computed(() => {
  if (!search.value) return iconNames
  const q = search.value.trim().toLowerCase()
  return iconNames.filter(name => {
    const kebab = toKebabCase(name)
    return name.toLowerCase().includes(q) || kebab.includes(q)
  })
})

const pagedIcons = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredIcons.value.slice(start, start + pageSize)
})

watch(search, () => { page.value = 1 })

const select = (icon) => {
  selectedIcon.value = toKebabCase(icon)
  visible.value = false
}
</script>

<style scoped>
.icon-select {
  width: 100%;
}

.icon-search {
  margin-bottom: 16px;
}

.icon-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(54px, 1fr));
  gap: 10px;
  max-height: 360px;
  overflow-y: auto;
  padding: 4px;
}

.icon-item {
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 18px;
}

.icon-item:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
  transform: scale(1.05);
}

.icon-item.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.icon-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
