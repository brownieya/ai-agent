<script setup>
import AppCard from '@/components/AppCard.vue'
import AppIcon from '@/components/icons/AppIcon.vue'

const apps = [
  {
    to: '/love',
    icon: 'heart',
    tag: '情感对话',
    title: 'AI 恋爱大师',
    description: '你的专属情感顾问，随时聊聊恋爱里的困惑、暧昧的心动，以及如何更好地表达自己。',
    theme: 'love',
  },
  {
    to: '/manus',
    icon: 'bot',
    tag: '智能体 · ReAct',
    title: 'AI 超级智能体',
    description: '拥有工具调用能力的全能助手，会自主拆解任务、逐步执行，并实时展示它的思考过程。',
    theme: 'manus',
  },
]
</script>

<template>
  <div class="home theme-home">
    <div class="home__bg" aria-hidden="true" />

    <header class="home__header">
      <div class="home__brand">
        <span class="home__brand-icon">
          <AppIcon name="sparkles" :size="18" />
        </span>
        Brownie AI Agent
      </div>
    </header>

    <main class="home__main">
      <p class="home__eyebrow">选择一个应用，开始体验</p>
      <h1 class="home__title">一个入口，<br class="home__title-break" />两种 AI 能力</h1>
      <p class="home__subtitle">
        左边是懂情感的聊天伙伴，右边是能拆解任务、调用工具的智能体。挑一个，开始对话吧。
      </p>

      <div class="home__grid">
        <AppCard v-for="app in apps" :key="app.to" v-bind="app" />
      </div>
    </main>

    <footer class="home__footer">Vue 3 · Axios · Server-Sent Events</footer>
  </div>
</template>

<style scoped>
.home {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.home__bg {
  position: absolute;
  inset: 0;
  background: var(--bg-gradient);
  z-index: 0;
}

.home__header,
.home__main,
.home__footer {
  position: relative;
  z-index: 1;
}

.home__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-8);
}

.home__brand {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: 700;
  font-size: 15px;
  letter-spacing: 0.01em;
  color: var(--neutral-100);
}

.home__brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--brand-love), var(--brand-manus));
  color: #fff;
}

.home__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: var(--space-6) var(--space-5) var(--space-10);
  max-width: 960px;
  margin: 0 auto;
  width: 100%;
}

.home__eyebrow {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--neutral-400);
  margin-bottom: var(--space-4);
}

.home__title {
  font-size: clamp(32px, 5vw, 52px);
  line-height: 1.15;
  font-weight: 800;
  letter-spacing: -0.01em;
  background: linear-gradient(135deg, #fff 30%, var(--neutral-200) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: var(--space-5);
}

.home__title-break {
  display: none;
}

@media (min-width: 640px) {
  .home__title-break {
    display: block;
  }
}

.home__subtitle {
  max-width: 560px;
  font-size: 16px;
  line-height: 1.7;
  color: var(--neutral-400);
  margin-bottom: var(--space-8);
}

.home__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-5);
  width: 100%;
}

@media (max-width: 720px) {
  .home__grid {
    grid-template-columns: 1fr;
  }
}

.home__footer {
  text-align: center;
  padding: var(--space-5);
  font-size: 12.5px;
  color: var(--neutral-600);
  letter-spacing: 0.03em;
}
</style>
