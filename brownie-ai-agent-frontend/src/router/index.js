import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: Home,
    meta: { title: '首页' },
  },
  {
    path: '/love',
    name: 'love-app',
    component: () => import('@/views/LoveApp.vue'),
    meta: { title: 'AI 恋爱大师' },
  },
  {
    path: '/manus',
    name: 'manus-app',
    component: () => import('@/views/ManusApp.vue'),
    meta: { title: 'AI 超级智能体' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.afterEach((to) => {
  document.title = to.meta?.title ? `${to.meta.title} · Brownie AI` : 'Brownie AI'
})

export default router
