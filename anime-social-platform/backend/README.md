# 动漫社交平台后端

## 项目概述
这是一个专为动漫爱好者设计的社交平台后端服务，提供完整的RESTful API支持，实现了用户管理、内容分享、社交互动、即时通讯等功能。本项目基于Spring Boot框架开发，采用Java 17版本，使用MySQL作为关系型数据库，并集成了Elasticsearch用于全文搜索功能。

## 技术栈
- **核心框架**: Spring Boot 3.2.3
- **ORM框架**: Spring Data JPA + MyBatis 3.0.3
- **数据库**: MySQL 8.0.33
- **安全框架**: Spring Security
- **搜索引擎**: Elasticsearch 8.12.1
- **消息队列**: Spring WebSocket (STOMP协议)
- **工具库**: 
  - Lombok (简化代码)
  - Jackson (JSON处理)
  - Spring Validation (数据验证)

## 功能模块

### 用户管理
- 用户注册与登录
- 用户信息管理
- 权限控制与认证
- 用户关系（关注、好友）

### 内容服务
- 帖子发布与管理
- 评论系统
- 标签分类
- 资源上传与分享
- 内容点赞与收藏

### 社交互动
- 好友关系管理
- 即时消息通讯
- 用户动态关注
- 通知系统

### 活动系统
- 活动创建与管理
- 活动参与
- 活动推荐

### 搜索服务
- 全文内容搜索
- 标签搜索
- 用户搜索

### 管理后台
- 用户管理
- 内容审核
- 资源管理
- 活动管理
- 系统设置

## 项目结构
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── animesocial/
│   │           └── platform/
│   │               ├── config/        # 配置类
│   │               ├── controller/    # 控制器层
│   │               ├── exception/     # 异常处理
│   │               ├── listener/      # 事件监听器
│   │               ├── model/         # 数据模型
│   │               ├── repository/    # 数据访问层
│   │               ├── service/       # 业务逻辑层
│   │               ├── util/          # 工具类
│   │               └── PlatformApplication.java  # 应用入口
│   │
│   └── resources/
│       ├── database/     # 数据库相关脚本
│       ├── dtd/          # DTD文件
│       ├── mapper/       # MyBatis映射文件
│       ├── uploads/      # 文件上传目录
│       └── application.properties  # 应用配置文件
│
└── test/                 # 测试代码
```

## 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Elasticsearch 8.x (可选，如不需要搜索功能可在配置中禁用)

## 构建与运行

### 数据库配置
1. 安装MySQL数据库
2. 创建数据库`anime_social`（如配置了`createDatabaseIfNotExist=true`则会自动创建）
3. 在`application.properties`中配置数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/anime_social?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### Elasticsearch配置（可选）
1. 安装Elasticsearch 8.x
2. 在`application.properties`中配置Elasticsearch连接信息：
```properties
spring.elasticsearch.rest.uris=http://localhost:9200
spring.elasticsearch.rest.username=elastic
spring.elasticsearch.rest.password=your_password
```

### 文件上传配置
在`application.properties`中配置文件上传路径和域名：
```properties
app.upload.dir=/path/to/upload/directory
app.file.domain=http://your-domain.com
```

### 构建项目
```bash
mvn clean package
```

### 运行项目
```bash
java -jar target/platform-1.0.0.jar
```

或使用Maven插件：
```bash
mvn spring-boot:run
```

## API文档
启动应用后，可通过以下地址访问Swagger API文档：

http://localhost:8080/swagger-ui.html

### 主要API端点

#### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/logout` - 用户登出
- `GET /api/auth/current` - 获取当前用户信息

#### 用户相关
- `GET /api/users/{id}` - 获取用户信息
- `PUT /api/users/{id}` - 更新用户信息
- `POST /api/users/follow/{id}` - 关注用户
- `DELETE /api/users/follow/{id}` - 取消关注用户
- `GET /api/users/{id}/followers` - 获取用户粉丝列表
- `GET /api/users/{id}/following` - 获取用户关注列表

#### 帖子相关
- `GET /api/posts` - 获取帖子列表
- `GET /api/posts/{id}` - 获取帖子详情
- `POST /api/posts` - 创建帖子
- `PUT /api/posts/{id}` - 更新帖子
- `DELETE /api/posts/{id}` - 删除帖子
- `POST /api/posts/{id}/like` - 点赞帖子
- `POST /api/posts/{id}/favorite` - 收藏帖子

#### 评论相关
- `GET /api/posts/{postId}/comments` - 获取帖子评论
- `POST /api/comments` - 发表评论
- `PUT /api/comments/{id}` - 更新评论
- `DELETE /api/comments/{id}` - 删除评论

#### 资源相关
- `GET /api/resources` - 获取资源列表
- `GET /api/resources/{id}` - 获取资源详情
- `POST /api/resources` - 上传资源
- `DELETE /api/resources/{id}` - 删除资源
- `GET /api/resources/{id}/download` - 下载资源

#### 消息相关
- `GET /api/messages/{userId}` - 获取与指定用户的消息历史
- `POST /api/messages` - 发送消息

#### 搜索相关
- `GET /api/search/posts?q={query}` - 搜索帖子
- `GET /api/search/users?q={query}` - 搜索用户
- `GET /api/search/resources?q={query}` - 搜索资源
- `GET /api/search/tags?q={query}` - 搜索标签

#### 活动相关
- `GET /api/events` - 获取活动列表
- `GET /api/events/{id}` - 获取活动详情
- `POST /api/events` - 创建活动
- `PUT /api/events/{id}` - 更新活动
- `DELETE /api/events/{id}` - 删除活动
- `POST /api/events/{id}/join` - 参加活动

#### 管理员相关
- `GET /api/admin/users` - 获取所有用户
- `PUT /api/admin/users/{id}/status` - 更新用户状态
- `GET /api/admin/posts` - 管理帖子
- `PUT /api/admin/posts/{id}/status` - 更新帖子状态
- `GET /api/admin/resources` - 管理资源
- `PUT /api/admin/resources/{id}/status` - 更新资源状态

## WebSocket支持
项目使用Spring WebSocket实现即时通讯功能，支持STOMP协议。

连接端点：`/ws`
订阅用户私人消息：`/user/queue/messages`
发送消息：`/app/chat`

## 安全性
- 使用Spring Security实现身份认证和权限控制
- 实现CSRF防护
- XSS过滤
- 会话管理
- 密码加密存储

## 开发指南

### 添加新的控制器
1. 在`controller`包下创建新的控制器类
2. 使用`@RestController`和`@RequestMapping`注解
3. 注入所需的服务
4. 实现API端点方法

示例:
```java
@RestController
@RequestMapping("/api/example")
public class ExampleController {

    private final ExampleService exampleService;
    
    @Autowired
    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ExampleDTO> getExample(@PathVariable Long id) {
        return ResponseEntity.ok(exampleService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<ExampleDTO> createExample(@Valid @RequestBody ExampleDTO example) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exampleService.save(example));
    }
}
```

### 创建新的服务
1. 在`service`包下创建服务接口
2. 在`service/impl`包下实现服务接口
3. 使用`@Service`注解标记实现类

### 添加新的数据模型
1. 在`model`包下创建实体类
2. 使用JPA注解进行ORM映射
3. 创建相应的DTO类进行数据传输

## 项目特点
- RESTful API设计
- 模块化架构
- 全面的异常处理
- 多级缓存策略
- 完善的日志系统
- 丰富的WebSocket支持
- 高性能的数据访问层设计 