# JWT 登录鉴权与 Token 刷新接口文档

## 1. 接入结论

后端使用 JWT accessToken 鉴权，并为普通用户提供 refreshToken 无感刷新能力。

- accessToken 默认有效期：`7200` 秒（2 小时）。
- refreshToken 默认有效期：`259200` 秒（3 天）。
- 普通用户每次登录都会生成独立的 refreshToken，因此 Web、Android 或多个浏览器可以同时登录。
- refreshToken 成功刷新后会立即轮换，旧 refreshToken 作废，新 refreshToken 重新获得 3 天有效期。
- 管理员登录暂不签发 refreshToken，仍按原 JWT 登录方式处理。

访问受保护接口时统一携带：

```http
Authorization: Bearer <ACCESS_TOKEN>
```

## 2. 普通用户登录

以下两个接口的请求参数保持不变：

```http
POST /user/normal/login
POST /user/normal/code/login
Content-Type: application/json
```

普通用户登录成功响应示例：

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "id": 1,
    "username": "user",
    "phone": "13800138000",
    "displayName": "用户",
    "userType": "normal",
    "role": "normal_user",
    "status": 1,
    "realNameStatus": 0,
    "token": "ACCESS_TOKEN",
    "refreshToken": "REFRESH_TOKEN",
    "expireSeconds": 7200,
    "refreshExpireSeconds": 259200,
    "lastLoginTime": "2026-06-20T16:30:00"
  }
}
```

普通用户登录返回对象：

```ts
export interface UserLoginVO {
  id: number
  username: string
  phone?: string | null
  displayName: string
  userType: 'normal'
  role: 'normal_user'
  status: number
  realNameStatus?: number | null
  token: string
  refreshToken: string
  expireSeconds: number
  refreshExpireSeconds: number
  lastLoginTime?: string | null
}
```

## 3. 刷新 Token

```http
POST /user/normal/token/refresh
Content-Type: application/json
```

该接口不要求携带 Authorization，因为调用时 accessToken 可能已经过期。

请求：

```json
{
  "refreshToken": "REFRESH_TOKEN"
}
```

成功响应：

```json
{
  "code": 200,
  "msg": "刷新成功",
  "data": {
    "token": "NEW_ACCESS_TOKEN",
    "refreshToken": "NEW_REFRESH_TOKEN",
    "expireSeconds": 7200,
    "refreshExpireSeconds": 259200
  }
}
```

刷新成功后，客户端必须同时覆盖本地的 accessToken 和 refreshToken。旧 refreshToken 已经作废，不能再次使用。

refreshToken 不存在、已过期、已作废，或所属账号已失效时，返回 HTTP `401`：

```json
{
  "code": 401,
  "msg": "登录状态已过期，请重新登录",
  "data": null
}
```

重点：多个业务请求同时收到 `401` 时，客户端只能发起一次刷新请求，其余请求等待该刷新结果后重放，避免同一个 refreshToken 被并发使用。

## 4. 退出登录

```http
POST /user/normal/logout
Content-Type: application/json
```

请求：

```json
{
  "refreshToken": "REFRESH_TOKEN"
}
```

成功响应：

```json
{
  "code": 200,
  "msg": "退出成功",
  "data": null
}
```

退出接口是幂等的：refreshToken 不存在或已经失效时也按退出成功处理。客户端应在接口完成后清理本地 Token。退出只作废当前请求携带的 refreshToken，不影响其他设备。

accessToken 是无状态 JWT，退出后可能在剩余有效期内继续通过签名校验；客户端必须立即删除它。后端以 refreshToken 作废来阻止该设备继续续期。

## 5. 401 处理规则

| 场景 | 客户端处理 |
| --- | --- |
| 普通用户业务接口返回 HTTP 401，且本地有 refreshToken | 单飞调用刷新接口，成功后更新两个 Token 并重放原请求 |
| 刷新接口返回 HTTP 401 | 清理本地登录态并跳转登录页 |
| 没有 refreshToken | 清理本地登录态并跳转登录页 |
| 同一请求刷新后再次返回 401 | 不再刷新，直接退出登录，防止死循环 |
| 管理员接口返回 HTTP 401 | 管理员没有 refreshToken，直接重新登录 |

不要把业务 `code = 500` 当作 Token 过期，也不要拦截刷新接口自身的 `401` 后再次刷新。

Web 与 Android 的完整接入代码见 [token-refresh-client-integration.md](./token-refresh-client-integration.md)。

## 6. 无需 accessToken 的接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/user/admin/login` | 管理员登录 |
| `POST` | `/user/normal/login` | 普通用户账号密码登录 |
| `POST` | `/user/normal/register` | 普通用户注册（当前 Controller 中已停用） |
| `POST` | `/user/normal/code/send` | 发送普通用户手机验证码 |
| `POST` | `/user/normal/code/login` | 普通用户手机验证码登录/自动注册 |
| `POST` | `/user/normal/token/refresh` | 普通用户刷新 Token |
| `POST` | `/user/normal/logout` | 普通用户退出登录 |
| `GET/POST` | `/user/init` | 初始化超级管理员 |
| `GET/POST` | `/reservation/agent/**` | Agent 服务调用的预约工具接口 |
| `GET` | `/reservation/spots/enabled` | 查询可预约景点 |
| `GET` | `/reservation/slots` | 查询指定景点某天可预约时段 |

`/internal/**` 使用 `X-Internal-Token`，不使用用户 JWT。

## 7. 需要 accessToken 的接口与权限

除白名单接口外，其余接口默认需要有效 accessToken。

普通用户本人接口会校验请求中的 `userId` 或 `id` 必须等于 Token 中的当前用户 ID。管理员接口根据 JWT 中的 `userType` 和 `role` 校验权限，超级管理员功能要求 `role = super_admin`。

重点：客户端不能通过修改 `userId` 查看或操作其他用户数据。

## 8. 后端存储与安全说明

数据库表为 `sys_normal_user_refresh_token`，每条记录保存：

- refreshToken 的 SHA-256 摘要，不保存可直接使用的明文 Token。
- 普通用户 ID。
- 过期时间。
- 失效状态。

刷新时后端通过条件更新原子作废旧 Token，因此同一个 refreshToken 即使被并发提交，也只允许一个请求刷新成功。

现有数据库需要执行一次增量脚本：

```text
src/main/resources/sql/normal_user_refresh_token_migration.sql
```

生产环境必须通过环境变量配置足够安全的 `WANLV_JWT_SECRET`，客户端和服务端日志中都不要输出 accessToken 或 refreshToken。
