/**
 * 数据字典工具类
 *
 * 提供便捷的字典数据获取、转换、渲染方法
 * 内部通过 useDictStore 实现懒加载 + 内存缓存
 */
import { ref } from 'vue'
import { useDictStoreWithOut } from '@/stores/dict'

const dictStore = useDictStoreWithOut()

// ==================== 类型定义 ====================

/**
 * @typedef {Object} DictDataType
 * @property {string}  dictType   - 字典类型编码
 * @property {string}  label      - 显示文本
 * @property {string}  value      - 字典值
 * @property {string}  colorType  - Element Plus el-tag type（primary/success/warning/danger/info）
 * @property {string}  cssClass   - 自定义 CSS 类名
 */

// ==================== 获取字典选项 ====================

/**
 * 获取指定 dictType 的字典数据列表（原始数据）
 * @param {string} dictType
 * @returns {DictDataType[]}
 */
export const getDictOptions = (dictType) => {
  return dictStore.getDictByType(dictType) || []
}

/**
 * 获取 dictType 字典数据，并强制 value 转为 number 类型
 * 解决 el-option key 类型告警
 * @param {string} dictType
 * @returns {{ dictType, label, value: number, colorType, cssClass }[]}
 */
export const getIntDictOptions = (dictType) => {
  return getDictOptions(dictType).map((item) => ({
    ...item,
    value: parseInt(item.value + '')
  }))
}

/**
 * 获取 dictType 字典数据，并强制 value 转为 string 类型
 * 解决 el-option key 类型告警
 * @param {string} dictType
 * @returns {{ dictType, label, value: string, colorType, cssClass }[]}
 */
export const getStrDictOptions = (dictType) => {
  return getDictOptions(dictType).map((item) => ({
    ...item,
    value: item.value + ''
  }))
}

/**
 * 获取 dictType 字典数据，并强制 value 转为 boolean 类型
 * @param {string} dictType
 * @returns {{ dictType, label, value: boolean, colorType, cssClass }[]}
 */
export const getBoolDictOptions = (dictType) => {
  return getDictOptions(dictType).map((item) => ({
    ...item,
    value: item.value + '' === 'true'
  }))
}

// ==================== 字典查找 ====================

/**
 * 根据字典值获取字典对象
 * @param {string} dictType 字典类型
 * @param {*}      value    字典值
 * @returns {DictDataType|undefined}
 */
export const getDictObj = (dictType, value) => {
  return getDictOptions(dictType).find((item) => item.value === value + '')
}

/**
 * 获取字典值的文本展示（label）
 * @param {string} dictType 字典类型
 * @param {*}      value    字典值
 * @returns {string} 字典名称
 */
export const getDictLabel = (dictType, value) => {
  const label = ref('')
  getDictOptions(dictType).forEach((item) => {
    if (item.value === value + '') label.value = item.label
  })
  return label.value
}

// ==================== 字典类型枚举 ====================

export const DICT_TYPE = {
  SYSTEM_USER_SEX: 'sys_user_sex',
  SYSTEM_NORMAL_DISABLE: 'sys_normal_disable',
  SYSTEM_NOTICE_TYPE: 'sys_notice_type'
}
