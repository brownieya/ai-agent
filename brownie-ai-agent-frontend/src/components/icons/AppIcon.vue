<script setup>
defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 20 },
})
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.8"
    stroke-linecap="round"
    stroke-linejoin="round"
    class="app-icon"
    aria-hidden="true"
  >
    <!-- 心形：AI 恋爱大师 -->
    <path
      v-if="name === 'heart'"
      d="M12 20.5s-6.9-4.2-9.4-8.4C1.1 9.6 1.6 6.4 4.2 4.9c2.1-1.2 4.7-.6 6.1 1.3l1.7 2.3 1.7-2.3c1.4-1.9 4-2.5 6.1-1.3 2.6 1.5 3.1 4.7 1.6 7.2-2.5 4.2-9.4 8.4-9.4 8.4z"
    />

    <!-- 机器人 / 智能体：AI 超级智能体 -->
    <g v-else-if="name === 'bot'">
      <rect x="4.5" y="9" width="15" height="10" rx="3.2" />
      <path d="M12 9V5.5" />
      <circle cx="12" cy="4" r="1.15" fill="currentColor" stroke="none" />
      <circle cx="9" cy="14" r="1.15" fill="currentColor" stroke="none" />
      <circle cx="15" cy="14" r="1.15" fill="currentColor" stroke="none" />
      <path d="M9 17.2h6" />
      <path d="M2.5 12.5v3" />
      <path d="M21.5 12.5v3" />
    </g>

    <!-- 发送 -->
    <path v-else-if="name === 'send'" d="M3.4 11.6 20.8 3.9 13.1 21.3l-2.5-7.4-7.2-2.3z" />

    <!-- 返回箭头 -->
    <g v-else-if="name === 'arrow-left'">
      <path d="M19 12H5" />
      <path d="M11 6l-6 6 6 6" />
    </g>

    <!-- 用户 -->
    <g v-else-if="name === 'user'">
      <circle cx="12" cy="8" r="3.4" />
      <path d="M5 20c1.1-3.6 4-5.4 7-5.4s5.9 1.8 7 5.4" />
    </g>

    <!-- 星火 / 智能感 -->
    <g v-else-if="name === 'sparkles'">
      <path d="M12 3.5l1.4 3.6 3.6 1.4-3.6 1.4L12 13.5l-1.4-3.6-3.6-1.4 3.6-1.4z" />
      <path d="M18.5 14l.8 2 2 .8-2 .8-.8 2-.8-2-2-.8 2-.8z" />
    </g>

    <!-- 右箭头 -->
    <g v-else-if="name === 'arrow-right'">
      <path d="M5 12h14" />
      <path d="M13 6l6 6-6 6" />
    </g>

    <!-- 加号（新建会话，预留） -->
    <g v-else-if="name === 'plus'">
      <path d="M12 5v14" />
      <path d="M5 12h14" />
    </g>

    <!-- 警示 -->
    <g v-else-if="name === 'alert'">
      <path d="M12 9v4.5" />
      <circle cx="12" cy="16.7" r="0.9" fill="currentColor" stroke="none" />
      <path
        d="M10.6 3.9 2.9 18.3A1.8 1.8 0 0 0 4.5 21h15a1.8 1.8 0 0 0 1.6-2.7L13.4 3.9a1.8 1.8 0 0 0-2.8 0z"
      />
    </g>
  </svg>
</template>

<style scoped>
.app-icon {
  display: block;
  flex-shrink: 0;
}
</style>
