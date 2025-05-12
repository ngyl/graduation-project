# 动漫社交平台前端

## 项目概述
这是一个专为动漫爱好者设计的社交平台前端项目。用户可以分享、讨论动漫内容，发布帖子，上传资源，参与活动，并与好友交流。项目采用现代前端技术栈，提供流畅的用户体验和丰富的交互功能。

## 技术栈
- **框架**: Vue 3.5.13 (组合式API) + TypeScript 5.7.2
- **路由**: Vue Router 4.5.0
- **状态管理**: Pinia 3.0.2 (使用持久化存储)
- **UI组件库**: Element Plus 2.9.7
- **构建工具**: Vite 6.2.0
- **HTTP客户端**: Axios 1.8.4
- **图表**: ECharts 5.6.0
- **WebSocket**: SockJS-Client 1.6.1 + StompJS 7.1.1
- **日期处理**: date-fns 4.1.0

## 核心功能

### 用户系统
- 注册与登录（Session认证）
- 个人资料管理
- 隐私设置
- 多级权限控制

### 社区内容
- **帖子系统**: 富文本编辑、分类标签、评论互动
- **资源分享**: 上传下载、评分系统
- **活动系统**: 创建管理、报名参与

### 社交互动
- **好友系统**: 添加关注、分组管理
- **即时通讯**: 私聊、群聊、消息推送
- **互动功能**: 点赞、收藏、成就系统

### 管理后台
- 用户管理
- 数据分析

## 快速开始

### 环境要求
- Node.js 16.0.0+ (推荐使用 Node.js 18 LTS)
- npm 7.0.0+ 或 yarn 1.22.0+ 或 pnpm

### 安装依赖
```bash
# 使用npm
npm install

# 使用yarn
yarn

# 使用pnpm
pnpm install
```

### 开发模式
```bash
npm run dev
# 默认在 http://localhost:5173 启动
```

### 构建生产版本
```bash
npm run build
# 构建文件将输出到 dist 目录
```

### 预览生产构建
```bash
npm run preview
```

## 项目结构

```
src/
├── api/                # API请求封装
│   ├── axios.ts        # Axios实例与拦截器
│   ├── auth.ts         # 认证相关API
│   ├── post.ts         # 帖子相关API
│   ├── resource.ts     # 资源相关API
│   ├── message.ts      # 消息相关API
│   └── ...
│
├── assets/             # 静态资源
│
├── components/         # 公共组件
│   ├── AppHeader.vue   # 应用头部
│   ├── AppLayout.vue   # 布局组件
│   ├── MainNavbar.vue  # 主导航栏
│   ├── AdminLayout.vue # 管理员布局
│   └── ...
│
├── router/             # 路由配置
│   └── index.ts        # 路由定义与守卫
│
├── stores/             # Pinia状态管理
│   ├── user.ts         # 用户状态
│   └── message.ts      # 消息状态
│
├── types/              # TypeScript类型定义
│
├── utils/              # 工具函数
│   └── websocket.ts    # WebSocket服务
│
├── views/              # 页面组件
│   ├── Home.vue        # 首页
│   ├── Login.vue       # 登录页
│   ├── Profile.vue     # 个人资料页
│   ├── PostList.vue    # 帖子列表
│   ├── admin/          # 管理员页面
│   └── ...
│
├── App.vue             # 应用入口组件
├── main.ts             # 应用入口文件
└── style.css           # 全局样式
```

## 开发指南

### 代码规范
- 使用ESLint和Prettier保证代码质量
- 遵循Vue 3组合式API最佳实践
- 使用TypeScript类型定义

### 组件开发示例
```vue
<script setup lang="ts">
// 组件Props定义
interface Props {
  user: {
    id: number;
    name: string;
    avatar?: string;
  };
}

const props = defineProps<Props>();
</script>
```

### API调用示例
```typescript
import { getUserProfile } from '@/api/user';
import { ref, onMounted } from 'vue';

const userProfile = ref(null);
const loading = ref(false);

onMounted(async () => {
  try {
    loading.value = true;
    const { data } = await getUserProfile(userId);
    userProfile.value = data;
  } finally {
    loading.value = false;
  }
});
```

### 状态管理示例
```typescript
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
console.log(userStore.isLoggedIn);
userStore.loginUser(username, password);
```

## 主题与样式
项目支持亮色/暗色主题切换，基于CSS变量实现：

```css
:root {
  /* 亮色主题变量 */
  --primary-color: #8c67ef;
  --bg-primary: #ffffff;
  --text-primary: #333333;
}

[data-theme="dark"] {
  /* 暗色主题变量 */
  --primary-color: #9d7aff;
  --bg-primary: #1a1a2e;
  --text-primary: #ffffff;
}
```

## 常见问题

### 开发环境问题
**Q: 为什么npm run dev命令启动失败？**  
A: 检查Node.js版本是否满足要求，确保所有依赖正确安装。

**Q: 如何处理跨域请求问题？**  
A: 在vite.config.ts中已配置proxy代理解决开发环境跨域问题。

### 构建部署问题
**Q: 如何配置不同环境的API地址？**  
A: 使用.env.development和.env.production文件配置不同环境的变量。

**Q: 生产构建后如何部署？**  
A: 将dist目录部署到静态服务器，并确保服务器配置支持SPA路由。

## 性能优化
- 路由懒加载
- 组件按需引入
- 图片资源优化
- 代码分割
- 本地缓存策略

## 贡献指南
1. Fork项目仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 提交Pull Request

