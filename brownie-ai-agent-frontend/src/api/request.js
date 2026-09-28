import axios from 'axios'

/**
 * 统一的 Axios 请求实例。
 *
 * 说明：本项目中「聊天流式对话」接口是 SSE（text/event-stream），
 * 浏览器原生 EventSource 才能正确解析 SSE 协议帧（自动处理 `data:` 分帧、
 * 断线语义等），Axios/XHR/fetch 都不会替我们做这件事，因此流式对话统一走
 * `src/api/sse.js` 中基于 EventSource 的封装。
 *
 * 这个 Axios 实例作为项目通用的 HTTP 客户端基座，用于后续任何非流式的
 * 普通接口调用（例如健康检查、同步问答 `/ai/love_app/chat/sync` 等），
 * 统一处理 baseURL、超时、错误提示等。
 */
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('[request error]', error?.message || error)
    return Promise.reject(error)
  },
)

export default request
