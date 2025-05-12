# 动漫爱好者社交平台

## 项目概述
动漫文化在全球范围内广受欢迎，动漫爱好者们需要一个专属的社交平台来分享观影心得、讨论作品情节、获取资源及交友互动。现有社交平台在内容上不够垂直，无法提供定制化的社区体验，且难以维系动漫爱好者的持久兴趣。本平台将为动漫爱好者提供一个专属的在线讨论和分享空间，用户可在此分享内容、参与讨论、交流资源和参与活动。通过构建以兴趣为核心的社区文化，本系统旨在增强用户的归属感和参与感，打造一个高质量、活跃的动漫社交平台。

## 功能模块
系统包含以下核心功能模块：

### 1. 用户注册与个人主页
- 支持用户注册和登录功能，简单的用户名和密码注册即可，无需邮箱验证
- 每个用户拥有个人主页，可展示个人收藏的动漫资源、兴趣标签及发布的内容
- 用户可以管理个人资料、更新喜好设置，创建个性化的展示空间
- 用户可以选择自己喜欢的动漫标签，用于个性化内容筛选

### 2. 社区讨论
- 用户可以创建讨论话题或帖子，参与他人发布的讨论，支持评论和回复功能，增加互动性
- 提供热门话题、最新讨论和标签筛选，帮助用户快速找到感兴趣的内容
- 支持帖子点赞等互动功能
- 用户可以根据自己喜欢的标签查找相关讨论

### 3. 资源分享
- 用户可以上传并分享动漫相关图片、视频、文章等资源，支持多种文件格式的上传
- 其他用户可以查看、下载资源，并进行收藏，形成活跃的资源交流氛围
- 资源支持分类展示和搜索功能
- 用户可以在个人主页查看自己收藏的所有资源

### 4. 好友与私信
- 支持用户之间互加好友，用户可关注感兴趣的用户，查看他们的动态
- 提供私信功能，允许用户之间发送信息进行深入交流
- 支持消息提醒，确保信息沟通的及时性

### 5. 活动通知
- 平台推送热门活动、讨论话题、新作品发布等通知
- 用户可接收感兴趣的活动提醒
- 平台在特定节日或新作品发布期间会组织线上活动，增加平台活跃度

### 6. 管理员系统
- 管理员可以管理普通用户（查看、禁用、删除）
- 管理员有权编辑和删除帖子与资源
- 管理员负责创建和管理活动
- 管理员也可以像普通用户一样发表帖子和上传资源

### 实时通信
- WebSocket实现私信功能和在线状态显示

### 搜索功能
- ElasticSearch实现全文搜索

## 技术架构

### 前端
- 框架：Vue3 + TypeScript + Vite
- 状态管理：Pinia
- UI组件库：Element Plus
- 页面设计：响应式设计，支持移动端和桌面端

### 后端
- 框架：SpringBoot + MyBatis
- Java版本：17
- 认证方式：Session认证（无需JWT）
- 业务逻辑：分层架构（Controller, Service, DAO）

### 数据库
- MySQL Ver 8.4.4 for Win64 on x86_64 (MySQL Community Server - GPL)
- 数据表设计：用户表、帖子表、评论表、资源表、好友表、私信表、活动表等

### 其他技术
- 实时通信: WebSocket
- 搜索: Elasticsearch

## 数据库设计

### 1. 用户表(users)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 用户ID | PRIMARY KEY, AUTO_INCREMENT |
| username | VARCHAR(50) | 用户名 | NOT NULL, UNIQUE |
| password | VARCHAR(100) | 密码(加密存储) | NOT NULL |
| avatar | VARCHAR(255) | 头像URL | DEFAULT NULL |
| bio | TEXT | 个人简介 | DEFAULT NULL |
| is_admin | TINYINT(1) | 是否为管理员 | DEFAULT 0 |
| status | TINYINT(1) | 用户状态(0禁用,1正常) | DEFAULT 1 |
| register_time | DATETIME | 注册时间 | NOT NULL |
| last_login_time | DATETIME | 最后登录时间 | DEFAULT NULL |

### 2. 帖子表(posts)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 帖子ID | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INT | 发帖用户ID | FOREIGN KEY, NOT NULL |
| title | VARCHAR(100) | 标题 | NOT NULL |
| content | TEXT | 内容 | NOT NULL |
| created_at | DATETIME | 创建时间 | NOT NULL |
| updated_at | DATETIME | 更新时间 | DEFAULT NULL |
| view_count | INT | 浏览次数 | DEFAULT 0 |
| like_count | INT | 点赞数 | DEFAULT 0 |
| is_top | TINYINT(1) | 是否置顶 | DEFAULT 0 |

### 3. 评论表(comments)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 评论ID | PRIMARY KEY, AUTO_INCREMENT |
| post_id | INT | 帖子ID | FOREIGN KEY, NOT NULL |
| user_id | INT | 评论用户ID | FOREIGN KEY, NOT NULL |
| content | TEXT | 评论内容 | NOT NULL |
| created_at | DATETIME | 创建时间 | NOT NULL |
| parent_id | INT | 父评论ID | DEFAULT NULL |

### 4. 资源表(resources)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 资源ID | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INT | 上传用户ID | FOREIGN KEY, NOT NULL |
| title | VARCHAR(100) | 资源标题 | NOT NULL |
| description | TEXT | 资源描述 | DEFAULT NULL |
| file_url | VARCHAR(255) | 文件URL | NOT NULL |
| cover_url | VARCHAR(255) | 封面图片URL | DEFAULT NULL |
| file_type | VARCHAR(50) | 文件类型 | NOT NULL |
| file_size | INT | 文件大小(KB) | NOT NULL |
| upload_time | DATETIME | 上传时间 | NOT NULL |
| download_count | INT | 下载次数 | DEFAULT 0 |
| like_count | INT | 点赞数 | DEFAULT 0 |

**注意**：资源的封面图片(cover_url)是可选的，用户可以选择不上传封面图片。系统会为没有封面图片的资源显示默认封面或根据资源类型自动生成封面。

### 5. 好友关系表(friendships)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 关系ID | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INT | 用户ID | FOREIGN KEY, NOT NULL |
| friend_id | INT | 好友ID | FOREIGN KEY, NOT NULL |
| status | TINYINT(1) | 关系状态(0单向关注,1互相关注) | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | NOT NULL |

### 6. 私信表(messages)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 消息ID | PRIMARY KEY, AUTO_INCREMENT |
| sender_id | INT | 发送者ID | FOREIGN KEY, NOT NULL |
| receiver_id | INT | 接收者ID | FOREIGN KEY, NOT NULL |
| content | TEXT | 消息内容 | NOT NULL |
| send_time | DATETIME | 发送时间 | NOT NULL |
| read_status | TINYINT(1) | 阅读状态(0未读,1已读) | DEFAULT 0 |

### 7. 收藏表(favorites)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 收藏ID | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INT | 用户ID | FOREIGN KEY, NOT NULL |
| resource_id | INT | 资源ID | FOREIGN KEY, NOT NULL |
| created_at | DATETIME | 收藏时间 | NOT NULL |

**注意**：系统只允许用户收藏资源，不支持收藏帖子。

### 8. 标签表(tags)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 标签ID | PRIMARY KEY, AUTO_INCREMENT |
| name | VARCHAR(50) | 标签名 | NOT NULL, UNIQUE |
| category | VARCHAR(50) | 标签分类 | DEFAULT NULL |
| type | VARCHAR(20) | 标签类型(post/resource/user) | NOT NULL |

### 9. 内容标签关联表(content_tags)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 关联ID | PRIMARY KEY, AUTO_INCREMENT |
| tag_id | INT | 标签ID | FOREIGN KEY, NOT NULL |
| content_type | VARCHAR(20) | 内容类型(post/resource) | NOT NULL |
| content_id | INT | 内容ID | NOT NULL |

**注意**：系统将通过应用程序逻辑确保标签只能用于与其类型匹配的内容（帖子标签只能用于帖子，资源标签只能用于资源）。

### 10. 用户标签关联表(user_tags)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 关联ID | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INT | 用户ID | FOREIGN KEY, NOT NULL |
| tag_id | INT | 标签ID | FOREIGN KEY, NOT NULL |
| created_at | DATETIME | 创建时间 | NOT NULL |

**说明**：用户标签关联表用于存储用户感兴趣的标签，可用于内容筛选、个人兴趣展示等功能。用户可以在个人主页上选择和管理自己喜欢的标签，这些标签将用于内容的筛选，同时这些标签也是用户个性化展示的一部分。系统支持通过UserTagService提供的方法管理用户标签，包括添加、删除和批量更新标签。

### 11. 帖子点赞表(post_likes)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 点赞ID | PRIMARY KEY, AUTO_INCREMENT |
| post_id | INT | 帖子ID | FOREIGN KEY, NOT NULL |
| user_id | INT | 用户ID | FOREIGN KEY, NOT NULL |
| created_at | DATETIME | 点赞时间 | NOT NULL |

**说明**：帖子点赞表用于记录用户对帖子的点赞信息，用于防止重复点赞和帖子点赞状态的显示。

### 12. 活动表(events)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 活动ID | PRIMARY KEY, AUTO_INCREMENT |
| title | VARCHAR(100) | 活动标题 | NOT NULL |
| description | TEXT | 活动描述 | NOT NULL |
| start_time | DATETIME | 开始时间 | NOT NULL |
| end_time | DATETIME | 结束时间 | NOT NULL |
| status | TINYINT(1) | 活动状态(0下线,1上线) | DEFAULT 1 |
| created_by | INT | 创建者ID | FOREIGN KEY, NOT NULL |

### 13. 活动参与表(event_participations)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 参与ID | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INT | 用户ID | FOREIGN KEY, NOT NULL |
| event_id | INT | 活动ID | FOREIGN KEY, NOT NULL |
| participation_time | DATETIME | 参与时间 | NOT NULL |
| status | TINYINT(1) | 参与状态(0已取消,1已报名) | DEFAULT 1 |

**说明**：活动参与表用于记录用户参加活动的情况，实现了用户与活动之间的多对多关系。通过这个表，系统可以：
1. 记录哪些用户参加了哪些活动
2. 统计每个活动的参与人数
3. 提供用户参与的活动历史记录
4. 防止用户重复报名同一活动

活动参与状态设计为可取消，这样用户可以取消已报名的活动而无需删除记录，便于数据统计和历史追踪。同时，外键约束确保了数据完整性，当用户或活动被删除时，相关的参与记录也会被级联删除。

### 14. 资源点赞表(resource_likes)
| 字段名 | 类型 | 描述 | 约束 |
| --- | --- | --- | --- |
| id | INT | 点赞ID | PRIMARY KEY, AUTO_INCREMENT |
| resource_id | INT | 资源ID | FOREIGN KEY, NOT NULL |
| user_id | INT | 用户ID | FOREIGN KEY, NOT NULL |
| created_at | DATETIME | 点赞时间 | NOT NULL |

**说明**：资源点赞表用于记录用户对资源的点赞信息，类似于帖子点赞表，用于防止重复点赞和显示资源的点赞状态。通过唯一键(resource_id, user_id)约束确保每个用户只能对同一资源点赞一次。同时，外键约束保证了数据完整性，当资源或用户被删除时，相关的点赞记录也会被级联删除。资源点赞数统计可以用于热门资源排行。

## 系统架构图
```
+----------------+     +----------------+     +-----------------+
|                |     |                |     |                 |
|  前端(Vue3)     |<--->|  后端(Spring)  |<--->|  数据库(MySQL)   |
|                |     |                |     |                 |
+----------------+     +----------------+     +-----------------+
                             |
                             v
                +-------------------------+
                |      其他组件/服务       |
                | (WebSocket/Elasticsearch)|
                +-------------------------+
```

## 开发与部署

### 开发环境
- 操作系统：Windows/MacOS/Linux
- IDE：VS Code/IntelliJ IDEA
- 数据库工具：MySQL Workbench
- 版本管理：Git

### 部署方式
- 前端：Nginx静态文件服务
- 后端：Java应用服务器(Tomcat)
- 数据库：MySQL独立部署

**注意事项：** 这是一个毕业设计项目，功能设计应合理适中，既不过于复杂也不过于简单。实际开发中可根据时间和能力进行适当调整。

## 开发流程

### 1. 需求分析与设计阶段 (2周)
- 需求分析：明确系统功能和用户需求
- 系统设计：技术选型、架构设计、数据库设计
- 原型设计：UI/UX设计，制作页面原型
- 接口设计：定义前后端交互的API接口

### 2. 开发环境搭建 (1周)
- 前端环境：安装Node.js，配置Vue3开发环境
- 后端环境：配置Java开发环境，搭建SpringBoot项目
- 数据库环境：安装配置MySQL，创建数据库及表结构
- 版本控制：建立Git仓库，配置分支管理策略

### 3. 后端开发 (3周)
- 基础框架：搭建基本项目结构，配置MyBatis
- 用户模块：实现用户注册、登录、个人信息管理
- 内容模块：实现帖子、评论、资源的CRUD操作
- 社交模块：实现好友关系、私信功能
- 管理模块：实现管理员功能

### 4. 前端开发 (3周)
- 页面布局：实现各页面的基本框架和布局
- 组件开发：开发公共组件(导航栏、卡片、表单等)
- 页面功能：实现各页面的具体功能
- 数据交互：与后端API对接，处理数据流
- 用户界面优化：提升用户体验和页面美观度

### 5. 实时通信与搜索功能 (1周)
- WebSocket：实现私信和通知的实时推送
- ElasticSearch：配置搜索引擎，实现全文搜索功能

### 6. 测试与优化 (2周)
- 单元测试：对核心功能进行单元测试
- 集成测试：测试各模块之间的交互
- 用户体验测试：收集用户反馈，优化界面和功能
- 性能测试：进行基础的性能测试，确保系统稳定运行

### 7. 部署与上线 (1周)
- 服务器配置：准备部署环境
- 应用部署：部署前端和后端应用
- 数据库部署：配置生产环境数据库
- 监控与维护：配置日志和监控系统

### 8. 文档编写与答辩准备 (1周)
- 项目文档：编写项目说明文档、API文档、用户手册
- 部署文档：编写部署指南和配置说明
- 答辩准备：准备演示材料和答辩内容

**总周期：约14周(3-4个月)**