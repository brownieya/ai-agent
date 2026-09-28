/**
 * 基于浏览器原生 EventSource 的 SSE 连接封装。
 *
 * 后端两个流式接口（/ai/love_app/chat/sse 与 /ai/manus/chat）都以
 * GET + text/event-stream 的方式返回数据，且在数据推送完毕后由服务端
 * 主动关闭连接（Flux 结束 / SseEmitter.complete()）。浏览器的 EventSource
 * 在连接被服务端正常关闭时也会触发 `onerror`（因为它内置了断线重连语义），
 * 所以这里用「是否已经收到过数据」来区分：
 *   - 已收到过数据后触发的 error   -> 视为「本轮流式响应正常结束」
 *   - 从未收到任何数据就触发的 error -> 视为「连接失败」，交给上层提示用户
 *
 * @param {string} url 完整的 SSE 请求地址（含 query 参数）
 * @param {object} handlers
 * @param {(data: string) => void} handlers.onMessage 每收到一条 SSE 消息时触发
 * @param {() => void} [handlers.onOpen] 连接建立成功时触发
 * @param {() => void} [handlers.onDone] 流式响应正常结束时触发（无论后端是否显式发送结束标记）
 * @param {(err: Event) => void} [handlers.onError] 连接从未成功收到数据即失败时触发
 * @returns {{ close: () => void }} 可用于提前主动关闭连接
 */
export function createSSEConnection(url, handlers = {}) {
  const { onMessage, onOpen, onDone, onError } = handlers

  const eventSource = new EventSource(url)
  let hasReceivedData = false
  let closed = false

  const safeClose = () => {
    if (closed) return
    closed = true
    eventSource.close()
  }

  eventSource.onopen = () => {
    onOpen && onOpen()
  }

  eventSource.onmessage = (event) => {
    hasReceivedData = true
    onMessage && onMessage(event.data)
  }

  eventSource.onerror = (event) => {
    safeClose()
    if (hasReceivedData) {
      onDone && onDone()
    } else {
      onError && onError(event)
    }
  }

  return {
    close: safeClose,
  }
}
