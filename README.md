# wanlv-back 后端项目说明

`wanlv-back` 是万旅项目的 Spring Boot 后端服务，负责用户账号、景区地图、旅游路线、景点预约、Agent 聊天、会话日报、用户数字画像以及内部表结构查询等能力。项目详细接口文档集中放在 `docs` 目录下，根目录 README 用于快速了解项目结构、运行配置、核心模块和联调入口。

## 项目定位

当前后端主要承担三类职责：

- 面向前端页面提供用户登录、地图初始化、景点预约、路线查询、聊天提问等业务接口。
- 面向管理端提供用户管理、景区/景点/路线维护、预约规则维护、订单查询、预约看板等后台接口。
- 面向独立 Agent 服务提供聊天转发、会话分析、景点预约工具、定制路线生成、数据库表结构查询等内部协作能力。

重点说明：

- 项目统一使用 UTF-8 编码，中文注释和中文文档不要随意改成其他编码。
- 当前接口默认没有 `/api` 网关前缀，Controller 实际路径以 `src/main/java/com/example/wanlvback/controller` 为准。
- 预约模块后端前缀是 `/reservation`，不是旧文档中可能出现过的 `/api/reservation`。
- 普通用户账号密码注册接口在当前 Controller 中已停用，新普通用户以手机验证码登录自动注册为主。

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 基础框架 | Java 17、Spring Boot 3.5.13、Spring MVC、Spring Scheduling |
| 数据访问 | MyBatis、MySQL、Druid、PageHelper |
| 登录鉴权 | JWT、JJWT |
| 常用组件 | Lombok、Fastjson、Apache HttpClient、Spring Data Redis |
| 构建工具 | Maven |

## 代码结构

```text
src/main/java/com/example/wanlvback
├── config        配置类，包含 MVC 拦截器、JWT、内部接口配置
├── constant      常量
├── context       当前请求用户上下文 BaseContext
├── controller    REST 接口控制器
├── exception     业务异常
├── handler       全局异常处理
├── interceptor   JWT 鉴权和内部接口鉴权拦截器
├── mapper        MyBatis Mapper 接口
├── pojo          DTO、Entity、VO
├── result        统一响应对象
├── service       业务服务接口与实现
├── task          定时任务
└── utils         工具类
```

资源目录：

```text
src/main/resources
├── application.yml      本地默认配置
├── mapper/              MyBatis XML 映射文件
└── sql/wanlv.sql        数据库建表脚本和初始化数据
```

## 本地运行

默认配置见 `src/main/resources/application.yml`：

```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/wanlv?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

启动前请确认：

1. 本地 MySQL 已创建 `wanlv` 数据库。
2. 已导入 `src/main/resources/sql/wanlv.sql` 中的表结构和基础数据。
   如果是已有数据库，仅需额外执行 `src/main/resources/sql/normal_user_refresh_token_migration.sql` 创建刷新令牌表。
3. 数据库账号密码与 `application.yml` 一致，或按本地环境修改配置。
4. 如需联调 Agent 聊天、会话分析、Agent 预约或 Agent 路线功能，确保 Agent 服务运行在 `http://127.0.0.1:8000`，或修改 `wanlv.agent.base-url`。

常用命令：

```bash
mvn spring-boot:run
```

仅编译验证：

```bash
mvn clean compile
```

服务启动后默认访问地址：

```text
http://127.0.0.1:8080
```

## 核心配置

### JWT 登录态

```yaml
wanlv:
  jwt:
    secret-key: ${WANLV_JWT_SECRET:wanlv-jwt-secret-key-change-me-at-least-32-bytes}
    ttl-ms: ${WANLV_JWT_TTL_MS:7200000}
    refresh-ttl-ms: ${WANLV_REFRESH_TOKEN_TTL_MS:259200000}
    header-name: Authorization
    token-prefix: Bearer
```

前端请求受保护接口时使用：

```text
Authorization: Bearer <token>
```

普通用户 accessToken 默认有效期为 2 小时，refreshToken 默认有效期为 3 天。刷新成功后服务端会轮换 refreshToken，Web 与 Android 接入方式见 `docs/token-refresh-client-integration.md`。

### 内部接口鉴权

```yaml
wanlv:
  internal:
    enabled: true
    token: ${WANLV_INTERNAL_TOKEN:change-this-internal-token}
    header-name: X-Internal-Token
    allow-loopback: true
    allow-private-network: true
```

内部表结构接口走 `InternalApiInterceptor`，支持本机/私有网络放行，也支持通过 `X-Internal-Token` 请求头传递内部 token。

### Agent 服务

```yaml
wanlv:
  agent:
    base-url: http://127.0.0.1:8000
    connect-timeout-ms: 600000
    read-timeout-ms: 600000
    daily-analysis-cron: "59 59 23 * * ?"
    daily-analysis-zone: Asia/Shanghai
```

后端会调用 Agent 服务完成聊天、会话日报分析等能力。开发联调时请保持 Agent 服务地址、端口和接口协议一致。

### 定时任务

| 任务 | 默认配置 | 说明 |
| --- | --- | --- |
| 会话日报批量分析 | `wanlv.agent.daily-analysis-cron: 59 59 23 * * ?` | 每天 23:59:59 按 `Asia/Shanghai` 汇总当天会话 |
| 预约订单过期检测 | `wanlv.reservation.expire-cron: 0 */5 * * * ?` | 每 5 分钟扫描已过结束时间的预约订单并置为过期 |

## 统一响应结构

业务接口统一返回 `Result<T>`：

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
- JWT 拦截失败时会返回 `401` 业务码和 HTTP 401。
- 前端应优先根据响应体中的 `code` 判断成功或失败，不要只依赖 HTTP 状态码。
- 分页接口通常返回 `PageResult`，结构为 `{ "total": 0, "records": [] }`。

## 鉴权规则概览

`WebMvcConfig` 默认对 `/**` 开启 JWT 拦截，并排除以下公开或内部接口：

- 内部接口：`/internal/**`，由内部接口拦截器单独处理。
- 登录和初始化：`/user/init`、`/user/admin/login`、`/user/normal/login`、`/user/normal/code/send`、`/user/normal/code/login`、`/user/normal/token/refresh`、`/user/normal/logout`。
- Agent 工具接口：`/reservation/agent/**`、`/map/agent/**`。
- 公开查询接口：`/reservation/spots/enabled`、`/reservation/slots`、`/map/init/{scenicAreaId}`、`/map/scenic-areas/page`。

重点说明：当前请求身份只从已校验的 JWT 中读取，并写入 `BaseContext`，业务层根据 `userId`、`userType`、`role` 做权限和越权控制。

## 已实现业务模块

### 用户与账号管理

主要能力：

- 初始化默认超级管理员。
- 管理员登录、新增、更新、详情、删除、分页查询。
- 普通用户账号密码登录、手机号验证码发送、验证码登录/自动注册、Token 无感刷新和单设备退出。
- 普通用户实名认证、更新、详情、删除、分页查询。
- 超级管理员查看用户数字画像。
- 密码入库前加密，查询结果不返回密码字段。

主要接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET/POST` | `/user/init` | 初始化超级管理员 |
| `POST` | `/user/admin/login` | 管理员登录 |
| `POST` | `/user/admin/add` | 新增管理员 |
| `POST` | `/user/normal/login` | 普通用户账号密码登录 |
| `POST` | `/user/normal/code/send` | 发送手机验证码 |
| `POST` | `/user/normal/code/login` | 手机验证码登录/自动注册 |
| `POST` | `/user/normal/token/refresh` | 轮换普通用户 accessToken 和 refreshToken |
| `POST` | `/user/normal/logout` | 作废当前设备 refreshToken |
| `POST` | `/user/normal/real-name/verify` | 普通用户实名认证 |
| `PUT` | `/user/admin/update` | 更新管理员信息 |
| `PUT` | `/user/normal/update` | 更新普通用户信息 |
| `GET` | `/user/admin/page` | 管理员分页列表 |
| `GET` | `/user/normal/page` | 普通用户分页列表 |
| `GET` | `/user/admin/digital-profile` | 超级管理员分页查询用户画像 |
| `GET` | `/user/admin/digital-profile/{userId}` | 超级管理员查询用户画像 |

详细文档：

- `docs/frontend-api.md`
- `docs/jwt-auth-api.md`
- `docs/token-refresh-client-integration.md`
- `docs/user-management-api.md`
- `docs/real-name-reservation-frontend-api.md`

### 地图、景区与路线

主要能力：

- 景区基础信息管理。
- 景点 POI 管理。
- 旅游路线和路线景点管理。
- 路线几何数据管理。
- 根据路线景点和道路空间要素自动生成路线轨迹。
- Agent 根据景区名和有序景点名生成定制路线轨迹，并保存为用户最新专属路线。
- 景区边界、道路、区域等空间要素管理。
- 地图初始化数据聚合返回。
- 地图交互日志记录。

主要接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST/PUT` | `/map/scenic-areas` | 新增/更新景区 |
| `GET` | `/map/scenic-areas/page` | 景区分页查询 |
| `GET/DELETE` | `/map/scenic-areas/{id}` | 景区详情/删除 |
| `POST/PUT` | `/map/spots` | 新增/更新景点 |
| `GET` | `/map/spots/page` | 景点分页查询 |
| `GET/DELETE` | `/map/spots/{id}` | 景点详情/删除 |
| `POST/PUT` | `/map/routes` | 新增/更新路线 |
| `GET` | `/map/routes/page` | 路线分页查询 |
| `GET/DELETE` | `/map/routes/{id}` | 路线详情/删除 |
| `POST` | `/map/routes/{routeId}/geo/generate` | 根据路线景点自动生成轨迹 |
| `POST` | `/map/agent/routes/geo/generate` | Agent 生成定制路线轨迹 |
| `GET` | `/map/agent-route-geos/latest` | 查询用户最新专属路线 |
| `POST/PUT` | `/map/geo-features` | 新增/更新空间要素 |
| `GET` | `/map/geo-features` | 查询景区空间要素 |
| `GET` | `/map/init/{scenicAreaId}` | 地图初始化聚合数据 |
| `POST` | `/map/interaction-logs` | 记录地图交互日志 |

详细文档：

- `docs/map-api.md`
- `docs/agent-route-api.md`

### 景点预约

主要能力：

- 查询已开启预约的景点和可预约时段。
- 创建、查询、取消用户预约订单。
- 管理端维护预约规则、预约时段、订单列表。
- 管理端支持检票入场和预约运营看板。
- 定时扫描并更新已过期预约订单。

用户端接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/reservation/spots/enabled` | 查询已开启预约的景点列表 |
| `GET` | `/reservation/slots` | 查询指定景点某天可预约时段 |
| `POST` | `/reservation/orders` | 创建用户预约订单 |
| `GET` | `/reservation/orders/my` | 分页查询个人预约订单 |
| `POST` | `/reservation/orders/{reservationNo}/cancel` | 取消指定预约订单 |

管理端接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/reservation/admin/rules` | 新增景点预约规则 |
| `PUT` | `/reservation/admin/rules/{id}` | 更新预约规则 |
| `PUT` | `/reservation/admin/rules/{id}/status` | 启用或停用预约规则 |
| `GET` | `/reservation/admin/rules` | 分页查询预约规则 |
| `POST` | `/reservation/admin/slots/generate` | 根据规则批量生成预约时段 |
| `POST` | `/reservation/admin/slots` | 手动新增预约时段 |
| `PUT` | `/reservation/admin/slots/{id}` | 更新预约时段 |
| `GET` | `/reservation/admin/slots` | 分页查询预约时段 |
| `GET` | `/reservation/admin/orders` | 分页查询预约订单 |
| `POST` | `/reservation/admin/orders/{reservationNo}/enter` | 检票入场 |
| `GET` | `/reservation/admin/dashboard` | 预约运营看板 |

Agent 预约工具接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/reservation/agent/spots/enabled` | Agent 查询可预约景点 |
| `GET` | `/reservation/agent/spots/search` | Agent 根据名称搜索可预约景点 |
| `GET` | `/reservation/agent/slots/match` | Agent 查询指定时间附近可预约时段 |
| `GET` | `/reservation/agent/slots/recommend` | Agent 推荐指定日期范围内的可预约时段 |
| `POST` | `/reservation/agent/orders` | Agent 创建预约订单，后端固定 `sourceType = AGENT` |
| `GET` | `/reservation/agent/orders/recent` | Agent 查询用户近期预约订单 |
| `POST` | `/reservation/agent/orders/{reservationNo}/cancel` | Agent 取消预约订单 |

详细文档：

- `docs/spot-reservation-api.md`
- `docs/agent-reservation-api.md`
- `docs/real-name-reservation-frontend-api.md`

### Agent 聊天与会话

主要能力：

- 接收前端聊天问题。
- 按 `userId + reportDate` 创建或复用当天会话。
- 写入用户消息和 Agent 回复到 `visitor_message`。
- 调用独立 Agent 服务 `/chat`。
- 返回回答内容、会话信息和景区识别结果。
- 用户已有数字画像时，转发给 Agent 的请求会携带 `user_profile`。
- 支持将当天会话绑定到指定景区。

主要接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/agent/chat` | 用户聊天提问 |
| `POST` | `/agent/session/scenic-area/bind` | 将当天会话绑定到指定景区 |

详细文档：

- `docs/agent-chat-api.md`
- `docs/agent-user-profile-integration.md`

### 会话日报与用户数字画像

主要能力：

- 单个用户某天会话日报总结。
- 按日期批量总结当天所有会话。
- 将总结结果回写到 `visitor_session`。
- 支持 `forceReanalyze` 强制重新分析。
- 日报生成后刷新用户数字画像。
- 数字画像沉淀兴趣标签、关注主题、服务需求、知识缺口、出游风格、活跃度、情绪倾向等字段。

主要接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/agent/session-analysis` | 单个用户日报总结 |
| `POST` | `/agent/session-analysis/daily` | 按日期批量日报总结 |
| `GET` | `/agent/digital-profile/{userId}` | 查询用户数字画像 |

注意：日报相关接口当前需要超级管理员权限，具体规则以 `docs/jwt-auth-api.md` 和 Controller/Service 实现为准。

详细文档：

- `docs/session-daily-analysis-api.md`
- `docs/agent-user-profile-integration.md`

### 内部表结构查询

内部接口用于查询当前数据库表结构，方便 Agent 或内部工具了解数据表、字段和注释。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/internal/schema/tables` | 查询数据库表结构，可通过 `tableNames` 指定表名 |

## 数据库表概览

`src/main/resources/sql/wanlv.sql` 中包含项目主要表结构，核心表包括：

| 模块 | 主要表 |
| --- | --- |
| 用户 | `sys_admin_user`、`sys_normal_user`、`sys_normal_user_refresh_token` |
| 地图 | `scenic_area`、`scenic_spot`、`scenic_geo_feature`、`tour_route`、`tour_route_spot`、`tour_route_geo`、`agent_route_geo`、`map_interaction_log` |
| 预约 | `spot_reservation_rule`、`spot_reservation_slot`、`spot_reservation_order`、`spot_reservation_visitor` |
| Agent 会话 | `visitor_session`、`visitor_message`、`knowledge_document`、`message_store` |
| 用户画像 | `user_digital_profile` |

## 文档目录

| 文档 | 内容 |
| --- | --- |
| `docs/README.md` | 接口文档总览、阅读顺序和当前接口路由总表 |
| `docs/jwt-auth-api.md` | JWT 登录鉴权与前端接入文档 |
| `docs/token-refresh-client-integration.md` | Web 与 Android Token 无感刷新快速接入说明 |
| `docs/frontend-api.md` | 前端基础登录接口文档 |
| `docs/phone-code-login-frontend-implementation.md` | 手机验证码登录前端实现参考 |
| `docs/user-management-api.md` | 用户管理接口文档 |
| `docs/real-name-reservation-frontend-api.md` | 实名认证与预约下单前端改造说明 |
| `docs/map-api.md` | 地图业务接口联调文档 |
| `docs/agent-route-api.md` | Agent 定制路线轨迹工具接口文档 |
| `docs/spot-reservation-api.md` | 景点预约接口联调文档 |
| `docs/agent-reservation-api.md` | Agent 景点预约工具接口文档 |
| `docs/agent-chat-api.md` | Agent 聊天接口文档 |
| `docs/session-daily-analysis-api.md` | 会话日报总结接口文档 |
| `docs/agent-user-profile-integration.md` | Agent 接入用户数字画像说明 |

## 推荐联调顺序

1. 导入 `src/main/resources/sql/wanlv.sql`，确认数据库连接可用。
2. 调用 `/user/init` 初始化超级管理员。
3. 调用 `/user/admin/login` 或 `/user/normal/code/login` 获取登录凭证；普通用户端同时保存 accessToken 和 refreshToken。
4. 普通用户端按 `docs/token-refresh-client-integration.md` 接入 401 单飞刷新和原请求重放。
5. 地图页面优先调用 `/map/init/{scenicAreaId}` 获取景区、空间要素、景点和路线聚合数据。
6. 预约页面先调用 `/reservation/spots/enabled` 和 `/reservation/slots`，再创建订单。
7. Agent 聊天、Agent 预约、Agent 路线和日报总结功能联调前，先确认独立 Agent 服务已启动。
8. 前端统一封装请求时，优先判断响应体 `code === 200`。

## 文档维护约定

- 新增或修改接口时，先更新对应 `docs/*.md` 业务文档，再同步更新根目录 README 的模块概览。
- 如果文档和 Controller 路径不一致，以 Controller 实现为准。
- 重点规则、鉴权限制、兼容策略请写中文说明，方便前端、后端和 Agent 联调时快速确认。
