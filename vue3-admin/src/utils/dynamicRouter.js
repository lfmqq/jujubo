/**
 * 动态路由工具 — 参考芋道源码设计
 *
 * 核心思路：
 * 1. 通过 import.meta.glob 自动收集 views 目录下所有 .vue 组件
 * 2. 将后端返回的菜单树（含 component 字段）映射为 Vue Router 路由
 * 3. 支持 Layout 容器、目录嵌套、首页自动解析
 */

// 目录型菜单的容器组件：只负责渲染 <router-view>，不能复用主 Layout，否则会出现嵌套布局
const ParentView = () => import('@/components/ParentView.vue')

// import.meta.glob 不支持 @/ 别名，必须用相对路径
// 当前文件: src/utils/dynamicRouter.js → 相对于 views: ../views/
const viewModules = import.meta.glob('../views/**/*.vue')

// 构建「组件路径 → 模块」的快速查找表（兼容各种 component 写法）
function buildLookupTable() {
  const map = {}
  for (const key of Object.keys(viewModules)) {
    // key 格式: ../views/system/user/index.vue
    const normalized = key
      .replace('../views/', '')       // → system/user/index.vue
      .replace('.vue', '')             // → system/user/index

    // 注册多种匹配形式
    map[normalized] = viewModules[key]                          // system/user/index
    map[normalized.replace(/\/index$/, '')] = viewModules[key]  // system/user (无 index 后缀)
  }
  return map
}

const viewLookup = buildLookupTable()

/**
 * 根据组件路径字符串解析出实际的 Vue 异步组件
 *
 * 数据库 component 字段示例：
 *   - 'Layout'              → Layout 容器
 *   - 'system/user'         → 匹配 ../views/system/user/index.vue
 *   - '/system/notify'      → 去掉前导 / 后匹配
 *   - 'home/index'          → 匹配 ../views/home/index.vue
 *   - null/空               → 目录无组件时，尝试根据 path 自动推导
 */
export function resolveComponent(componentPath) {
  if (!componentPath) return null

  // Layout / ParentView 特殊处理：数据库 component 字段为 'Layout' 的目录容器，
  // 统一映射为 ParentView（主 Layout 已在 router/index.js 中作为根容器）
  if (componentPath === 'Layout') return ParentView

  // 去掉可能的前导 /
  const clean = componentPath.replace(/^\//, '')

  // 1. 精确匹配
  if (viewLookup[clean]) return viewLookup[clean]

  // 2. 尝试拼接 /index 后再匹配（如 'system/user' → 'system/user/index'）
  if (viewLookup[`${clean}/index`]) return viewLookup[`${clean}/index`]

  console.warn(`[dynamicRouter] 未找到组件: ${componentPath}`)
  return null
}

/**
 * 递归查找当前节点的第一个可访问叶子路径（用于 redirect）
 */
function findFirstLeafPath(node) {
  if (node.children?.length) {
    return findFirstLeafPath(node.children[0])
  }
  return node.path
}

/**
 * 将绝对路径转为相对父路径的路径（用于 Vue Router 嵌套子路由）
 *
 * 例：
 *   fullPath = 'system/user', parentPath = 'system' → 'user'
 *   fullPath = 'home',        parentPath = ''      → 'home'
 */
function getRelativePath(fullPath, parentPath) {
    if (!parentPath) return fullPath
    const prefix = parentPath.endsWith('/') ? parentPath : parentPath + '/'
    if (fullPath.startsWith(prefix)) {
        return fullPath.slice(prefix.length)
    }
    return fullPath
}

/**
 * 将单个菜单节点转为 Vue Router 路由对象
 *
 * @param {Object} menu  - 后端菜单节点
 * @param {string} parentPath - 父级路由 path（已去掉前导 /）
 * @returns {Object|null} 路由配置对象
 *
 * 转换规则：
 * - type=2 (按钮)：跳过，不生成路由
 * - type=0 (目录) + 有子菜单 → 作为父路由，使用 ParentView 组件，redirect 到首个子路由
 * - type=0 (目录) + 无子菜单 → 作为独立页面（如「首页」），自动根据 path 推断组件
 * - type=1 (菜单) → 叶子路由，使用 component 字段指定的组件
 */
function menuToRoute(menu, parentPath = '') {
    if (menu.type === 2) return null // 按钮不生成路由

    const fullPath = (menu.path || '').replace(/^\//, '')
    const routePath = getRelativePath(fullPath, parentPath)

    // 构建完整的组件查找路径（父路径 + 当前路径），
    // 用于后端 path 字段为相对路径时（如子菜单 path='user' 而非 'system/user'）也能定位到 views 文件
    // 若 fullPath 已经以 parentPath/ 开头（绝对路径），则直接用 fullPath
    const combinedPath = parentPath && !fullPath.startsWith(parentPath + '/')
        ? `${parentPath}/${fullPath}`
        : fullPath

    // 递归处理子菜单，传入当前节点的路径（去掉前导 /）作为父路径
    const children = menu.children?.length
        ? menu.children.map((child) => menuToRoute(child, combinedPath)).filter(Boolean)
        : []

    // 确定组件
    let component = null

    if (menu.component === 'Layout') {
        component = ParentView
    } else if (menu.component) {
        // 有明确 component 字段（可能是相对路径，也可能是绝对路径）
        component = resolveComponent(menu.component)
    } else if (children.length > 0) {
        // 目录容器，用 ParentView 而不是主 Layout
        component = ParentView
    } else {
        // 无 component、无子菜单的目录/菜单：根据 combinedPath 自动推断组件
        // 优先用 combinedPath（如 'system/user'），确保能定位到嵌套在目录下的 views 文件
        if (combinedPath) {
            component = resolveComponent(`${combinedPath}/index`) || resolveComponent(combinedPath)
        }
        // 兜底：如果 combinedPath 也找不到，尝试只用 fullPath（兼容旧数据）
        if (!component && fullPath && fullPath !== combinedPath) {
            component = resolveComponent(`${fullPath}/index`) || resolveComponent(fullPath)
        }
    }

    // 没有组件也没有子路由，不生成
    if (!component && !children.length) {
        console.warn(`[dynamicRouter] 菜单「${menu.menuName}」无组件且无子菜单，已跳过`)
        return null
    }

    const route = {
        path: routePath,
        name: menu.menuName,
        component,
        meta: {
            title: menu.menuName,
            icon: menu.icon,
            perms: menu.perms || '',
            type: menu.type,
            menuId: menu.id,
        },
    }

    if (children.length) {
        route.children = children
        route.redirect = findFirstLeafPath(children[0])
    }

    return route
}

/**
 * 将后端菜单树转为扁平的一级路由数组（用于 addRoute 添加到 Layout 的 children 中）
 *
 * @param {Array} menus - 后端返回的菜单树
 * @returns {Array} 路由配置数组
 */
export function generateRoutes(menus) {
  if (!menus?.length) return []
  return menus.map(m => menuToRoute(m)).filter(Boolean)
}

/**
 * 从路由树中递归收集所有路由的 name（用于 tagsView / keep-alive）
 */
export function collectRouteNames(routes) {
  const names = []
  const walk = (list) => {
    list.forEach((r) => {
      if (r.name) names.push(r.name)
      if (r.children?.length) walk(r.children)
    })
  }
  walk(routes)
  return names
}
