<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ChatWindow from '@/components/ChatWindow.vue'
import { connectLoveAppChat } from '@/api/chat'
import { generateId } from '@/utils/uuid'
import { createTypewriter } from '@/composables/useTypewriter'

// 进入页面后自动生成一个聊天室 id，用于区分不同的会话
const chatId = ref('')
const messages = ref([])
const pending = ref(false)
const connectionState = ref('idle') // idle | connecting | streaming | error
let activeConnection = null
let activeTypewriter = null
let activeAiMessage = null

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
  activeTypewriter?.stop()
})

/** 如果上一轮回复还在“打字”中就被新消息打断，直接补全，避免残留半截文字 */
function finalizeActiveMessage() {
  if (activeAiMessage && activeAiMessage.status === 'streaming') {
    activeAiMessage.status = 'done'
  }
  activeTypewriter?.completeNow()
  activeTypewriter = null
  activeAiMessage = null
}

function handleSend(text) {
  activeConnection?.close()
  finalizeActiveMessage()

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
  activeAiMessage = aiMessage

  // 打字机效果：SSE 收到多少字都先攒起来，由 typewriter 按固定节奏“打”出来
  const typewriter = createTypewriter(aiMessage)
  activeTypewriter = typewriter

  pending.value = true
  connectionState.value = 'connecting'

  activeConnection = connectLoveAppChat(text, chatId.value, {
    onOpen: () => {
      connectionState.value = 'streaming'
    },
    onMessage: (chunk) => {
      typewriter.push(chunk)
    },
    onDone: () => {
      typewriter.finish(() => {
        aiMessage.status = 'done'
        pending.value = false
        connectionState.value = 'idle'
      })
    },
    onError: () => {
      typewriter.setImmediate('连接失败了，请确认后端服务（http://localhost:8123）已启动后重试。')
      aiMessage.status = 'error'
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
