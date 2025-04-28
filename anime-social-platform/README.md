## 管理员API

后端提供了以下管理员API，用于管理系统内容和用户：

### 用户管理
- `GET /api/admin/users` - 获取所有用户列表（分页）
- `GET /api/admin/admins` - 获取所有管理员列表
- `GET /api/admin/users/search` - 搜索用户
- `PUT /api/admin/users/{id}/status` - 禁用/启用用户

### 内容管理
- `DELETE /api/admin/posts/{id}` - 删除帖子
- `PUT /api/admin/posts/{id}/top` - 置顶/取消置顶帖子
- `DELETE /api/admin/resources/{id}` - 删除资源

### 活动管理
- `POST /api/admin/events` - 创建活动
- `PUT /api/admin/events/{id}` - 更新活动
- `DELETE /api/admin/events/{id}` - 删除活动
- `PUT /api/admin/events/{id}/status` - 上线/下线活动

### 统计信息
- `GET /api/admin/statistics` - 获取系统统计信息（用户数、帖子数、资源数、活动数等）

所有管理员API都需要管理员权限，通过会话中的isAdmin属性进行验证。 