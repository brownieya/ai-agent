<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import ChatWindow from '@/components/ChatWindow.vue'
import { connectManusChat } from '@/api/chat'
import { generateId } from '@/utils/uuid'
import { parseManusChunk } from '@/utils/format'
import { createTypewriter } from '@/composables/useTypewriter'

const messages = ref([])
const pending = ref(false)
const connectionState = ref('idle') // idle | connecting | streaming | error
let activeConnection = null
let thinkingId = null
let stepTypewriters = [] // 当前这一轮里，还在“打字”的所有步骤气泡的 typewriter

const statusText = computed(() => {
  switch (connectionState.value) {
    case 'connecting':
      return '连接中…'
    case 'streaming':
      return '执行中…'
    case 'error':
      return '连接异常'
    default:
      return '在线'
  }
})

onBeforeUnmount(() => {
  activeConnection?.close()
  stepTypewriters.forEach((tw) => tw.stop())
})

function variantOf(type) {
  if (type === 'error') return 'error'
  if (type === 'done') return 'final'
  return 'default'
}

function removeThinkingPlaceholder() {
  if (!thinkingId) return
  messages.value = messages.value.filter((m) => m.id !== thinkingId)
  thinkingId = null
}

/** 打断上一轮还没打完字的步骤气泡，直接补全，避免残留半截文字 */
function finalizePendingSteps() {
  stepTypewriters.forEach((tw) => tw.completeNow())
  stepTypewriters = []
}

function handleSend(text) {
  activeConnection?.close()
  finalizePendingSteps()

  messages.value.push({
    id: generateId(),
    role: 'user',
    content: text,
    status: 'done',
  })

  // 智能体在给出第一个执行步骤前，用一个“思考中”的占位气泡承接等待感
  thinkingId = generateId()
  messages.value.push({
    id: thinkingId,
    role: 'ai',
    content: '',
    status: 'streaming',
  })

  pending.value = true
  connectionState.value = 'connecting'

  activeConnection = connectManusChat(text, {
    onOpen: () => {
      connectionState.value = 'streaming'
    },
    onMessage: (raw) => {
      removeThinkingPlaceholder()
      const parsed = parseManusChunk(raw)

      const stepMessage = {
        id: generateId(),
        role: 'ai',
        content: '',
        status: 'streaming',
        stepLabel: parsed.label,
        variant: variantOf(parsed.type),
      }
      messages.value.push(stepMessage)

      // 每一步的完整文本一次性到达，用打字机效果把它“打”出来，而不是瞬间显示
      const typewriter = createTypewriter(stepMessage)
      stepTypewriters.push(typewriter)
      typewriter.push(parsed.content)
      typewriter.finish(() => {
        stepMessage.status = 'done'
        stepTypewriters = stepTypewriters.filter((tw) => tw !== typewriter)
      })
    },
    onDone: () => {
      removeThinkingPlaceholder()
      pending.value = false
      connectionState.value = 'idle'
    },
    onError: () => {
      if (thinkingId) {
        const placeholder = messages.value.find((m) => m.id === thinkingId)
        if (placeholder) {
          placeholder.content = '连接失败了，请确认后端服务（http://localhost:8123）已启动后重试。'
          placeholder.status = 'error'
          placeholder.stepLabel = '出错了'
          placeholder.variant = 'error'
        }
        thinkingId = null
      }
      pending.value = false
      connectionState.value = 'error'
    },
  })
}
</script>

<template>
  <div class="theme-manus">
    <ChatWindow
      title="AI 超级智能体"
      subtitle="Manus · 具备工具调用能力"
      ai-icon="bot"
      :messages="messages"
      :pending="pending"
      :status-text="statusText"
      :status-state="connectionState"
      placeholder="描述你想完成的任务…"
      empty-title="我是 Brownie Manus"
      empty-hint="告诉我一个目标，我会拆解任务、按步骤调用工具执行，并实时展示我的每一步思考。"
      @send="handleSend"
    />
  </div>
</template>
