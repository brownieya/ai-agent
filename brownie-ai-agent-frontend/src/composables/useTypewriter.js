/**
 * 打字机效果调度器。
 *
 * 背景：SSE 的 chunk 到达时机/大小完全由网络和后端决定——有时是一个字一个字地推，
 * 有时因为缓冲会几十个字一起“炸”出来，直接把 chunk 塞进 content 会导致文本
 * 一瞬间刷新，看不出“打字”的感觉。
 *
 * 做法：维护一个隐藏的 `target`（已收到的全部文本）和外部可见的 `message.content`
 * （已经“打出来”的文本），用 requestAnimationFrame 按固定语速把 target 逐字符
 * 显示到 content 上。当积压的字符变多时（比如后端一次性推了一大段），会自动
 * 提速追赶，保证不会永远卡在很久之前，但视觉上依然保留逐字浮现的效果。
 *
 * @param {{ content: string }} message 响应式消息对象，会原地修改它的 content 字段
 * @param {object} [options]
 * @param {number} [options.baseCps=60] 基础语速（字符/秒）
 * @param {number} [options.catchUpMs=900] 期望在多长时间内追平积压文本
 */
export function createTypewriter(message, options = {}) {
  const { baseCps = 60, catchUpMs = 900 } = options

  let target = ''
  let rafId = null
  let lastTime = null
  let finished = false
  let onFinishCallback = null

  function tick(time) {
    if (lastTime == null) lastTime = time
    const dt = time - lastTime
    lastTime = time

    const backlog = target.length - message.content.length
    if (backlog > 0) {
      // 积压越多追得越快：保证长文本也能在 ~catchUpMs 内被“打”完
      const dynamicCps = Math.max(baseCps, (backlog / catchUpMs) * 1000)
      const charsToReveal = Math.max(1, Math.round((dynamicCps * dt) / 1000))
      const nextLen = Math.min(target.length, message.content.length + charsToReveal)
      message.content = target.slice(0, nextLen)
    }

    if (message.content.length < target.length) {
      rafId = requestAnimationFrame(tick)
    } else {
      rafId = null
      lastTime = null
      if (finished && onFinishCallback) {
        const cb = onFinishCallback
        onFinishCallback = null
        cb()
      }
    }
  }

  function ensureRunning() {
    if (rafId == null) {
      lastTime = null
      rafId = requestAnimationFrame(tick)
    }
  }

  /** 追加新到达的文本片段，触发/延续打字动画 */
  function push(chunk) {
    if (!chunk) return
    target += chunk
    ensureRunning()
  }

  /**
   * 声明「不会再有新文本了」。若动画已经追上，立即执行回调；
   * 否则等动画自然播完剩余文字后再执行回调。
   */
  function finish(onFinish) {
    finished = true
    if (message.content.length >= target.length) {
      onFinish && onFinish()
    } else {
      onFinishCallback = onFinish
      ensureRunning()
    }
  }

  /** 立即把剩余文本全部显示出来并停止动画（用于用户提前打断/切换会话等场景） */
  function completeNow() {
    message.content = target
    stop()
    if (finished && onFinishCallback) {
      const cb = onFinishCallback
      onFinishCallback = null
      cb()
    }
  }

  /** 硬停止动画循环（不补全文本），用于组件卸载时清理 */
  function stop() {
    if (rafId != null) cancelAnimationFrame(rafId)
    rafId = null
    lastTime = null
  }

  /** 无动画地直接设置文本（用于错误提示等需要立即可见的场景） */
  function setImmediate(text) {
    target = text
    message.content = text
    stop()
  }

  return { push, finish, completeNow, stop, setImmediate }
}
