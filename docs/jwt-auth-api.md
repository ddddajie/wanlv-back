# JWT 登录鉴权接口文档

## 1. 接入结论

当前后端已经接入无状态 JWT 鉴权。

前端登录或注册成功后，需要从登录返回对象中读取 `token`，并在后续需要登录的接口里统一携带：

```http
Authorization: Bearer <token>
```

未携带 token、token 过期、token 签名无效时，后端会返回 HTTP `401`，响应体仍为项目统一结构。

```json
{
  "code": 401,
  "msg": "请先登录",
  "data": null
}
```

## 2. 登录返回对象

登录、普通用户注册接口的 `data` 均为 `UserLoginVO`。

```ts
export interface UserLoginVO {
  id: number
  username: string
  displayName: string
  userType: 'admin' | 'normal'
  role: 'super_admin' | 'admin' | 'normal_user'
  status: number
  realNameStatus?: number | null
  token: string
  lastLoginTime?: string | null
}
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| `id` | 当前登录用户 ID |
| `username` | 登录账号 |
| `displayName` | 展示名称 |
| `userType` | 用户类型，管理员为 `admin`，普通用户为 `normal` |
| `role` | 角色，超级管理员为 `super_admin`，普通管理员为 `admin`，普通用户为 `normal_user` |
| `status` | 用户状态 |
| `realNameStatus` | 普通用户实名状态，管理员为空 |
| `token` | JWT 登录凭证 |
| `lastLoginTime` | 最后登录时间 |

## 3. 无需 token 的接口

以下接口不需要携带 `Authorization`：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/user/admin/login` | 管理员登录 |
| `POST` | `/user/normal/login` | 普通用户登录 |
| `POST` | `/user/normal/register` | 普通用户注册 |
| `POST` | `/user/normal/code/send` | 发送普通用户手机验证码 |
| `POST` | `/user/normal/code/login` | 普通用户手机验证码登录/自动注册 |
| `GET/POST` | `/user/init` | 初始化超级管理员 |
| `GET/POST` | `/reservation/agent/**` | Agent 服务调用的预约工具接口 |
| `GET` | `/reservation/spots/enabled` | 查询可预约景点 |
| `GET` | `/reservation/slots` | 查询指定景点某天可预约时段 |

`/internal/**` 仍然走原来的内部接口鉴权，使用 `X-Internal-Token`，不使用用户 JWT。

`/reservation/agent/**` 是后端 Agent 服务调用的工具接口，当前不要求用户 JWT。前端用户必须先登录才能使用智能问答主入口 `/agent/chat`，后端会在主链路中把用户上下文传给 Agent。

## 4. 需要 token 的接口

除第 3 节列出的接口外，其余接口默认都需要携带 JWT。

前端请求封装建议：

```ts
request.interceptors.request.use((config) => {
  const token = userStore.token || localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

收到 `401` 时建议清理本地登录态，并跳转登录页。

## 5. 权限规则

### 5.1 普通用户本人接口

普通用户接口会校验请求中的 `userId` 必须等于 token 中的当前用户 ID。

涉及接口包括但不限于：

| 方法 | 路径 | 校验 |
| --- | --- | --- |
| `POST` | `/user/normal/real-name/verify` | 请求体 `userId` 必须是当前用户 |
| `PUT` | `/user/normal/update` | 请求体 `id` 必须是当前用户，管理员可操作 |
| `GET` | `/user/normal/{id}` | 路径 `id` 必须是当前用户，管理员可查看 |
| `POST` | `/agent/chat` | 请求体 `userId` 必须是当前用户 |
| `POST` | `/agent/session/scenic-area/bind` | 请求体 `userId` 必须是当前用户 |
| `GET` | `/agent/digital-profile/{userId}` | 路径 `userId` 必须是当前用户，管理员可查看 |
| `POST` | `/reservation/orders` | 请求体 `userId` 必须是当前用户 |
| `GET` | `/reservation/orders/my` | 查询参数 `userId` 必须是当前用户 |
| `POST` | `/reservation/orders/{reservationNo}/cancel` | 请求体 `userId` 必须是当前用户 |

重点：前端不能通过改 `userId` 查看或操作其他用户数据。

### 5.2 管理员接口

以下路径需要管理员 token：

| 路径规则 | 说明 |
| --- | --- |
| `/user/admin/**` | 管理员账号管理，登录接口除外 |
| `/user/normal/page` | 普通用户分页查询 |
| `/user/normal/{id}` 删除 | 删除普通用户 |
| `/reservation/admin/**` | 预约管理后台接口 |
| `/map/**` | 地图、景区、路线后台维护接口 |

### 5.3 超级管理员接口

以下接口需要 `role = super_admin`：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/user/admin/add` | 新增管理员 |
| `DELETE` | `/user/admin/{id}` | 删除管理员 |
| `POST` | `/agent/session-analysis` | 单用户日报总结 |
| `POST` | `/agent/session-analysis/daily` | 批量日报总结 |

## 6. 接口变化说明

### 6.1 新增管理员

路径：

```http
POST /user/admin/add
```

现在权限以 JWT 中的 `role=super_admin` 为准。

请求体中的 `operatorUsername`、`operatorPassword` 已不再作为权限依据，前端可以不再收集和传递这两个字段。

推荐请求体：

```json
{
  "username": "admin2",
  "password": "123456",
  "realName": "景区管理员",
  "phone": "13800000001",
  "email": "admin2@wanlv.com",
  "scenicSpot": "默认景区",
  "remark": "后台创建"
}
```

### 6.2 日报总结接口

路径：

```http
POST /agent/session-analysis
POST /agent/session-analysis/daily
```

现在权限以 JWT 中的 `role=super_admin` 为准。

`operatorUsername`、`operatorPassword` 已不再需要传递。

单用户日报请求示例：

```json
{
  "userId": 1,
  "reportDate": "2026-05-08",
  "forceReanalyze": false
}
```

批量日报请求示例：

```json
{
  "reportDate": "2026-05-08",
  "forceReanalyze": false
}
```

## 7. 前端存储建议

登录成功后建议缓存：

```ts
{
  id,
  username,
  displayName,
  userType,
  role,
  realNameStatus,
  token
}
```

路由守卫建议：

| 页面类型 | 判断 |
| --- | --- |
| 普通用户页面 | 有 `token` 且 `userType === 'normal'` |
| 管理后台页面 | 有 `token` 且 `userType === 'admin'` |
| 超级管理员功能 | 有 `token` 且 `role === 'super_admin'` |

## 8. 调试示例

管理员登录：

```http
POST /user/admin/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

携带 token 查询管理员分页：

```http
GET /user/admin/page?pageNum=1&pageSize=10
Authorization: Bearer <token>
```

普通用户登录后查询自己的预约：

```http
GET /reservation/orders/my?pageNum=1&pageSize=10&userId=1
Authorization: Bearer <token>
```
