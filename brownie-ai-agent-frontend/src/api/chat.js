import { createSSEConnection } from './sse'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

/**
 * 构建带 query 参数的完整请求地址
 */
function buildUrl(path, params = {}) {
  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      searchParams.append(key, value)
    }
  })
  const query = searchParams.toString()
  return `${BASE_URL}${path}${query ? `?${query}` : ''}`
}

/**
 * AI 恋爱大师 —— 流式对话（SSE）
 * 对应后端 GET /ai/love_app/chat/sse?message=&chatId=
 *
 * @param {string} message 用户输入内容
 * @param {string} chatId 会话 id，用于区分不同聊天室
 * @param {object} handlers 见 createSSEConnection
 */
export function connectLoveAppChat(message, chatId, handlers) {
  const url = buildUrl('/ai/love_app/chat/sse', { message, chatId })
  return createSSEConnection(url, handlers)
}

/**
 * AI 超级智能体 Manus —— 流式对话（SSE）
 * 对应后端 GET /ai/manus/chat?message=
 *
 * @param {string} message 用户输入内容
 * @param {object} handlers 见 createSSEConnection
 */
export function connectManusChat(message, handlers) {
  const url = buildUrl('/ai/manus/chat', { message })
  return createSSEConnection(url, handlers)
}
