# 论坛项目技术文档

## 1. 项目概述

### 1.1 项目简介

本项目是一套前后端分离的社区论坛系统，支持用户注册登录、发帖回帖、点赞收藏、私信聊天、实时推送、管理后台等核心功能，适用于技术社区、兴趣小组等场景。

### 1.2 技术选型

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端框架 | Vue 3 + TypeScript | 3.4+ |
| 构建工具 | Vite | 5.x |
| UI 组件库 | Element Plus | 2.x |
| 状态管理 | Pinia | 2.x |
| 路由 | Vue Router | 4.x |
| HTTP 客户端 | Axios | 1.x |
| WebSocket 客户端 | STOMP.js | 7.x |
| Markdown 渲染 | marked | 12.x |
| 后端框架 | Spring Boot 3 | 3.2.5 |
| ORM | MyBatis-Plus | 3.5.6 |
| 安全框架 | Spring Security | 6.x |
| 认证 | JWT (jjwt) | 0.12.5 |
| 实时通信 | Spring WebSocket + STOMP | - |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 7.x |
| 对象存储 | 阿里云 OSS | 3.17.4 |
| 构建工具 | Maven | 3.9+ |
| Java 版本 | JDK 17 | - |

---

## 2. 系统架构

### 2.1 总体架构

```
┌──────────────────────────────────────────┐
│           Nginx (反向代理)                 │
├──────────────────┬───────────────────────┤
│    Frontend      │       Backend         │
│    Vue 3 SPA     │    Spring Boot 3      │
│    Port: 5173    │    Port: 8082         │
├──────────────────┴───────────────────────┤
│   MySQL  ←──→  Redis                     │
│   (持久化)     (缓存)                     │
├──────────────────────────────────────────┤
│   阿里云 OSS (图片存储)                    │
│   STOMP over WebSocket (实时推送)         │
└──────────────────────────────────────────┘
```

### 2.2 后端分层架构

```
controller/    -- 接口层 (Spring MVC)，参数校验、响应封装
service/       -- 业务逻辑层 (Spring Service)
mapper/        -- 数据访问层 (MyBatis-Plus BaseMapper)
model/
  ├── entity/  -- 数据库实体 (Lombok + @TableName)
  └── dto/     -- 请求/响应 DTO + WebSocket 事件
security/      -- JWT 签发/验证 + Spring Security Filter
oss/           -- 阿里云 OSS 上传服务
config/        -- 配置类 (Security, MyBatisPlus, WebSocket, Web, 数据初始化)
common/        -- 公共类 (统一响应、异常处理、密码工具)
```

---

## 3. 功能模块

```
论坛系统
├── 用户模块
│   ├── 注册 / 登录 (JWT AccessToken + RefreshToken)
│   ├── 个人信息编辑
│   ├── 密码修改 (bcrypt cost=12)
│   └── 头像上传 (阿里云 OSS)
├── 帖子模块
│   ├── 帖子发布 (Markdown + 工具栏)
│   ├── 帖子列表 (分页 / 排序 / 筛选 / 用户过滤)
│   ├── 帖子详情
│   ├── 帖子编辑 / 删除
│   ├── 帖子搜索
│   └── 图片上传 (OSS)
├── 回复模块
│   ├── 回复帖子 / 回复评论 (嵌套树形展示)
│   ├── Emoji 表情选择器 (6 分类 300+ 表情)
│   ├── 底部固定回复栏 (贴吧风格)
│   └── 回复删除
├── 互动模块
│   ├── 帖子点赞 / 取消 + 评论点赞 / 取消
│   ├── 帖子收藏 / 取消
│   └── 浏览计数
├── 私信模块
│   ├── 发送私信 / 对话记录
│   ├── 最近联系人列表 / 未读计数
│   └── Emoji 表情支持
├── 通知模块
│   ├── 回复 / 点赞 / 收藏 / 私信通知
│   ├── 通知列表页 (已读/未读/跳转)
│   ├── 铃铛红点实时更新
│   └── 全部已读
├── 实时推送 (WebSocket + STOMP)
│   ├── 新评论即时推送
│   ├── 点赞数即时更新
│   ├── 新通知即时推送
│   └── 新私信即时推送
├── 管理模块
│   ├── 用户管理 (列表 / 禁用启用)
│   ├── 帖子管理 (搜索 / 置顶 / 删除)
│   ├── 板块管理 (新增 / 编辑 / 删除)
│   └── 默认管理员 admin/admin123
└── 个人主页
    ├── 帖子数 / 回复数 / 获赞数 / 私信入口
    └── 近期帖子列表 (可点击跳转)
```

---

## 4. 数据库设计

### 4.1 ER 图 (核心表)

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│   user   │     │   post   │     │ category │
├──────────┤     ├──────────┤     ├──────────┤
│ id (PK)  │────<│ user_id  │     │ id (PK)  │
│ username │     │ category ├────<│ name     │
│ email    │     │ title    │     │ slug     │
│ password │     │ content  │     └──────────┘
│ avatar   │     │ status   │
│ role     │     │ view_cnt │
└──────────┘     └──────────┘
     │                 │
     ├──<── message ───┤
     │    from_id      │
     │    to_id        │
     │    content      │
     │    ┌────────────┤
     ├───<│  comment   │     ┌──────────┐
     │    ├────────────┤     │  like    │
     │    │ id (PK)    │     ├──────────┤
     │    │ post_id    │     │ user_id  │
     │    │ user_id    │     │ target_type
     │    │ parent_id  │     │ target_id│
     │    │ content    │     └──────────┘
     │    └────────────┘
     │    ┌──────────┐     ┌───────────┐
     └───<│ favorite │     │notification│
          ├──────────┤     ├───────────┤
          │ user_id  │     │ user_id   │
          │ post_id  │     │ type      │
          └──────────┘     │ content   │
                           │ is_read   │
                           └───────────┘
```

### 4.2 核心建表语句

```sql
CREATE TABLE `user` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `username`   VARCHAR(64)  NOT NULL UNIQUE,
    `email`      VARCHAR(128) NOT NULL UNIQUE,
    `password`   VARCHAR(256) NOT NULL,
    `avatar`     VARCHAR(512) DEFAULT '',
    `bio`        VARCHAR(512) DEFAULT '',
    `role`       TINYINT      DEFAULT 0 COMMENT '0-普通用户 1-管理员',
    `status`     TINYINT      DEFAULT 1 COMMENT '1-正常 0-禁用',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `category` (
    `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(64) NOT NULL,
    `slug`        VARCHAR(64) NOT NULL UNIQUE,
    `description` VARCHAR(256) DEFAULT '',
    `sort`        INT DEFAULT 0,
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `post` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT UNSIGNED NOT NULL,
    `category_id` INT UNSIGNED NOT NULL,
    `title`       VARCHAR(256) NOT NULL,
    `content`     LONGTEXT     NOT NULL,
    `content_raw` LONGTEXT     NOT NULL COMMENT '原始 Markdown',
    `status`      TINYINT DEFAULT 1 COMMENT '1-正常 0-删除 2-置顶',
    `view_count`  BIGINT DEFAULT 0,
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_category` (`category_id`),
    INDEX `idx_created_at` (`created_at`),
    FULLTEXT INDEX `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `comment` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `post_id`    BIGINT UNSIGNED NOT NULL,
    `user_id`    BIGINT UNSIGNED NOT NULL,
    `parent_id`  BIGINT UNSIGNED DEFAULT 0 COMMENT '0-回复帖子 其他-回复评论',
    `reply_to`   BIGINT UNSIGNED DEFAULT 0 COMMENT '被回复的用户ID',
    `content`    TEXT NOT NULL,
    `status`     TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_post_id` (`post_id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `like` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT UNSIGNED NOT NULL,
    `target_type` TINYINT NOT NULL COMMENT '1-帖子 2-回复',
    `target_id`   BIGINT UNSIGNED NOT NULL,
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `idx_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `favorite` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT UNSIGNED NOT NULL,
    `post_id`    BIGINT UNSIGNED NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `idx_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `notification` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT UNSIGNED NOT NULL,
    `from_id`    BIGINT UNSIGNED DEFAULT 0,
    `type`       TINYINT NOT NULL COMMENT '1-回复 2-点赞 3-系统通知 4-私信',
    `content`    VARCHAR(512) NOT NULL,
    `target_id`  BIGINT UNSIGNED DEFAULT 0,
    `is_read`    TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `message` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `from_id`    BIGINT UNSIGNED NOT NULL,
    `to_id`      BIGINT UNSIGNED NOT NULL,
    `content`    TEXT NOT NULL,
    `is_read`    TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_from_to` (`from_id`, `to_id`),
    INDEX `idx_to_read` (`to_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 默认板块
INSERT INTO `category` (`name`, `slug`, `description`, `sort`) VALUES
('技术讨论', 'tech', '技术相关话题', 1),
('项目反馈', 'feedback', '项目建议与反馈', 2),
('职场交流', 'career', '职场经验分享', 3),
('资源分享', 'resources', '学习资源与工具', 4),
('灌水闲聊', 'chat', '轻松闲聊区', 5);
```

---

## 5. API 设计

### 5.1 通用规范

| 项目 | 规范 |
|------|------|
| 协议 | HTTP + WebSocket |
| 格式 | JSON (snake_case) |
| 认证 | Header `Authorization: Bearer <token>` |
| 版本 | `/api/v1/` |

### 5.2 统一响应格式

```json
{
    "code": 0,
    "message": "ok",
    "data": {}
}
```

**状态码约定：**

| code | 含义 |
|------|------|
| 0 | 成功 |
| 40001 | 参数错误 |
| 40100 | 未认证 (Token 缺失/过期) |
| 40300 | 无权限 |
| 40400 | 资源不存在 |
| 50000 | 服务端错误 |

### 5.3 接口清单

#### 用户模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/v1/user/register` | 注册 | 否 |
| POST | `/api/v1/user/login` | 登录 | 否 |
| GET | `/api/v1/user/profile` | 获取个人信息 | 是 |
| PUT | `/api/v1/user/profile` | 修改个人信息 | 是 |
| PUT | `/api/v1/user/password` | 修改密码 | 是 |
| POST | `/api/v1/user/avatar` | 上传头像 (OSS) | 是 |
| GET | `/api/v1/user/:id` | 用户公开信息 (获赞数+帖子列表) | 否 |

#### 板块模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/v1/categories` | 板块列表 | 否 |

#### 帖子模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/v1/posts` | 帖子列表(分页/排序/筛选/用户过滤) | 否 |
| GET | `/api/v1/post/:id` | 帖子详情 | 否 |
| POST | `/api/v1/post` | 发布帖子 | 是 |
| PUT | `/api/v1/post/:id` | 编辑帖子 | 是 |
| DELETE | `/api/v1/post/:id` | 删除帖子 | 是 |
| GET | `/api/v1/posts/search` | 搜索帖子 | 否 |

**帖子列表请求参数：**

```
GET /api/v1/posts?page=1&page_size=20&category_id=1&sort=latest&keyword=xxx&user_id=1
```

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 (默认 1) |
| page_size | int | 每页条数 (默认 20, 最大 50) |
| category_id | int | 板块ID (可选) |
| sort | string | `latest` 最新 / `hot` 最热 |
| keyword | string | 搜索关键词 (可选) |
| user_id | int | 用户ID (可选, "我的帖子"功能用) |

#### 回复模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/v1/comments?post_id=:id` | 回复列表 (嵌套树形) | 否 |
| POST | `/api/v1/comment` | 发表回复 | 是 |
| DELETE | `/api/v1/comment/:id` | 删除回复 | 是 |

#### 互动模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/v1/like` | 点赞 / 取消 | 是 |
| POST | `/api/v1/favorite` | 收藏 / 取消 | 是 |
| GET | `/api/v1/favorites` | 收藏列表 | 是 |

**点赞请求体：**
```json
{ "target_type": 1, "target_id": 123 }
```
> target_type: 1=帖子, 2=回复。同一接口无感切换点赞/取消。

#### 上传模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/v1/upload/image` | 通用图片上传 (OSS) | 是 |

#### 通知模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/v1/notifications` | 通知列表 (含未读数) | 是 |
| GET | `/api/v1/notifications/unread` | 未读通知数 | 是 |
| PUT | `/api/v1/notification/:id/read` | 标记已读 | 是 |
| PUT | `/api/v1/notifications/read-all` | 全部已读 | 是 |

**通知类型 (type)：**

| type | 含义 | 触发场景 |
|------|------|----------|
| 1 | 回复 | 有人回复了你的帖子/评论 |
| 2 | 点赞 | 有人点赞了你的帖子/评论/收藏了你的帖子 |
| 3 | 系统通知 | 预留 |
| 4 | 私信 | 有人给你发了私信 |

#### 私信模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/v1/message` | 发送私信 | 是 |
| GET | `/api/v1/messages?with_user_id=:id` | 对话记录 (分页) | 是 |
| GET | `/api/v1/messages/conversations` | 最近联系人列表 | 是 |
| GET | `/api/v1/messages/unread` | 未读私信数 | 是 |

#### 管理模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/v1/admin/users` | 用户列表 | 管理员 |
| PUT | `/api/v1/admin/user/:id/status` | 禁用/启用用户 | 管理员 |
| GET | `/api/v1/admin/posts` | 帖子列表 (筛选) | 管理员 |
| DELETE | `/api/v1/admin/post/:id` | 删除帖子 | 管理员 |
| PUT | `/api/v1/admin/post/:id/pin` | 置顶/取消置顶 | 管理员 |
| GET | `/api/v1/admin/categories` | 板块列表 | 管理员 |
| POST | `/api/v1/admin/category` | 新增板块 | 管理员 |
| PUT | `/api/v1/admin/category/:id` | 编辑板块 | 管理员 |
| DELETE | `/api/v1/admin/category/:id` | 删除板块 | 管理员 |

---

## 6. WebSocket 实时推送

### 6.1 架构

```
┌──────────────┐  STOMP over WebSocket  ┌─────────────────┐
│   Spring      │ ←───────────────────→  │  Vue 3 前端      │
│   WebSocket   │   /ws endpoint         │  @stomp/stompjs  │
│   STOMP       │                        │                  │
│ + 原生WS      │                        │                  │
└──────────────┘                        └─────────────────┘
```

### 6.2 事件路由

| 后端 Topic | 事件类型 | 订阅方 | 效果 |
|------------|----------|--------|------|
| `/topic/post/{postId}` | `new_comment` | PostDetail | 新评论即时追加 |
| `/topic/post/{postId}` | `like_changed` | PostDetail | 点赞数/状态即时更新 |
| `/topic/user/{userId}` | `new_notification` | AppHeader | 铃铛红点实时刷新 |
| `/topic/message/{userId}` | `new_message` | AppHeader | 私信未读实时通知 |

### 6.3 推送触发点

| Service | 时机 | 推送内容 |
|---------|------|----------|
| CommentService.create() | 发表回复 | new_comment 事件 |
| LikeService.toggle() | 点赞/取消 | like_changed 事件 (含最新 count) |
| NotificationService.create() | 创建通知 | new_notification 事件 |
| MessageService.send() | 发送私信 | new_message 事件 |

---

## 7. 后端核心设计

### 7.1 项目目录结构

```
backend-java/
├── pom.xml
├── Dockerfile
├── src/main/java/com/myforum/
│   ├── MyForumApplication.java
│   ├── common/
│   │   ├── Result.java             -- 统一响应体
│   │   ├── BusinessException.java  -- 业务异常
│   │   ├── GlobalExceptionHandler.java
│   │   └── PasswordUtils.java      -- bcrypt (cost=12)
│   ├── config/
│   │   ├── SecurityConfig.java     -- Spring Security (JWT + URL 级授权)
│   │   ├── MyBatisPlusConfig.java  -- 分页 + 自动填充
│   │   ├── WebConfig.java          -- CORS + 静态资源
│   │   ├── WebSocketConfig.java    -- STOMP + 原生 WebSocket
│   │   └── DataInitializer.java    -- 默认管理员初始化
│   ├── model/
│   │   ├── entity/                  -- 8 个实体 (User/Post/Comment/Category/Like/Favorite/Notification/Message)
│   │   └── dto/                     -- 请求/响应 DTO + WsEvent
│   ├── mapper/                      -- 8 个 MyBatis-Plus Mapper
│   ├── service/                     -- 8 个 Service (含 Notification/Message)
│   ├── controller/                  -- 12 个 Controller
│   ├── security/
│   │   ├── JwtTokenProvider.java    -- JWT 签发/验证
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtClaims.java
│   └── oss/
│       ├── OssConfig.java           -- OSS 配置绑定
│       └── OssService.java          -- OSS 上传 (按日期组织路径)
├── src/main/resources/
│   ├── application.yml
│   └── init.sql                     -- 8 张表 + 默认板块数据
```

### 7.2 认证流程

```
1. 用户登录 → 验证用户名 + bcrypt 密码
2. 签发 AccessToken (2h) + RefreshToken (7d)
3. 前端 AccessToken 存 sessionStorage, 每次请求自动附加 Header
4. JwtAuthenticationFilter 拦截 → 解析 → 设置 SecurityContext
5. AccessToken 过期 → 返回 40100 → 前端跳转登录页
6. 管理员路由 SecurityConfig 中 URL 级 `.hasRole("ADMIN")`
```

### 7.3 请求处理链

```
请求 → CORS → JWT Filter → Spring Security (URL 授权) → Controller
                                                           ↓
                                          GlobalExceptionHandler (JSON 异常响应)
```

### 7.4 JSON 命名约定

`spring.jackson.property-naming-strategy: SNAKE_CASE` 全局 snake_case。Query 参数 `@RequestParam("snake_name")` 显式映射。

### 7.5 默认管理员

`DataInitializer` 启动时检查，无管理员则创建：`admin` / `admin123`。

---

## 8. 前端设计

### 8.1 项目目录结构

```
frontend/
├── index.html
├── package.json
├── vite.config.ts                    -- Vite + 代理 (→ :8082)
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── api/                          -- 6 个 API 模块
│   │   ├── request.ts                -- Axios + 拦截器 (sessionStorage token)
│   │   ├── user.ts / post.ts / comment.ts
│   │   ├── interaction.ts / notification.ts / message.ts
│   ├── router/index.ts               -- 11 条路由 + auth/admin 守卫
│   ├── stores/
│   │   ├── user.ts                   -- 用户状态 (sessionStorage)
│   │   └── app.ts                    -- 通知未读数 (Pinia 响应式)
│   ├── views/                        -- 10 个页面
│   │   ├── Home.vue                  -- 首页帖子流
│   │   ├── PostDetail.vue            -- 帖子详情 (WebSocket 订阅)
│   │   ├── PostCreate.vue            -- 发帖/编辑
│   │   ├── MyPosts.vue               -- 我的帖子
│   │   ├── Login.vue / Register.vue
│   │   ├── UserProfile.vue           -- 用户主页 (统计+帖子列表+私信)
│   │   ├── Settings.vue              -- 个人设置
│   │   ├── Notifications.vue         -- 通知列表
│   │   ├── Messages.vue              -- 私信列表
│   │   └── admin/AdminDashboard.vue  -- 管理后台 (3 tab)
│   ├── components/                   -- 9 个组件
│   │   ├── AppHeader.vue             -- 导航栏 (铃铛+WebSocket订阅)
│   │   ├── AppFooter.vue
│   │   ├── PostCard.vue              -- 帖子卡片
│   │   ├── CommentList.vue           -- 回复列表 (嵌套+高亮)
│   │   ├── MarkdownEditor.vue        -- Markdown 编辑器 (工具栏)
│   │   ├── MarkdownViewer.vue        -- Markdown 渲染
│   │   ├── EmojiPicker.vue           -- Emoji 表情选择器
│   │   ├── Pagination.vue
│   │   └── UserAvatar.vue
│   ├── composables/
│   │   ├── useWebSocket.ts           -- WebSocket 连接/订阅/重连
│   │   ├── useAuth.ts / usePagination.ts / useLike.ts
│   └── utils/format.ts
```

### 8.2 路由设计

| 路径 | 组件 | 说明 |
|------|------|------|
| `/` | Home | 首页 |
| `/post/:id` | PostDetail | 帖子详情 (WebSocket 实时更新) |
| `/create` | PostCreate | 发帖 (需登录) |
| `/edit/:id` | PostCreate | 编辑 (需登录) |
| `/my-posts` | MyPosts | 我的帖子 (需登录) |
| `/login` | Login | 登录 |
| `/register` | Register | 注册 |
| `/user/:id` | UserProfile | 用户主页 (统计+私信入口) |
| `/settings` | Settings | 设置 (需登录) |
| `/notifications` | Notifications | 通知列表 (需登录) |
| `/messages` | Messages | 私信 (需登录) |
| `/admin` | AdminDashboard | 管理后台 (需管理员) |

### 8.3 存储策略

- **sessionStorage**：token + userInfo，每个 tab 独立登录态，关闭 tab 自动清除
- **Pinia store**：通知未读数，跨组件响应式共享

### 8.4 图标规范

| 位置 | 浏览 | 点赞 | 收藏 |
|------|------|------|------|
| 帖子卡片 | View | CaretTop | Star |
| 帖子详情 | View | CaretTop | Star |
| 评论列表 | — | CaretTop | — |

---

## 9. 部署架构

### 9.1 Docker Compose

```yaml
services:
  nginx:       # 反向代理 + 静态资源 (端口 80)
  backend:     # Spring Boot JAR (端口 8082)
  mysql:       # 数据库 (端口 3306)
  redis:       # 缓存 (端口 6379)
```

MySQL 启动时自动执行 `init.sql` 建表 + 插入默认板块。WebSocket `/ws` 路径需 Nginx 升级为 WebSocket 连接。

### 9.2 Nginx 路由

| 路径 | 处理方式 |
|------|----------|
| `/api/` | 反向代理到 `backend:8082` |
| `/ws` | WebSocket 代理到 `backend:8082` |
| `/uploads/` | 反向代理到 `backend:8082` |
| `/` | 静态文件 `frontend/dist`，SPA fallback |

### 9.3 后端配置

```yaml
server.port:        8082
spring.datasource:  MySQL 8.0 (HikariCP max=100)
spring.data.redis:  Redis 7
spring.jackson:     SNAKE_CASE
jwt:                HMAC-SHA256, access 2h / refresh 7d
oss:                阿里云 OSS (oss-cn-shenzhen, bucket: sky-deliver-666)
```

---

## 10. 安全设计

| 场景 | 措施 |
|------|------|
| 密码存储 | bcrypt, cost=12 |
| XSS | Markdown HTML 转义后存储 |
| SQL 注入 | MyBatis-Plus LambdaQueryWrapper 参数化 |
| CSRF | Spring Security 无状态 + JWT, CSRF 禁用 |
| 文件上传 | MultipartFile 10MB 限制, OSS 隔离 |
| 认证失败 | JSON 格式错误, 非空白 403 |
| 管理员路由 | SecurityConfig URL 级 `.hasRole("ADMIN")` |

---

## 11. 扩展性设计

- **通用点赞表**：`target_type` 区分帖子(1)和回复(2)
- **评论树**：`parent_id` 自引用，内存构建树形结构
- **通知系统**：type 1-回复 2-点赞 3-系统 4-私信，可扩展
- **角色体系**：role 0/1，预留多级权限
- **OSS 存储**：`dir/yyyy/MM/dd/uuid.ext` 按日期分区
- **会话隔离**：sessionStorage 多 tab 独立登录
- **WebSocket**：`/topic/post/` + `/topic/user/` + `/topic/message/`，新增场景只需加 topic
