<template>
  <template v-if="isMenuVisible">
    <!-- 当 alwaysShow=0 且只有一个可见子菜单时，直接展示该子菜单 -->
    <MenuItem
      v-if="onlyOneChild"
      :menu="onlyOneChild"
      :parent-path="resolvedPath"
    />

    <!-- 有子菜单：使用 el-sub-menu，图标文字放在 title 插槽 -->
    <el-sub-menu
      v-else-if="hasChildren"
      :index="resolvedPath"
    >
      <template #title>
        <el-icon>
          <component :is="getIcon(menu.icon) || Menu" />
        </el-icon>
        <span>{{ menu.menuName }}</span>
      </template>

      <!-- 递归渲染子菜单，传入当前节点的 resolvedPath 作为父路径 -->
      <MenuItem
        v-for="child in visibleChildren"
        :key="child.id"
        :menu="child"
        :parent-path="resolvedPath"
      />
    </el-sub-menu>

    <!-- 无子菜单：使用 el-menu-item，图标和文字作为默认插槽 -->
    <el-menu-item
      v-else
      :index="resolvedPath"
    >
      <el-icon>
        <component :is="getIcon(menu.icon) || Menu" />
      </el-icon>
      <span>{{ menu.menuName }}</span>
    </el-menu-item>
  </template>
</template>

<script setup>
import { computed } from 'vue'
import { Menu } from '@element-plus/icons-vue'
import { getIcon } from '@/utils/icons'

const props = defineProps({
  menu: { type: Object, required: true },
  /** 父级菜单的完整路径（确保子菜单 index 拼出完整的前端路由路径） */
  parentPath: { type: String, default: '' }
})

/**
 * 计算供 <el-menu router> 导航使用的完整路由路径。
 *
 * 后端 menu.path 可能为：
 *   - 绝对路径：'/system/user'  → 直接使用
 *   - 相对路径：'user'          → 拼上 parentPath 得到 /system/user
 *   - 空值/null                → 目录型菜单不需要导航，叶子菜单 fallback 到 ''
 */
const resolvedPath = computed(() => {
  const raw = (props.menu.path || '').replace(/^\//, '')
  if (!raw) return ''

  // 如果 raw 以父路径开头，说明它是绝对路径，直接用
  if (props.parentPath && raw.startsWith(props.parentPath.replace(/^\//, '') + '/')) {
    return '/' + raw
  }

  // 拼上父路径
  const base = props.parentPath ? props.parentPath.replace(/^\//, '') : ''
  const full = base ? `${base}/${raw}` : raw
  return '/' + full
})

/**
 * 是否总是显示：
 * - alwaysShow 为 0 或 false 表示"不是"
 * - 未设置（null/undefined）或 1 表示"总是"
 */
const isAlwaysShow = computed(() => {
  const val = props.menu.alwaysShow
  return val === undefined || val === null || val === 1 || val === true
})

/** 当前菜单是否在侧边栏可见（hidden 时不渲染但仍可通过路由访问） */
const isMenuVisible = computed(() => props.menu.visible !== 0)

/** 过滤出可见的子菜单 */
const visibleChildren = computed(() => {
  return (props.menu.children || []).filter(c => c.visible !== 0)
})

/** 当前菜单是否包含可见的子菜单 */
const hasChildren = computed(() => {
  return visibleChildren.value.length > 0
})

/**
 * 当「不是」且仅有一个可见子菜单时，返回该子菜单节点，由父组件直接渲染。
 * 子菜单以当前 resolvedPath 作为 parentPath，保证路由路径正确拼接。
 */
const onlyOneChild = computed(() => {
  if (isAlwaysShow.value) return null
  if (visibleChildren.value.length !== 1) return null
  return visibleChildren.value[0]
})
</script>
