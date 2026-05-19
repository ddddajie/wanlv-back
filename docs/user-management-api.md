# 用户管理接口文档

本文档用于说明本次新增的用户管理接口能力，包含以下 6 个接口：

1. 管理员信息更新
2. 普通用户信息更新
3. 管理员详情查询
4. 普通用户详情查询
5. 管理员分页列表查询
6. 普通用户分页列表查询

适用项目：`wanlv-back`  
默认服务地址：`http://127.0.0.1:8080`  
统一接口前缀：`/user`

---

## 1. 基础说明

- 请求格式：`application/json`
- 当前接口未接入统一 token 鉴权
- 接口统一返回 `Result<T>`
- 分页接口返回 `Result<PageResult>`
- 查询和分页返回中都不会返回密码字段

统一响应结构：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

字段说明：

- `code`：业务状态码，`200` 表示成功，`500` 表示失败
- `msg`：响应消息
- `data`：响应数据

---

## 2. 数据模型

### 2.1 管理员详情对象 `AdminUserVO`

```json
{
  "id": 1,
  "username": "admin",
  "realName": "系统超级管理员",
  "phone": "13800000000",
  "email": "admin@wanlv.com",
  "avatarUrl": "https://example.com/admin.png",
  "role": "super_admin",
  "scenicSpot": "默认景区",
  "status": 1,
  "lastLoginTime": "2026-04-18T10:00:00",
  "remark": "由初始化接口自动生成",
  "createTime": "2026-04-18T09:00:00",
  "updateTime": "2026-04-18T10:00:00"
}
```

### 2.2 普通用户详情对象 `NormalUserVO`

```json
{
  "id": 10,
  "username": "user01",
  "nickname": "小王",
  "phone": "13900139000",
  "email": "user01@test.com",
  "avatarUrl": "https://example.com/user01.png",
  "gender": 1,
  "age": 24,
  "interestTags": "[\"摄影\",\"徒步\"]",
  "status": 1,
  "lastLoginTime": "2026-04-18T10:05:00",
  "createTime": "2026-04-18T09:20:00",
  "updateTime": "2026-04-18T10:05:00"
}
```

### 2.3 分页对象 `PageResult`

```json
{
  "total": 23,
  "records": []
}
```

字段说明：

- `total`：总记录数
- `records`：当前页数据列表

---

## 3. 动态 SQL 更新规则

管理员和普通用户更新接口都使用 MyBatis 动态 SQL，更新规则如下：

1. 只有请求中传了值的字段才会进入 `update set`
2. 字符串字段要求“非 `null` 且非空字符串”才会更新
3. 数值字段如 `status`、`gender`、`age` 只要不是 `null` 就会更新
4. `password` 如果传入，会先由后端加密后再保存
5. 更新时会自动刷新 `update_time = now()`
6. 只允许更新 `deleted = 0` 的用户

注意事项：

- 如果请求里除了 `id` 外没有任何可更新字段，后端会返回“更新内容不能为空”
- 当前动态 SQL 不支持把字符串字段更新为空字符串
- 如果修改后的 `username` 与其他未删除用户重复，后端会返回账号已存在

---

## 4. 管理员信息更新接口

### 4.1 接口地址

`PUT /user/admin/update`

完整示例：

`http://127.0.0.1:8080/user/admin/update`

### 4.2 请求参数

```json
{
  "id": 2,
  "username": "manager01",
  "password": "123456",
  "realName": "张三",
  "phone": "13800138000",
  "email": "manager01@wanlv.com",
  "avatarUrl": "https://example.com/avatar.png",
  "role": "admin",
  "scenicSpot": "西湖景区",
  "status": 1,
  "remark": "负责票务管理"
}
```

字段说明：

- `id`：管理员 ID，必填
- `username`：管理员账号，选填
- `password`：管理员密码，选填，后端会自动加密
- `realName`：真实姓名，选填
- `phone`：手机号，选填
- `email`：邮箱，选填
- `avatarUrl`：头像地址，选填
- `role`：角色，选填
- `scenicSpot`：所属景区，选填
- `status`：状态，选填，`1` 启用，`0` 禁用
- `remark`：备注，选填

### 4.3 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 2,
    "username": "manager01",
    "realName": "张三",
    "phone": "13800138000",
    "email": "manager01@wanlv.com",
    "avatarUrl": "https://example.com/avatar.png",
    "role": "admin",
    "scenicSpot": "西湖景区",
    "status": 1,
    "lastLoginTime": null,
    "remark": "负责票务管理",
    "createTime": "2026-04-18T09:00:00",
    "updateTime": "2026-04-18T10:30:00"
  }
}
```

### 4.4 常见失败场景

- `管理员ID不能为空`
- `管理员更新内容不能为空`
- `管理员用户不存在`
- `管理员账号已存在`

---

## 5. 普通用户信息更新接口

### 5.1 接口地址

`PUT /user/normal/update`

完整示例：

`http://127.0.0.1:8080/user/normal/update`

### 5.2 请求参数

```json
{
  "id": 10,
  "username": "user01",
  "password": "123456",
  "nickname": "小王",
  "phone": "13900139000",
  "email": "user01@test.com",
  "avatarUrl": "https://example.com/user01.png",
  "gender": 1,
  "age": 24,
  "interestTags": "[\"摄影\",\"徒步\"]",
  "status": 1
}
```

字段说明：

- `id`：普通用户 ID，必填
- `username`：用户账号，选填
- `password`：用户密码，选填，后端会自动加密
- `nickname`：昵称，选填
- `phone`：手机号，选填
- `email`：邮箱，选填
- `avatarUrl`：头像地址，选填
- `gender`：性别，选填
- `age`：年龄，选填
- `interestTags`：兴趣标签 JSON 字符串，选填
- `status`：状态，选填，`1` 启用，`0` 禁用

### 5.3 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 10,
    "username": "user01",
    "nickname": "小王",
    "phone": "13900139000",
    "email": "user01@test.com",
    "avatarUrl": "https://example.com/user01.png",
    "gender": 1,
    "age": 24,
    "interestTags": "[\"摄影\",\"徒步\"]",
    "status": 1,
    "lastLoginTime": "2026-04-18T10:05:00",
    "createTime": "2026-04-18T09:20:00",
    "updateTime": "2026-04-18T10:35:00"
  }
}
```

### 5.4 常见失败场景

- `普通用户ID不能为空`
- `普通用户更新内容不能为空`
- `普通用户不存在`
- `普通用户账号已存在`

---

## 6. 管理员详情查询接口

### 6.1 接口地址

`GET /user/admin/{id}`

完整示例：

`http://127.0.0.1:8080/user/admin/2`

### 6.2 路径参数

- `id`：管理员 ID，必填

### 6.3 成功响应

响应 `data` 对应 `AdminUserVO`，结构与“管理员信息更新接口”的返回一致。

### 6.4 常见失败场景

- `管理员ID不能为空`
- `管理员用户不存在`

---

## 7. 普通用户详情查询接口

### 7.1 接口地址

`GET /user/normal/{id}`

完整示例：

`http://127.0.0.1:8080/user/normal/10`

### 7.2 路径参数

- `id`：普通用户 ID，必填

### 7.3 成功响应

响应 `data` 对应 `NormalUserVO`，结构与“普通用户信息更新接口”的返回一致。

### 7.4 常见失败场景

- `普通用户ID不能为空`
- `普通用户不存在`

---

## 8. 管理员分页列表接口

### 8.1 接口地址

`GET /user/admin/page?pageNum=1&pageSize=10`

### 8.2 请求参数

| 参数名 | 是否必填 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `pageNum` | 否 | `number` | `1` | 页码 |
| `pageSize` | 否 | `number` | `10` | 每页条数 |

补充说明：

- 当 `pageNum < 1` 时，后端会按 `1` 处理
- 当 `pageSize < 1` 时，后端会按 `10` 处理

### 8.3 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 2,
    "records": [
      {
        "id": 2,
        "username": "manager01",
        "realName": "张三",
        "phone": "13800138000",
        "email": "manager01@wanlv.com",
        "avatarUrl": "https://example.com/avatar.png",
        "role": "admin",
        "scenicSpot": "西湖景区",
        "status": 1,
        "lastLoginTime": null,
        "remark": "负责票务管理",
        "createTime": "2026-04-18T09:00:00",
        "updateTime": "2026-04-18T10:30:00"
      },
      {
        "id": 1,
        "username": "admin",
        "realName": "系统超级管理员",
        "phone": "13800000000",
        "email": "admin@wanlv.com",
        "avatarUrl": "https://example.com/admin.png",
        "role": "super_admin",
        "scenicSpot": "默认景区",
        "status": 1,
        "lastLoginTime": "2026-04-18T10:00:00",
        "remark": "由初始化接口自动生成",
        "createTime": "2026-04-18T09:00:00",
        "updateTime": "2026-04-18T10:00:00"
      }
    ]
  }
}
```

### 8.4 查询规则

- 只查询 `deleted = 0` 的管理员
- 排序规则：`create_time desc, id desc`

---

## 9. 普通用户分页列表接口

### 9.1 接口地址

`GET /user/normal/page?pageNum=1&pageSize=10`

### 9.2 请求参数

| 参数名 | 是否必填 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `pageNum` | 否 | `number` | `1` | 页码 |
| `pageSize` | 否 | `number` | `10` | 每页条数 |

补充说明：

- 当 `pageNum < 1` 时，后端会按 `1` 处理
- 当 `pageSize < 1` 时，后端会按 `10` 处理

### 9.3 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 2,
    "records": [
      {
        "id": 11,
        "username": "user02",
        "nickname": "小李",
        "phone": "13900139001",
        "email": "user02@test.com",
        "avatarUrl": "https://example.com/user02.png",
        "gender": 2,
        "age": 22,
        "interestTags": "[\"美食\",\"拍照\"]",
        "status": 1,
        "lastLoginTime": "2026-04-18T10:20:00",
        "createTime": "2026-04-18T09:30:00",
        "updateTime": "2026-04-18T10:20:00"
      },
      {
        "id": 10,
        "username": "user01",
        "nickname": "小王",
        "phone": "13900139000",
        "email": "user01@test.com",
        "avatarUrl": "https://example.com/user01.png",
        "gender": 1,
        "age": 24,
        "interestTags": "[\"摄影\",\"徒步\"]",
        "status": 1,
        "lastLoginTime": "2026-04-18T10:05:00",
        "createTime": "2026-04-18T09:20:00",
        "updateTime": "2026-04-18T10:35:00"
      }
    ]
  }
}
```

### 9.4 查询规则

- 只查询 `deleted = 0` 的普通用户
- 排序规则：`create_time desc, id desc`

---

## 10. 用户画像查询接口

### 10.1 查询全部用户画像接口

`GET /user/admin/digital-profile?pageNum=1&pageSize=10`

完整示例：

`http://127.0.0.1:8080/user/admin/digital-profile?pageNum=1&pageSize=10`

该接口用于超级管理员后台分页查询当前已有的用户数字画像，后端按 `update_time desc, id desc` 排序。

请求参数：

| 参数名 | 是否必填 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `pageNum` | 否 | `number` | `1` | 页码 |
| `pageSize` | 否 | `number` | `10` | 每页条数 |

成功响应：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 1,
    "records": [
      {
        "id": 1,
        "userId": 10,
        "profileName": "摄影徒步偏好用户",
        "interestTags": ["摄影", "徒步", "自然风光"],
        "focusTopics": ["路线规划", "景点推荐"],
        "serviceNeeds": ["个性化路线", "预约提醒"],
        "knowledgeGaps": ["交通换乘", "开放时间"],
        "travelStyle": "深度游",
        "activityLevel": "高",
        "sentimentTendency": "positive",
        "sentimentScoreAvg": 0.82,
        "profileScore": 86,
        "sourceSessionCount": 5,
        "lastAnalyzedDate": "2026-05-19",
        "profileJson": "{\"summary\":\"偏好自然风光和轻户外体验\"}",
        "createTime": "2026-05-19T09:00:00",
        "updateTime": "2026-05-19T10:30:00"
      }
    ]
  }
}
```

如果当前还没有任何用户画像，`data.total` 为 `0`，`data.records` 返回空数组 `[]`。

### 10.2 按用户 ID 查询画像接口

`GET /user/admin/digital-profile/{userId}`

完整示例：

`http://127.0.0.1:8080/user/admin/digital-profile/10`

### 10.3 权限要求

- 必须携带管理员登录后返回的 JWT：

```http
Authorization: Bearer <token>
```

- 仅允许超级管理员调用，即 token 中的 `userType = admin` 且 `role = super_admin`。
- 普通管理员或普通用户调用会返回无权限错误。

### 10.4 路径参数

| 参数名 | 是否必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| `userId` | 是 | `number` | 要查询用户画像的普通用户 ID |

### 10.5 成功响应

响应 `data` 对应 `UserDigitalProfileVO`：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "userId": 10,
    "profileName": "摄影徒步偏好用户",
    "interestTags": ["摄影", "徒步", "自然风光"],
    "focusTopics": ["路线规划", "景点推荐"],
    "serviceNeeds": ["个性化路线", "预约提醒"],
    "knowledgeGaps": ["交通换乘", "开放时间"],
    "travelStyle": "深度游",
    "activityLevel": "高",
    "sentimentTendency": "positive",
    "sentimentScoreAvg": 0.82,
    "profileScore": 86,
    "sourceSessionCount": 5,
    "lastAnalyzedDate": "2026-05-19",
    "profileJson": "{\"summary\":\"偏好自然风光和轻户外体验\"}",
    "createTime": "2026-05-19T09:00:00",
    "updateTime": "2026-05-19T10:30:00"
  }
}
```

### 10.6 字段说明

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `number` | 用户画像记录 ID |
| `userId` | `number` | 画像所属普通用户 ID |
| `profileName` | `string` | 用户画像名称 |
| `interestTags` | `string[]` | 兴趣标签 |
| `focusTopics` | `string[]` | 关注主题 |
| `serviceNeeds` | `string[]` | 服务需求 |
| `knowledgeGaps` | `string[]` | 信息缺口 |
| `travelStyle` | `string` | 旅行风格 |
| `activityLevel` | `string` | 活跃程度 |
| `sentimentTendency` | `string` | 情绪倾向 |
| `sentimentScoreAvg` | `number` | 平均情绪分 |
| `profileScore` | `number` | 画像完整度或评分 |
| `sourceSessionCount` | `number` | 画像来源会话数量 |
| `lastAnalyzedDate` | `string` | 最近分析日期 |
| `profileJson` | `string` | 原始画像 JSON 字符串 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

### 10.7 常见失败场景

- 未携带 token：返回 `401`，提示需要先登录。
- 普通管理员或普通用户调用：返回无权限错误。
- `userId` 对应画像不存在：`data` 可能为 `null`，前端展示时需要做好空状态处理。

---

## 11. 前端接入建议

推荐的接口封装示例：

```ts
import request from '@/utils/request'

export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export const updateAdminUserApi = (data: {
  id: number
  username?: string
  password?: string
  realName?: string
  phone?: string
  email?: string
  avatarUrl?: string
  role?: string
  scenicSpot?: string
  status?: number
  remark?: string
}) =>
  request.put<any, ApiResponse<any>>('/user/admin/update', data)

export const updateNormalUserApi = (data: {
  id: number
  username?: string
  password?: string
  nickname?: string
  phone?: string
  email?: string
  avatarUrl?: string
  gender?: number
  age?: number
  interestTags?: string
  status?: number
}) =>
  request.put<any, ApiResponse<any>>('/user/normal/update', data)

export const getAdminUserApi = (id: number) =>
  request.get<any, ApiResponse<any>>(`/user/admin/${id}`)

export const getNormalUserApi = (id: number) =>
  request.get<any, ApiResponse<any>>(`/user/normal/${id}`)

export const pageAdminUsersApi = (params: {
  pageNum?: number
  pageSize?: number
}) =>
  request.get<any, ApiResponse<PageResult<any>>>('/user/admin/page', { params })

export const pageNormalUsersApi = (params: {
  pageNum?: number
  pageSize?: number
}) =>
  request.get<any, ApiResponse<PageResult<any>>>('/user/normal/page', { params })

export const pageUserDigitalProfilesApi = (params: {
  pageNum?: number
  pageSize?: number
}) =>
  request.get<any, ApiResponse<PageResult<any>>>('/user/admin/digital-profile', { params })

export const getUserDigitalProfileApi = (userId: number) =>
  request.get<any, ApiResponse<any>>(`/user/admin/digital-profile/${userId}`)
```

---

## 12. 本次涉及文件

后端代码文件：

1. `src/main/java/com/example/wanlvback/controller/UserController.java`
2. `src/main/java/com/example/wanlvback/service/UserService.java`
3. `src/main/java/com/example/wanlvback/service/impl/UserServiceImpl.java`
4. `src/main/java/com/example/wanlvback/mapper/SysAdminUserMapper.java`
5. `src/main/java/com/example/wanlvback/mapper/SysNormalUserMapper.java`
6. `src/main/resources/mapper/SysAdminUserMapper.xml`
7. `src/main/resources/mapper/SysNormalUserMapper.xml`
8. `src/main/java/com/example/wanlvback/pojo/dto/AdminUserUpdateDTO.java`
9. `src/main/java/com/example/wanlvback/pojo/dto/NormalUserUpdateDTO.java`
10. `src/main/java/com/example/wanlvback/pojo/vo/AdminUserVO.java`
11. `src/main/java/com/example/wanlvback/pojo/vo/NormalUserVO.java`
12. `src/main/java/com/example/wanlvback/pojo/vo/UserDigitalProfileVO.java`
13. `src/main/java/com/example/wanlvback/service/UserDigitalProfileService.java`

文档文件：

1. `docs/user-management-api.md`
