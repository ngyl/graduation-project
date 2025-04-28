# 动漫社交平台前端

本项目是动漫社交平台的前端部分，基于Vue 3、TypeScript和Vite开发，采用现代化的开发模式和工具链。

## 技术栈

- **框架**: Vue 3.5+
- **构建工具**: Vite 6.2+
- **状态管理**: Pinia 3.0+
- **路由**: Vue Router 4.5+
- **UI组件库**: Element Plus 2.9+
- **HTTP客户端**: Axios
- **语言**: TypeScript 5.7+
- **样式**: 自定义CSS变量系统

## 功能特点

- 响应式设计，适配各种屏幕尺寸
- 暗黑/明亮模式切换
- 用户认证与授权
- 帖子浏览与创建
- 社交互动功能（好友、收藏、消息等）
- 管理员控制台

## 项目结构

```
src/
├── api/         # API接口
├── assets/      # 静态资源
├── components/  # 可复用组件
├── router/      # 路由配置
├── stores/      # Pinia状态管理
├── types/       # TypeScript类型定义
├── utils/       # 工具函数
└── views/       # 页面组件
```

## 开发指南

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 样式指南

项目使用CSS变量系统定义了统一的颜色、间距、字体等样式属性，确保整个应用的风格一致性。详细样式定义请参考`src/style.css`文件。

## 组件设计原则

1. **单一职责**: 每个组件应该只做一件事情
2. **可复用性**: 抽象通用逻辑到可复用组件
3. **自包含**: 组件应尽可能减少对外部依赖
4. **可测试**: 组件设计应方便单元测试
5. **命名一致**: 遵循项目命名规范

## 持续优化计划

- [ ] 性能优化（代码分割、懒加载）
- [ ] 增强动画效果
- [ ] 国际化支持
- [ ] 无障碍访问优化
- [ ] PWA支持

## 贡献指南

欢迎提交代码或建议！请确保您的代码符合项目的风格指南，并通过相关测试。

## API响应格式统一说明

为了统一前后端的API响应格式，我们对API的返回结果进行了标准化处理：

### API响应结构

```typescript
export interface ApiResponse<T> {
  /**
   * 状态码
   * 200: 成功
   * 400: 参数错误
   * 401: 未授权
   * 403: 禁止访问
   * 404: 资源不存在
   * 500: 服务器错误
   */
  code: number;
  
  /**
   * 响应消息
   */
  message: string;
  
  /**
   * 响应数据
   */
  data: T;
  
  /**
   * 时间戳（毫秒）
   */
  timestamp: number;
}
```

### 响应工具类

为了方便对响应状态进行判断，我们提供了一个工具类：

```typescript
export const ApiResponseUtil = {
  /**
   * 判断响应是否成功
   */
  isSuccess: <T>(response: ApiResponse<T>): boolean => {
    return response.code === 200;
  },

  /**
   * 判断是否未授权
   */
  isUnauthorized: <T>(response: ApiResponse<T>): boolean => {
    return response.code === 401;
  },

  /**
   * 判断是否禁止访问
   */
  isForbidden: <T>(response: ApiResponse<T>): boolean => {
    return response.code === 403;
  },

  /**
   * 判断资源是否不存在
   */
  isNotFound: <T>(response: ApiResponse<T>): boolean => {
    return response.code === 404;
  },

  /**
   * 判断是否服务器错误
   */
  isServerError: <T>(response: ApiResponse<T>): boolean => {
    return response.code === 500;
  }
};
```

### 使用示例

```typescript
const res = await getUserDetail(userId);
if (ApiResponseUtil.isSuccess(res.data)) {
  // 处理成功的响应
  const userData = res.data.data;
  // ...
} else if (ApiResponseUtil.isUnauthorized(res.data)) {
  // 处理未授权的情况
  ElMessage.warning('请先登录');
  router.push('/login');
} else {
  // 处理其他错误
  ElMessage.error(res.data.message || '操作失败');
}
```

这种统一的处理方式，可以让我们的代码更加清晰、一致，并且与后端的API响应格式保持同步。
