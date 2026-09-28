# Brownie AI Agent · 前端

基于 Vue 3 + Vite 的多应用前端，用于切换体验两个 AI 应用：

- **AI 恋爱大师**：情感对话聊天室，通过 SSE 调用后端 `/ai/love_app/chat/sse` 接口，逐字流式展示回复。
- **AI 超级智能体（Manus）**：具备工具调用能力的智能体，通过 SSE 调用后端 `/ai/manus/chat` 接口，实时展示每一步（Step）执行过程。

## 技术栈

- Vue 3（`<script setup>`）+ Vite
- Vue Router 4（首页 / 恋爱大师 / 超级智能体 三个路由）
- Axios（通用 HTTP 请求基座，见 `src/api/request.js`）
- 浏览器原生 `EventSource` 封装（用于消费 `text/event-stream` 流式接口，见 `src/api/sse.js`）
- 纯 CSS 实现的玻璃拟态（Glassmorphism）视觉风格，无第三方 UI 组件库依赖

> 为什么流式对话不直接用 Axios？后端两个聊天接口返回 `text/event-stream`，
> 浏览器原生 `EventSource` 才能正确按 SSE 协议帧解析数据、处理断线语义；
> Axios 在本项目中承担其余「非流式」请求的统一封装角色（见 `src/api/request.js`）。

## 目录结构

```
src/
  api/
    request.js   # Axios 实例（预留给非流式接口）
    sse.js       # 基于 EventSource 的 SSE 连接封装
    chat.js      # 恋爱大师 / Manus 两个聊天接口的调用方法
  components/
    ChatWindow.vue     # 通用聊天室布局（消息列表 + 输入框）
    MessageBubble.vue  # 消息气泡（支持流式打字动效 / 步骤标签）
    AppCard.vue         # 首页应用卡片
    icons/AppIcon.vue   # 线性 SVG 图标集合
  views/
    Home.vue      # 首页：应用切换入口
    LoveApp.vue   # AI 恋爱大师聊天页
    ManusApp.vue  # AI 超级智能体聊天页
  utils/
    uuid.js    # 生成聊天室 id
    format.js  # 消息内容渲染 / Manus 分步文本解析
```

## 开发环境运行

后端需先启动（默认监听 `http://localhost:8123`，接口前缀 `/api`）。

```bash
npm install
npm run dev
```

开发服务器默认运行在 `http://localhost:5173`，并通过 `vite.config.js` 中的 `server.proxy`
将 `/api` 请求代理到 `http://localhost:8123`，同源访问可避免 CORS 及 `EventSource` 跨域问题。

## 生产构建

```bash
npm run build
npm run preview
```

如果生产环境前后端不同源部署，请修改 `.env.production` 中的 `VITE_API_BASE_URL`
为后端完整地址，例如 `http://your-backend-host:8123/api`。
