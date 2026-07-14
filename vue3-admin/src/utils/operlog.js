import request from '@/utils/request'

// 日志上报接口（与后端 OperLogController.receiveFrontendLog 对应）
const LOG_ENDPOINT = '/monitor/operlog/frontend'

/**
 * 上报一条前端操作日志。
 *
 * 该调用失败会被静默忽略，绝不影响主业务流程。
 * 同时避免对日志上报接口自身失败做二次上报，防止死循环。
 *
 * @param {Object} options
 * @param {string} [options.title='前端操作'] 模块标题
 * @param {number} [options.businessType=0] 业务类型（0其他 1新增 2修改 3删除 4查询 5登录 6退出 ...）
 * @param {string} [options.requestMethod] 请求方式
 * @param {string} [options.operUrl] 请求URL / 页面路径
 * @param {string} [options.operParam] 请求参数
 * @param {string} [options.jsonResult] 返回参数
 * @param {number} [options.status=0] 操作状态（0成功 1失败）
 * @param {string} [options.errorMsg] 错误消息
 */
export function reportOperLog({
  title = '前端操作',
  businessType = 0,
  requestMethod = '',
  method = '',
  operUrl = '',
  operParam = '',
  jsonResult = '',
  status = 0,
  errorMsg = ''
} = {}) {
  request
    .post(LOG_ENDPOINT, {
      title,
      businessType,
      requestMethod,
      method,
      operUrl,
      operParam,
      jsonResult,
      status,
      errorMsg
    })
    .catch(() => {})
}
