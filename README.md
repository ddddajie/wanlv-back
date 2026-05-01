# wanlv-back 后端功能说明

`wanlv-back` 是万旅项目的 Spring Boot 后端服务，主要负责用户管理、景区地图业务、Agent 聊天、会话日报总结、用户数字画像以及内部表结构查询等能力。项目接口文档集中放在 `docs` 目录下，本文档用于快速了解当前后端已实现功能、运行配置和主要接口入口。

## 技术栈

- Java 17
- Spring Boot 3.5.13
- Spring MVC
- MyBatis
- MySQL
- PageHelper
- Druid
- Lombok
- Fastjson
- JJWT
- Spring Data Redis
- Apache HttpClient

## 本地运行

默认配置见 `src/main/resources/application.yml`。

```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/wanlv?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

启动前请确认：

1. 本地 MySQL 已创建 `wanlv` 数据库并导入项目所需表结构。
2. 数据库账号密码与 `application.yml` 保持一致，或按本地环境修改配置。
3. 如需使用 Agent 相关功能，确保 Agent 服务运行在 `http://127.0.0.1:8000`，或修改 `wanlv.agent.base-url`。

常用命令：

```bash
mvn spring-boot:run
```

或先编译：

```bash
mvn clean compile
```

服务启动后默认访问地址：

```text
http://127.0.0.1:8080
```

## 统一响应结构

当前接口统一返回 `Result<T>`：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

说明：

- `code = 200` 表示业务成功。
- `code = 500` 表示业务失败。
- 前端需要优先根据响应体中的 `code` 判断成功或失败，不要只依赖 HTTP 状态码。
- 分页接口返回 `PageResult`，结构通常为 `{ "total": 0, "records": [] }`。

## 已实现功能

### 用户与账号管理

用户模块提供管理员和普通用户的初始化、登录、注册、新增、更新、详情查询和分页查询能力。

主要能力：

- 初始化默认超级管理员。
- 管理员登录。
- 超级管理员新增管理员。
- 普通用户注册与登录。
- 管理员信息更新、详情查询、分页查询。
- 普通用户信息更新、详情查询、分页查询。
- 密码入库前会进行加密处理。
- 查询结果不会返回密码字段。

接口入口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET/POST | `/user/init` | 初始化超级管理员 |
| POST | `/user/admin/login` | 管理员登录 |
| POST | `/user/admin/add` | 新增管理员 |
| POST | `/user/normal/register` | 普通用户注册 |
| POST | `/user/normal/login` | 普通用户登录 |
| PUT | `/user/admin/update` | 更新管理员信息 |
| PUT | `/user/normal/update` | 更新普通用户信息 |
| GET | `/user/admin/{id}` | 查询管理员详情 |
| GET | `/user/normal/{id}` | 查询普通用户详情 |
| GET | `/user/admin/page` | 管理员分页列表 |
| GET | `/user/normal/page` | 普通用户分页列表 |

详细文档：

- `docs/frontend-api.md`
- `docs/user-management-api.md`

### 地图业务

地图模块面向前端地图页面和后台景区数据维护，支持景区、景点、路线、路线几何、空间要素和交互日志等业务。

主要能力：

- 景区基础信息管理。
- 景点 POI 管理。
- 旅游路线管理。
- 路线几何数据管理。
- 根据路线景点和道路空间要素自动生成路线轨迹。
- 景区边界、道路、区域等空间要素管理。
- 地图初始化数据聚合返回。
- 景点详情和路线详情查询。
- 地图交互日志记录。

接口入口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/map/scenic-areas` | 新增景区 |
| PUT | `/map/scenic-areas` | 更新景区 |
| GET | `/map/scenic-areas/page` | 景区分页查询 |
| GET | `/map/scenic-areas/{id}` | 景区详情 |
| POST | `/map/spots` | 新增景点 |
| PUT | `/map/spots` | 更新景点 |
| GET | `/map/spots/page` | 景点分页查询 |
| GET | `/map/spots/{id}` | 景点详情 |
| POST | `/map/routes` | 新增路线 |
| PUT | `/map/routes` | 更新路线 |
| GET | `/map/routes/page` | 路线分页查询 |
| GET | `/map/routes/{id}` | 路线详情 |
| POST | `/map/route-geos` | 新增路线几何 |
| POST | `/map/routes/{routeId}/geo/generate` | 自动生成路线轨迹 |
| PUT | `/map/route-geos` | 更新路线几何 |
| GET | `/map/route-geos/route/{routeId}` | 查询路线几何版本列表 |
| POST | `/map/geo-features` | 新增空间要素 |
| PUT | `/map/geo-features` | 更新空间要素 |
| GET | `/map/geo-features` | 查询景区空间要素 |
| GET | `/map/init/{scenicAreaId}` | 地图初始化数据 |
| POST | `/map/interaction-logs` | 记录地图交互日志 |

详细文档：

- `docs/map-api.md`

### Agent 聊天

Agent 聊天模块负责接收前端问题，维护本地会话和消息记录，并将请求转发给独立 Agent 服务。

主要流程：

1. 根据 `userId + reportDate` 创建或复用当天会话。
2. 将用户消息写入 `visitor_message`。
3. 调用 Agent 服务 `/chat`。
4. 将 Agent 回复写入 `visitor_message`。
5. 返回回答内容、会话信息和景区识别结果。
6. 如用户已有数字画像，会在调用 Agent 时携带 `user_profile`。

接口入口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/agent/chat` | 用户聊天提问 |
| POST | `/agent/session/scenic-area/bind` | 将当天会话绑定到指定景区 |

详细文档：

- `docs/agent-chat-api.md`
- `docs/agent-user-profile-integration.md`

### 会话日报总结

会话日报模块用于对用户当天聊天记录进行总结分析，支持手动触发和定时批量执行。

主要能力：

- 单个用户某天会话日报总结。
- 按日期批量总结当天所有会话。
- 将总结结果回写到 `visitor_session`。
- 支持 `forceReanalyze` 强制重新分析。
- 定时任务默认每天 `23:59:59` 按 `Asia/Shanghai` 时区执行。
- 日报生成后会刷新用户数字画像。

接口入口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/agent/session-analysis` | 单个用户日报总结 |
| POST | `/agent/session-analysis/daily` | 按日期批量日报总结 |

注意：日报相关接口当前通过请求体中的超级管理员账号密码进行权限校验。

定时任务配置：

```yaml
wanlv:
  agent:
    daily-analysis-cron: "59 59 23 * * ?"
    daily-analysis-zone: Asia/Shanghai
```

详细文档：

- `docs/session-daily-analysis-api.md`

### 用户数字画像

用户数字画像模块基于会话日报分析结果沉淀用户偏好，供后续 Agent 聊天时进行个性化回答。

主要能力：

- 查询某个普通用户的数字画像。
- 在日报分析后更新画像数据。
- 在 `/agent/chat` 转发给 Agent 服务时追加可选字段 `user_profile`。

接口入口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/agent/digital-profile/{userId}` | 查询用户数字画像 |

画像字段包括兴趣标签、关注主题、服务需求、知识缺口、出游风格、活跃度、情绪倾向、平均情绪分、画像评分、来源会话数和最近分析日期等。

详细文档：

- `docs/agent-user-profile-integration.md`

### 内部表结构查询

内部接口用于查询当前数据库表结构，方便 Agent 或内部工具了解数据表、字段和注释。

接口入口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/internal/schema/tables` | 查询数据库表结构 |

可选参数：

- `tableNames`：指定要查询的表名列表，不传时返回可查询范围内的表结构。

内部接口配置：

```yaml
wanlv:
  internal:
    enabled: true
    token: ${WANLV_INTERNAL_TOKEN:change-this-internal-token}
    header-name: X-Internal-Token
    allow-loopback: true
    allow-private-network: true
```

## 配置说明

### Agent 服务

```yaml
wanlv:
  agent:
    base-url: http://127.0.0.1:8000
    connect-timeout-ms: 600000
    read-timeout-ms: 600000
```

当前后端会调用 Agent 服务完成聊天和会话分析。开发联调时请确保 Agent 服务地址、端口和接口协议一致。

### 内部接口鉴权

内部表结构接口由 `InternalApiInterceptor` 保护，配置项位于 `wanlv.internal` 下。默认允许本机和私有网络访问，并支持通过 `X-Internal-Token` 请求头传递内部 token。

## 文档目录

| 文档 | 内容 |
| --- | --- |
| `docs/frontend-api.md` | 前端基础登录注册接口文档 |
| `docs/user-management-api.md` | 用户管理接口文档 |
| `docs/map-api.md` | 地图业务接口联调文档 |
| `docs/agent-chat-api.md` | Agent 聊天接口文档 |
| `docs/session-daily-analysis-api.md` | 会话日报总结接口文档 |
| `docs/agent-user-profile-integration.md` | Agent 接入用户数字画像说明 |

## 主要代码结构

```text
src/main/java/com/example/wanlvback
├── config        配置类
├── constant      常量
├── context       上下文
├── controller    接口控制器
├── exception     业务异常
├── handler       全局异常处理
├── interceptor   拦截器
├── mapper        MyBatis Mapper
├── pojo          DTO、Entity、VO
├── result        统一响应对象
├── service       业务服务接口与实现
├── task          定时任务
└── utils         工具类
```

## 联调建议

1. 先调用 `/user/init` 初始化超级管理员。
2. 使用管理员或普通用户登录接口验证账号能力。
3. 地图页面优先调用 `/map/init/{scenicAreaId}` 获取景区、空间要素、景点和路线聚合数据。
4. Agent 聊天前确认 Agent 服务已启动。
5. 日报总结接口需要超级管理员账号密码。
6. 前端统一封装请求时，优先判断响应体 `code === 200`。
