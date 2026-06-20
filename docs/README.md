# wanlv-back 接口文档总览

本文档是 `docs` 目录的统一入口，用来快速判断“该看哪份文档、当前有哪些接口、哪些文档是实现参考”。详细请求体、响应示例和前端封装代码仍放在各业务文档中，避免一个文件过长。

## 1. 基础约定

- 本地默认地址：`http://127.0.0.1:8080`
- 请求体格式：`application/json`
- 响应体格式：统一使用 `Result<T>`

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

- 前端判断成功时优先看业务字段 `code === 200`。
- JWT 规则以 [jwt-auth-api.md](./jwt-auth-api.md) 为准。
- 当前后端 Controller 中的预约接口前缀是 `/reservation`，不是旧文档里曾出现过的 `/api/reservation`。
- 普通用户账号注册接口 `/user/normal/register` 在当前 `UserController` 中已停用，普通用户新账号以手机验证码登录自动注册为主。

## 2. 文档阅读顺序

| 场景 | 优先阅读 | 说明 |
| --- | --- | --- |
| Web/Android 登录与无感刷新 | [token-refresh-client-integration.md](./token-refresh-client-integration.md)、[jwt-auth-api.md](./jwt-auth-api.md) | Token 字段、401 单飞刷新、请求重放、退出登录 |
| 前端登录、注册、验证码登录 | [frontend-api.md](./frontend-api.md)、[phone-code-login-frontend-implementation.md](./phone-code-login-frontend-implementation.md) | 登录与验证码页面实现参考 |
| 用户管理后台 | [user-management-api.md](./user-management-api.md) | 管理员和普通用户的查询、更新、分页、删除 |
| 实名认证和预约下单改造 | [real-name-reservation-frontend-api.md](./real-name-reservation-frontend-api.md)、[spot-reservation-api.md](./spot-reservation-api.md) | 实名字段、预约游客信息、订单返回字段 |
| 景点预约用户端和管理端 | [spot-reservation-api.md](./spot-reservation-api.md) | 预约景点、时段、订单、规则、后台看板 |
| Agent 预约工具 | [agent-reservation-api.md](./agent-reservation-api.md) | Agent 服务直接调用的预约工具接口 |
| 地图、景区、景点、路线 | [map-api.md](./map-api.md) | 地图初始化、景区景点管理、路线和空间要素 |
| Agent 定制路线 | [agent-route-api.md](./agent-route-api.md) | Agent 生成专属路线轨迹、前端查询最新路线 |
| Agent 聊天主链路 | [agent-chat-api.md](./agent-chat-api.md) | 聊天、会话绑定景区、单用户会话分析 |
| 会话日报任务 | [session-daily-analysis-api.md](./session-daily-analysis-api.md) | 单用户日报、批量日报、定时任务 |
| Agent 用户画像接入 | [agent-user-profile-integration.md](./agent-user-profile-integration.md) | 后端传给 Agent 的用户画像字段和兼容策略 |

## 3. 接口模块总表

### 3.1 用户与登录

| 方法 | 路径 | 说明 | 文档 |
| --- | --- | --- | --- |
| `GET/POST` | `/user/init` | 初始化超级管理员 | [frontend-api.md](./frontend-api.md) |
| `POST` | `/user/admin/login` | 管理员登录 | [frontend-api.md](./frontend-api.md) |
| `POST` | `/user/admin/add` | 新增管理员 | [frontend-api.md](./frontend-api.md)、[jwt-auth-api.md](./jwt-auth-api.md) |
| `POST` | `/user/normal/login` | 普通用户账号密码登录 | [frontend-api.md](./frontend-api.md) |
| `POST` | `/user/normal/code/send` | 发送手机验证码 | [frontend-api.md](./frontend-api.md) |
| `POST` | `/user/normal/code/login` | 手机验证码登录/自动注册 | [frontend-api.md](./frontend-api.md) |
| `POST` | `/user/normal/token/refresh` | 刷新并轮换普通用户 Token | [jwt-auth-api.md](./jwt-auth-api.md)、[token-refresh-client-integration.md](./token-refresh-client-integration.md) |
| `POST` | `/user/normal/logout` | 作废当前设备 refreshToken | [jwt-auth-api.md](./jwt-auth-api.md)、[token-refresh-client-integration.md](./token-refresh-client-integration.md) |
| `POST` | `/user/normal/real-name/verify` | 普通用户实名认证 | [real-name-reservation-frontend-api.md](./real-name-reservation-frontend-api.md) |
| `PUT` | `/user/admin/update` | 更新管理员信息 | [user-management-api.md](./user-management-api.md) |
| `PUT` | `/user/normal/update` | 更新普通用户信息 | [user-management-api.md](./user-management-api.md) |
| `GET` | `/user/admin/{id}` | 管理员详情 | [user-management-api.md](./user-management-api.md) |
| `GET` | `/user/admin/digital-profile` | 超级管理员分页查询用户画像 | [user-management-api.md](./user-management-api.md)、[jwt-auth-api.md](./jwt-auth-api.md) |
| `GET` | `/user/admin/digital-profile/{userId}` | 超级管理员查询用户画像 | [user-management-api.md](./user-management-api.md)、[jwt-auth-api.md](./jwt-auth-api.md) |
| `DELETE` | `/user/admin/{id}` | 删除管理员 | [user-management-api.md](./user-management-api.md) |
| `GET` | `/user/normal/{id}` | 普通用户详情 | [user-management-api.md](./user-management-api.md) |
| `DELETE` | `/user/normal/{id}` | 删除普通用户 | [user-management-api.md](./user-management-api.md) |
| `GET` | `/user/admin/page` | 管理员分页 | [user-management-api.md](./user-management-api.md) |
| `GET` | `/user/normal/page` | 普通用户分页 | [user-management-api.md](./user-management-api.md) |

### 3.2 预约

| 方法 | 路径 | 说明 | 文档 |
| --- | --- | --- | --- |
| `GET` | `/reservation/spots/enabled` | 查询支持预约的景点 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `GET` | `/reservation/slots` | 查询景点可预约时段 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `POST` | `/reservation/orders` | 创建预约订单 | [spot-reservation-api.md](./spot-reservation-api.md)、[real-name-reservation-frontend-api.md](./real-name-reservation-frontend-api.md) |
| `GET` | `/reservation/orders/my` | 查询我的预约 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `POST` | `/reservation/orders/{reservationNo}/cancel` | 取消我的预约 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `POST` | `/reservation/admin/rules` | 创建预约规则 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `PUT` | `/reservation/admin/rules/{id}` | 修改预约规则 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `PUT` | `/reservation/admin/rules/{id}/status` | 启用或停用预约规则 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `GET` | `/reservation/admin/rules` | 查询预约规则列表 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `POST` | `/reservation/admin/slots/generate` | 手动生成预约时段 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `POST` | `/reservation/admin/slots` | 新增临时预约时段 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `PUT` | `/reservation/admin/slots/{id}` | 修改预约时段 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `GET` | `/reservation/admin/slots` | 查询预约时段列表 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `GET` | `/reservation/admin/orders` | 查询预约订单列表 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `POST` | `/reservation/admin/orders/{reservationNo}/enter` | 检票入场 | [spot-reservation-api.md](./spot-reservation-api.md) |
| `GET` | `/reservation/admin/dashboard` | 预约运营看板 | [spot-reservation-api.md](./spot-reservation-api.md) |

### 3.3 Agent 预约工具

| 方法 | 路径 | 说明 | 文档 |
| --- | --- | --- | --- |
| `GET` | `/reservation/agent/spots/enabled` | Agent 查询可预约景点 | [agent-reservation-api.md](./agent-reservation-api.md) |
| `GET` | `/reservation/agent/spots/search` | Agent 按名称搜索景点 | [agent-reservation-api.md](./agent-reservation-api.md) |
| `GET` | `/reservation/agent/slots/match` | Agent 匹配指定时间附近时段 | [agent-reservation-api.md](./agent-reservation-api.md) |
| `POST` | `/reservation/agent/orders` | Agent 创建预约订单 | [agent-reservation-api.md](./agent-reservation-api.md) |
| `GET` | `/reservation/agent/orders/recent` | Agent 查询用户近期预约 | [agent-reservation-api.md](./agent-reservation-api.md) |
| `GET` | `/reservation/agent/slots/recommend` | Agent 推荐可预约时段 | [agent-reservation-api.md](./agent-reservation-api.md) |
| `POST` | `/reservation/agent/orders/{reservationNo}/cancel` | Agent 取消预约订单 | [agent-reservation-api.md](./agent-reservation-api.md) |

### 3.4 地图与路线

| 方法 | 路径 | 说明 | 文档 |
| --- | --- | --- | --- |
| `POST` | `/map/scenic-areas` | 新增景区 | [map-api.md](./map-api.md) |
| `PUT` | `/map/scenic-areas` | 更新景区 | [map-api.md](./map-api.md) |
| `GET` | `/map/scenic-areas/page` | 景区分页 | [map-api.md](./map-api.md) |
| `GET` | `/map/scenic-areas/{id}` | 景区详情 | [map-api.md](./map-api.md) |
| `DELETE` | `/map/scenic-areas/{id}` | 删除景区 | [map-api.md](./map-api.md) |
| `POST` | `/map/spots` | 新增景点 | [map-api.md](./map-api.md) |
| `PUT` | `/map/spots` | 更新景点 | [map-api.md](./map-api.md) |
| `GET` | `/map/spots/page` | 景点分页 | [map-api.md](./map-api.md) |
| `GET` | `/map/spots/{id}` | 景点详情 | [map-api.md](./map-api.md) |
| `DELETE` | `/map/spots/{id}` | 删除景点 | [map-api.md](./map-api.md) |
| `POST` | `/map/routes` | 新增路线 | [map-api.md](./map-api.md) |
| `PUT` | `/map/routes` | 更新路线 | [map-api.md](./map-api.md) |
| `GET` | `/map/routes/page` | 路线分页 | [map-api.md](./map-api.md) |
| `GET` | `/map/routes/{id}` | 路线详情 | [map-api.md](./map-api.md) |
| `DELETE` | `/map/routes/{id}` | 删除路线 | [map-api.md](./map-api.md) |
| `POST` | `/map/route-geos` | 新增路线几何 | [map-api.md](./map-api.md) |
| `POST` | `/map/routes/{routeId}/geo/generate` | 根据路线景点生成轨迹 | [map-api.md](./map-api.md) |
| `PUT` | `/map/route-geos` | 更新路线几何 | [map-api.md](./map-api.md) |
| `GET` | `/map/route-geos/route/{routeId}` | 路线几何列表 | [map-api.md](./map-api.md) |
| `POST` | `/map/geo-features` | 新增空间要素 | [map-api.md](./map-api.md) |
| `PUT` | `/map/geo-features` | 更新空间要素 | [map-api.md](./map-api.md) |
| `GET` | `/map/geo-features` | 空间要素列表 | [map-api.md](./map-api.md) |
| `DELETE` | `/map/geo-features/{id}` | 删除空间要素 | [map-api.md](./map-api.md) |
| `GET` | `/map/init/{scenicAreaId}` | 地图初始化 | [map-api.md](./map-api.md) |
| `POST` | `/map/interaction-logs` | 交互日志记录 | [map-api.md](./map-api.md) |
| `POST` | `/map/agent/routes/geo/generate` | Agent 生成定制路线轨迹 | [agent-route-api.md](./agent-route-api.md) |
| `GET` | `/map/agent-route-geos/latest` | 查询用户最新专属路线 | [agent-route-api.md](./agent-route-api.md) |

### 3.5 Agent 聊天与会话分析

| 方法 | 路径 | 说明 | 文档 |
| --- | --- | --- | --- |
| `POST` | `/agent/chat` | Agent 聊天提问 | [agent-chat-api.md](./agent-chat-api.md) |
| `POST` | `/agent/session-analysis` | 单用户会话日报总结 | [agent-chat-api.md](./agent-chat-api.md)、[session-daily-analysis-api.md](./session-daily-analysis-api.md) |
| `POST` | `/agent/session-analysis/daily` | 按日期批量日报总结 | [session-daily-analysis-api.md](./session-daily-analysis-api.md) |
| `POST` | `/agent/session/scenic-area/bind` | 绑定当天会话景区 | [agent-chat-api.md](./agent-chat-api.md) |
| `GET` | `/agent/digital-profile/{userId}` | 查询用户数字画像 | [agent-user-profile-integration.md](./agent-user-profile-integration.md) |

### 3.6 内部接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/internal/schema/tables` | 内部表结构查询，走 `X-Internal-Token` 鉴权 |

## 4. 文档维护规则

- 新增接口时，先更新对应业务文档，再更新本文档的模块总表。
- 如果 Controller 实际路径和旧文档不一致，以 `src/main/java/com/example/wanlvback/controller` 中的路由为准。
- 如果线上网关额外加 `/api` 前缀，请在部署说明或网关文档里说明，不要直接改后端接口文档里的 Controller 路径。
- 重点规则、鉴权限制、兼容策略请写中文说明，方便前后端和 Agent 联调时快速确认。
