<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ChatWindow from '@/components/ChatWindow.vue'
import { connectLoveAppChat } from '@/api/chat'
import { generateId } from '@/utils/uuid'

// 进入页面后自动生成一个聊天室 id，用于区分不同的会话
const chatId = ref('')
const messages = ref([])
const pending = ref(false)
const connectionState = ref('idle') // idle | connecting | streaming | error
let activeConnection = null

const statusText = computed(() => {
  switch (connectionState.value) {
    case 'connecting':
      return '连接中…'
    case 'streaming':
      return '思考中…'
    case 'error':
      return '连接异常'
    default:
      return '在线'
  }
})

onMounted(() => {
  chatId.value = generateId()
})

onBeforeUnmount(() => {
  activeConnection?.close()
})

function handleSend(text) {
  activeConnection?.close()

  messages.value.push({
    id: generateId(),
    role: 'user',
    content: text,
    status: 'done',
  })

  const aiMessage = {
    id: generateId(),
    role: 'ai',
    content: '',
    status: 'streaming',
  }
  messages.value.push(aiMessage)

  pending.value = true
  connectionState.value = 'connecting'

  activeConnection = connectLoveAppChat(text, chatId.value, {
    onOpen: () => {
      connectionState.value = 'streaming'
    },
    onMessage: (chunk) => {
      aiMessage.content += chunk
    },
    onDone: () => {
      aiMessage.status = 'done'
      pending.value = false
      connectionState.value = 'idle'
    },
    onError: () => {
      aiMessage.status = 'error'
      if (!aiMessage.content) {
        aiMessage.content = '连接失败了，请确认后端服务（http://localhost:8123）已启动后重试。'
      }
      pending.value = false
      connectionState.value = 'error'
    },
  })
}
</script>

<template>
  <div class="theme-love">
    <ChatWindow
      title="AI 恋爱大师"
      :subtitle="`会话 ID: ${chatId.slice(0, 8)}`"
      ai-icon="heart"
      :messages="messages"
      :pending="pending"
      :status-text="statusText"
      :status-state="connectionState"
      placeholder="说说你的恋爱烦恼，或想聊的话题…"
      empty-title="嗨，我是你的 AI 恋爱大师"
      empty-hint="无论是暧昧不明、如何表白，还是关系中的小摩擦，都可以说给我听。"
      @send="handleSend"
    />
  </div>
</template>
