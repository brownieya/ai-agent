<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import ChatWindow from '@/components/ChatWindow.vue'
import { connectManusChat } from '@/api/chat'
import { generateId } from '@/utils/uuid'
import { parseManusChunk } from '@/utils/format'

const messages = ref([])
const pending = ref(false)
const connectionState = ref('idle') // idle | connecting | streaming | error
let activeConnection = null
let thinkingId = null

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

function handleSend(text) {
  activeConnection?.close()

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
      messages.value.push({
        id: generateId(),
        role: 'ai',
        content: parsed.content,
        status: 'done',
        stepLabel: parsed.label,
        variant: variantOf(parsed.type),
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
