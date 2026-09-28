<script setup>
import { useRouter } from 'vue-router'
import AppIcon from './icons/AppIcon.vue'

const props = defineProps({
  to: { type: String, required: true },
  icon: { type: String, required: true },
  tag: { type: String, default: '' },
  title: { type: String, required: true },
  description: { type: String, required: true },
  theme: { type: String, required: true }, // 'love' | 'manus'
})

const router = useRouter()

function enter() {
  router.push(props.to)
}
</script>

<template>
  <button
    class="app-card"
    :class="`theme-${theme}`"
    type="button"
    @click="enter"
  >
    <span class="app-card__glow" aria-hidden="true" />

    <div class="app-card__top">
      <span class="app-card__icon">
        <AppIcon :name="icon" :size="28" />
      </span>
      <span v-if="tag" class="app-card__tag">{{ tag }}</span>
    </div>

    <h3 class="app-card__title">{{ title }}</h3>
    <p class="app-card__desc">{{ description }}</p>

    <span class="app-card__cta">
      进入对话
      <AppIcon name="arrow-right" :size="16" />
    </span>
  </button>
</template>

<style scoped>
.app-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  width: 100%;
  padding: var(--space-6) var(--space-5) var(--space-5);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.06) 0%, rgba(255, 255, 255, 0.02) 100%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  color: var(--neutral-100);
  overflow: hidden;
  transition: transform var(--duration-base) var(--ease-standard),
    border-color var(--duration-base) var(--ease-standard),
    box-shadow var(--duration-base) var(--ease-standard);
}

.app-card:hover,
.app-card:focus-visible {
  transform: translateY(-6px);
  border-color: rgba(255, 255, 255, 0.22);
  box-shadow: var(--shadow-lg);
  outline: none;
}

.app-card:active {
  transform: translateY(-2px) scale(0.99);
}

.app-card__glow {
  position: absolute;
  inset: -40% -20% auto auto;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: var(--accent-gradient);
  opacity: 0.35;
  filter: blur(50px);
  pointer-events: none;
  transition: opacity var(--duration-base) var(--ease-standard);
}

.app-card:hover .app-card__glow {
  opacity: 0.55;
}

.app-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin-bottom: var(--space-5);
  z-index: 1;
}

.app-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  background: var(--accent-gradient);
  color: #fff;
  box-shadow: var(--shadow-md);
}

.app-card__tag {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
  padding: 5px 12px;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: var(--neutral-200);
}

.app-card__title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: var(--space-2);
  z-index: 1;
}

.app-card__desc {
  font-size: 14.5px;
  line-height: 1.6;
  color: var(--neutral-400);
  margin-bottom: var(--space-6);
  z-index: 1;
}

.app-card__cta {
  margin-top: auto;
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 14px;
  font-weight: 600;
  color: var(--neutral-0);
  z-index: 1;
  transition: gap var(--duration-fast) var(--ease-standard);
}

.app-card:hover .app-card__cta {
  gap: var(--space-3);
}
</style>
