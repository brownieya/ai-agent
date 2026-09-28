<script setup>
import { computed } from 'vue'
import AppIcon from './icons/AppIcon.vue'
import { renderMessageHtml } from '@/utils/format'

const props = defineProps({
  role: { type: String, required: true }, // 'user' | 'ai'
  content: { type: String, default: '' },
  status: { type: String, default: 'done' }, // 'streaming' | 'done' | 'error'
  aiIcon: { type: String, default: 'bot' },
  stepLabel: { type: String, default: '' },
  variant: { type: String, default: 'default' }, // 'default' | 'error' | 'final'
})

const isUser = computed(() => props.role === 'user')
const html = computed(() => renderMessageHtml(props.content))
const showTypingDots = computed(() => props.status === 'streaming' && !props.content)
</script>

<template>
  <div class="msg" :class="[isUser ? 'msg--user' : 'msg--ai']">
    <div class="msg__avatar" :class="isUser ? 'msg__avatar--user' : 'msg__avatar--ai'">
      <AppIcon :name="isUser ? 'user' : aiIcon" :size="16" />
    </div>

    <div class="msg__body">
      <span
        v-if="stepLabel"
        class="msg__step"
        :class="{
          'msg__step--error': variant === 'error',
          'msg__step--final': variant === 'final',
        }"
      >
        {{ stepLabel }}
      </span>

      <div
        class="msg__bubble"
        :class="{
          'msg__bubble--user': isUser,
          'msg__bubble--ai': !isUser,
          'msg__bubble--error': variant === 'error',
          'msg__bubble--final': variant === 'final',
        }"
      >
        <span v-if="showTypingDots" class="msg__typing" aria-label="正在输入">
          <i /><i /><i />
        </span>
        <span v-else class="msg__text" v-html="html" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.msg {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  max-width: 100%;
  animation: msg-in var(--duration-base) var(--ease-standard);
}

@keyframes msg-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.msg--user {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.msg--ai {
  align-self: flex-start;
}

.msg__avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: var(--shadow-sm);
}

.msg__avatar--ai {
  background: var(--accent-gradient);
}

.msg__avatar--user {
  background: linear-gradient(135deg, var(--neutral-600), var(--neutral-700));
}

.msg__body {
  display: flex;
  flex-direction: column;
  max-width: min(560px, 72vw);
}

.msg--user .msg__body {
  align-items: flex-end;
}

.msg__step {
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.03em;
  text-transform: uppercase;
  color: var(--accent-1);
  margin-bottom: var(--space-1);
  padding-left: var(--space-1);
}

.msg__step--final {
  color: #34d399;
}

.msg__step--error {
  color: #f87171;
}

.msg__bubble {
  padding: 11px 16px;
  border-radius: var(--radius-md);
  font-size: 14.5px;
  line-height: 1.65;
  word-break: break-word;
  white-space: normal;
}

.msg__bubble--ai {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.09);
  color: var(--neutral-100);
  border-top-left-radius: 4px;
}

.msg__bubble--user {
  background: var(--accent-gradient);
  color: #fff;
  border-top-right-radius: 4px;
}

.msg__bubble--final {
  background: rgba(52, 211, 153, 0.1);
  border-color: rgba(52, 211, 153, 0.3);
}

.msg__bubble--error {
  background: rgba(248, 113, 113, 0.1);
  border-color: rgba(248, 113, 113, 0.3);
}

.msg__text :deep(code) {
  background: rgba(255, 255, 255, 0.12);
  padding: 1px 6px;
  border-radius: 6px;
  font-size: 13px;
}

.msg__typing {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 18px;
  padding: 2px 0;
}

.msg__typing i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.55;
  animation: typing-bounce 1.1s var(--ease-standard) infinite;
}

.msg__typing i:nth-child(2) {
  animation-delay: 0.15s;
}
.msg__typing i:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typing-bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}
</style>
