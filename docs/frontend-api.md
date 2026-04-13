# wanlv-back 前端接口文档

本文档基于当前后端代码整理，适用于 Vue 3 + Element Plus 前端项目接入。

## 1. 基础信息

- 项目名称：`wanlv-back`
- 本地默认端口：`8080`
- 默认 Base URL：`http://127.0.0.1:8080`
- 当前接口统一前缀：`/user`
- 请求体格式：`application/json`
- 当前阶段没有实际启用的 token 鉴权和登录拦截器
- 当前阶段前端不要依赖 `Authorization` 请求头
- 当前阶段接口成功或失败，前端都要优先判断响应体中的 `code`

推荐前端 axios 配置：

```ts
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://127.0.0.1:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    ElMessage.error(error?.message || '网络异常')
    return Promise.reject(error)
  },
)

export default request
```

## 2. 统一返回结构

所有接口都返回统一结构：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

字段说明：

- `code`: 业务状态码，`200` 表示成功，`500` 表示业务失败
- `msg`: 响应消息，成功通常为 `success`，失败时为具体错误原因
- `data`: 响应数据，成功时返回具体对象或字符串，失败时通常为 `null`

重要说明：

- 当前全局异常处理器返回的是 `Result.error(...)`
- 这意味着很多业务失败场景，HTTP 状态码仍可能是 `200`
- 前端必须用 `res.code === 200` 作为成功判断条件

## 3. 通用错误提示

后端当前可能返回的常见失败消息：

- `管理员账号或密码不能为空`
- `管理员账号不存在`
- `管理员账号已被禁用`
- `管理员账号或密码错误`
- `操作人账号或密码不能为空`
- `操作人不存在`
- `操作人已被禁用`
- `操作人账号或密码错误`
- `只有超级管理员才能新增管理员`
- `管理员账号已存在`
- `普通用户账号或密码不能为空`
- `普通用户账号不存在`
- `普通用户账号已被禁用`
- `普通用户账号或密码错误`
- `普通用户账号已存在`
- `请求参数格式错误`
- `数据已存在`
- `系统繁忙，请稍后重试`

推荐前端处理方式：

- 直接使用 `msg` 作为 Element Plus 的 `ElMessage.error(msg)`
- 表单校验优先在前端做一次，减少无效请求

## 4. 数据模型

### 4.1 登录返回对象 `UserLoginVO`

```ts
export interface UserLoginVO {
  id: number
  username: string
  displayName: string
  userType: 'admin' | 'normal'
  role: string
  status: number
  lastLoginTime: string | null
}
```

字段说明：

- `id`: 用户 ID
- `username`: 账号
- `displayName`: 展示名称
- `userType`: 用户类型，管理员为 `admin`，普通用户为 `normal`
- `role`: 角色
- `status`: 状态，当前启用为 `1`，禁用为 `0`
- `lastLoginTime`: 最后登录时间，建议前端按字符串处理

### 4.2 通用响应结构

```ts
export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}
```

## 5. 接口清单

当前后端实际可用接口如下：

1. 初始化超级管理员
2. 管理员登录
3. 新增管理员
4. 普通用户注册
5. 普通用户登录

---

## 6. 接口详情

### 6.1 初始化超级管理员

- 路径：`/user/init`
- 方法：`GET` 或 `POST`
- 用途：初始化系统默认超级管理员
- 是否需要登录：否

说明：

- 如果数据库中不存在 `admin` 账号，会创建一个默认超级管理员
- 默认账号：`admin`
- 默认密码：`123456`
- 密码会由后端加密存储
- 如果已经存在，则不会重复创建

请求示例：

```http
GET /user/init
```

成功响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": "超级管理员初始化成功"
}
```

已存在响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": "超级管理员已存在"
}
```

前端建议：

- 该接口更适合开发环境、测试环境或后台初始化按钮使用
- 正式前台页面通常不需要暴露给普通用户

### 6.2 管理员登录

- 路径：`/user/admin/login`
- 方法：`POST`
- 用途：管理员登录
- 是否需要登录：否

请求参数：

```ts
export interface AdminLoginDTO {
  username: string
  password: string
}
```

请求示例：

```json
{
  "username": "admin",
  "password": "123456"
}
```

成功响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "displayName": "系统超级管理员",
    "userType": "admin",
    "role": "super_admin",
    "status": 1,
    "lastLoginTime": "2026-04-11T20:30:00"
  }
}
```

失败场景：

- 账号为空
- 密码为空
- 管理员账号不存在
- 管理员账号已被禁用
- 管理员账号或密码错误

前端建议：

- 登录成功后将 `data` 缓存在 `pinia` 和 `localStorage`
- 当前后端不返回 token，所以先不要设计基于 token 的路由守卫
- 可以根据 `role` 区分 `super_admin` 和 `admin`

### 6.3 新增管理员

- 路径：`/user/admin/add`
- 方法：`POST`
- 用途：由超级管理员新增管理员
- 是否需要登录：当前后端未做登录拦截，但请求体中必须传操作人账号密码

请求参数：

```ts
export interface AdminCreateDTO {
  operatorUsername: string
  operatorPassword: string
  username: string
  password: string
  realName?: string
  phone?: string
  email?: string
  avatarUrl?: string
  scenicSpot?: string
  remark?: string
}
```

字段说明：

- `operatorUsername`: 操作人账号，必须是超级管理员账号
- `operatorPassword`: 操作人密码
- `username`: 新管理员账号
- `password`: 新管理员密码
- `realName`: 真实姓名，缺省时后端默认写入 `普通管理员`
- `phone`: 手机号
- `email`: 邮箱
- `avatarUrl`: 头像地址
- `scenicSpot`: 所属景区
- `remark`: 备注

请求示例：

```json
{
  "operatorUsername": "admin",
  "operatorPassword": "123456",
  "username": "manager01",
  "password": "123456",
  "realName": "张三",
  "phone": "13800138000",
  "email": "manager01@wanlv.com",
  "avatarUrl": "https://example.com/avatar.png",
  "scenicSpot": "西湖景区",
  "remark": "负责票务管理"
}
```

成功响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 2,
    "username": "manager01",
    "displayName": "张三",
    "userType": "admin",
    "role": "admin",
    "status": 1,
    "lastLoginTime": null
  }
}
```

失败场景：

- 操作人账号或密码不能为空
- 新管理员账号或密码不能为空
- 操作人不存在
- 操作人已被禁用
- 操作人账号或密码错误
- 只有超级管理员才能新增管理员
- 管理员账号已存在

前端建议：

- 这个页面只建议给超级管理员入口显示
- 如果当前前端已经缓存了超级管理员账号，可以自动填充 `operatorUsername`
- `operatorPassword` 建议每次手动输入，不建议明文长期缓存

### 6.4 普通用户注册

- 路径：`/user/normal/register`
- 方法：`POST`
- 用途：普通用户注册
- 是否需要登录：否

请求参数：

```ts
export interface NormalUserRegisterDTO {
  username: string
  password: string
  nickname?: string
  phone?: string
  email?: string
  avatarUrl?: string
  gender?: number
  age?: number
  interestTags?: string
}
```

字段说明：

- `username`: 用户账号，必填
- `password`: 密码，必填
- `nickname`: 昵称，未传时后端默认使用 `username`
- `phone`: 手机号
- `email`: 邮箱
- `avatarUrl`: 头像地址
- `gender`: 性别，整型字段，建议前端自行约定 `0/1/2`
- `age`: 年龄
- `interestTags`: 兴趣标签，后端当前接收的是字符串，建议前端传 JSON 字符串

`interestTags` 推荐传值示例：

```json
"[\"摄影\",\"徒步\",\"美食\"]"
```

完整请求示例：

```json
{
  "username": "user01",
  "password": "123456",
  "nickname": "小王",
  "phone": "13900139000",
  "email": "user01@test.com",
  "avatarUrl": "https://example.com/user01.png",
  "gender": 1,
  "age": 24,
  "interestTags": "[\"摄影\",\"徒步\"]"
}
```

成功响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 10,
    "username": "user01",
    "displayName": "小王",
    "userType": "normal",
    "role": "normal_user",
    "status": 1,
    "lastLoginTime": "2026-04-11T20:30:00"
  }
}
```

失败场景：

- 普通用户账号或密码不能为空
- 普通用户账号已存在

前端建议：

- 注册成功后可以直接跳转到普通用户首页
- 也可以把返回的 `UserLoginVO` 当作“注册即登录”的结果直接缓存

### 6.5 普通用户登录

- 路径：`/user/normal/login`
- 方法：`POST`
- 用途：普通用户登录
- 是否需要登录：否

请求参数：

```ts
export interface NormalUserLoginDTO {
  username: string
  password: string
}
```

请求示例：

```json
{
  "username": "user01",
  "password": "123456"
}
```

成功响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 10,
    "username": "user01",
    "displayName": "小王",
    "userType": "normal",
    "role": "normal_user",
    "status": 1,
    "lastLoginTime": "2026-04-11T20:35:00"
  }
}
```

失败场景：

- 普通用户账号或密码不能为空
- 普通用户账号不存在
- 普通用户账号已被禁用
- 普通用户账号或密码错误

前端建议：

- 登录成功后缓存用户信息
- 根据 `userType` 和 `role` 决定进入普通用户端还是管理端

## 7. 推荐前端接口封装

```ts
import request from '@/utils/request'

export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

export interface UserLoginVO {
  id: number
  username: string
  displayName: string
  userType: 'admin' | 'normal'
  role: string
  status: number
  lastLoginTime: string | null
}

export const initSuperAdminApi = () =>
  request.get<any, ApiResponse<string>>('/user/init')

export const adminLoginApi = (data: {
  username: string
  password: string
}) =>
  request.post<any, ApiResponse<UserLoginVO>>('/user/admin/login', data)

export const createAdminApi = (data: {
  operatorUsername: string
  operatorPassword: string
  username: string
  password: string
  realName?: string
  phone?: string
  email?: string
  avatarUrl?: string
  scenicSpot?: string
  remark?: string
}) =>
  request.post<any, ApiResponse<UserLoginVO>>('/user/admin/add', data)

export const normalRegisterApi = (data: {
  username: string
  password: string
  nickname?: string
  phone?: string
  email?: string
  avatarUrl?: string
  gender?: number
  age?: number
  interestTags?: string
}) =>
  request.post<any, ApiResponse<UserLoginVO>>('/user/normal/register', data)

export const normalLoginApi = (data: {
  username: string
  password: string
}) =>
  request.post<any, ApiResponse<UserLoginVO>>('/user/normal/login', data)
```

## 8. 页面开发建议

### 8.1 建议优先开发页面

1. 普通用户登录页
2. 普通用户注册页
3. 管理员登录页
4. 超级管理员新增管理员页

### 8.2 Element Plus 表单建议

- 登录页使用 `el-form + el-input + el-button`
- 密码框统一加 `show-password`
- 注册页对手机号、邮箱做前端格式校验
- 提交按钮要加 loading，避免重复提交
- 成功后统一 `ElMessage.success`
- 失败直接读取后端 `msg` 提示

### 8.3 Pinia 建议存储字段

```ts
export interface AuthState {
  userInfo: UserLoginVO | null
  isLogin: boolean
}
```

建议缓存：

- `userInfo`
- `isLogin`

当前不建议缓存：

- 管理员新增接口里的 `operatorPassword`

## 9. 当前接口限制

- 当前后端未返回 JWT token
- 当前后端未实现真正的登录态校验
- 当前管理员新增接口仍依赖请求体中的 `operatorUsername + operatorPassword`
- 当前普通用户注册接口中的 `interestTags` 不是数组，而是字符串
- 当前只有用户相关接口，暂无景区、路线、消息等业务接口可供前端联调

## 10. 给前端 Codex 的任务说明建议

如果你要把这份文档直接交给前端 Codex，可以附上这段说明：

```text
请基于 docs/frontend-api.md 开发一个 Vue3 + Element Plus 前端项目，优先完成：
1. 普通用户登录页
2. 普通用户注册页
3. 管理员登录页
4. 新增管理员页

要求：
1. 使用 axios 封装请求
2. 使用 pinia 管理登录用户信息
3. 所有接口统一按响应体 code 判断成功失败
4. 失败提示直接显示后端 msg
5. 表单使用 Element Plus，并补充必要校验
6. 页面风格简洁、可直接联调后端
```
