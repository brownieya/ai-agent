/**
 * 将纯文本消息内容转换为可安全渲染的 HTML 片段。
 * 先转义特殊字符防止 XSS，再支持极简的 **加粗** / `行内代码` / 换行展示，
 * 满足 AI 回复中常见的轻量排版需求。
 */
export function renderMessageHtml(text) {
  if (!text) return ''
  const escaped = String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  return escaped
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br/>')
}

/**
 * 解析 BaseAgent#runStream 推送的分步文本，形如：
 *   "Step1: 正在思考如何解决问题..."
 *   "执行结束: 达到最大步骤 (20)"
 *   "执行错误: xxx"
 *   "错误：不能使用空提示词运行代理"
 * 返回结构化信息，便于 UI 用「步骤卡片」的形式逐条展示 Manus 的执行过程。
 */
export function parseManusChunk(raw) {
  const text = String(raw ?? '')

  const stepMatch = text.match(/^Step(\d+):\s*([\s\S]*)$/)
  if (stepMatch) {
    return {
      type: 'step',
      step: Number(stepMatch[1]),
      label: `Step ${stepMatch[1]}`,
      content: stepMatch[2],
    }
  }

  if (text.startsWith('执行结束')) {
    return { type: 'done', label: '执行结束', content: text.replace(/^执行结束[:：]\s*/, '') }
  }

  if (text.startsWith('执行错误') || text.startsWith('错误')) {
    return { type: 'error', label: '出错了', content: text.replace(/^(执行错误|错误)[:：]\s*/, '') }
  }

  return { type: 'plain', label: null, content: text }
}
