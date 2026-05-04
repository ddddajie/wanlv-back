# wanlv-back 景点预约接口联调文档

本文档用于前端对接景点预约主功能。当前后端已实现预约闭环：

1. 查询支持预约的景点
2. 查询景点可预约时段
3. 创建预约订单
4. 查询我的预约
5. 取消预约
6. 管理端维护预约规则
7. 管理端生成和维护预约时段
8. 管理端查询预约订单

Agent 专属工具接口本阶段暂不单独实现；订单创建接口保留 `sourceType = AGENT`、`agentSessionCode`、`clientRequestId` 字段，后续 Agent 可复用同一个下单接口。

---

## 1. 基础信息

- 项目名称：`wanlv-back`
- 本地默认地址：`http://127.0.0.1:8080`
- 用户端接口前缀：`/api/reservation`
- 管理端接口前缀：`/api/admin/reservation`
- 请求格式：`application/json`
- 响应格式：`application/json`
- 当前阶段未接入统一登录态校验，接口直接传 `userId`
- 前端必须优先根据响应体中的 `code` 判断成功或失败

统一响应结构：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

字段说明：

- `code`：业务状态码，`200` 表示成功，`500` 表示业务失败
- `msg`：响应消息
- `data`：具体返回数据

---

## 2. 通用约定

### 2.1 时间格式

- 日期：`yyyy-MM-dd`，例如 `2026-05-05`
- 时间：`HH:mm:ss`，例如 `10:00:00`
- 日期时间：后端返回 `LocalDateTime`，通常形如 `2026-05-04T13:00:00`

### 2.2 状态枚举

预约规则和预约时段状态：

| 值 | 说明 |
|---|---|
| `0` | 停用 |
| `1` | 启用 |

预约订单状态：

| 值 | 说明 |
|---|---|
| `PENDING` | 待确认 |
| `CONFIRMED` | 已预约 |
| `CANCELLED` | 已取消 |
| `COMPLETED` | 已完成 |
| `EXPIRED` | 已过期 |

预约来源：

| 值 | 说明 |
|---|---|
| `FRONTEND` | 前端用户主动预约 |
| `AGENT` | AI Agent 调用预约 |
| `ADMIN` | 后台创建 |

### 2.3 分页返回

分页接口 `data` 结构统一为：

```json
{
  "total": 1,
  "records": []
}
```

---

## 3. 用户端接口

### 3.1 查询支持预约的景点

```http
GET /api/reservation/spots/enabled
```

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `scenicAreaId` | 否 | 景区 ID |
| `keyword` | 否 | 景点名称关键词 |

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "spotId": 1,
      "scenicAreaId": 1,
      "spotName": "古城博物馆",
      "shortIntro": "展示古城历史文化的核心景点",
      "reservationEnabled": 1,
      "reservationNotice": "请提前30分钟到场",
      "advanceReservationDays": 7,
      "minAdvanceMinutes": 30
    }
  ]
}
```

---

### 3.2 查询景点可预约时段

```http
GET /api/reservation/slots
```

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `spotId` | 是 | 景点 ID |
| `visitDate` | 是 | 预约日期，格式 `yyyy-MM-dd` |

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "spotId": 1,
    "spotName": "古城博物馆",
    "visitDate": "2026-05-05",
    "slots": [
      {
        "id": 10,
        "slotId": 10,
        "scenicAreaId": 1,
        "scenicName": "古城景区",
        "spotId": 1,
        "spotName": "古城博物馆",
        "ruleId": 2,
        "visitDate": "2026-05-05",
        "startTime": "10:00:00",
        "endTime": "12:00:00",
        "totalCapacity": 100,
        "reservedCount": 20,
        "remainingCount": 80,
        "available": true,
        "status": 1,
        "remark": null,
        "createTime": "2026-05-04T13:00:00",
        "updateTime": "2026-05-04T13:00:00"
      }
    ]
  }
}
```

前端建议：

- 只允许用户选择 `available = true` 的时段
- `remainingCount` 可直接展示为剩余名额
- 如果 `available = false`，可能原因是停用、已满、未满足最少提前预约时间、超出可预约日期范围

---

### 3.3 创建预约订单

```http
POST /api/reservation/orders
```

请求体：

```json
{
  "userId": 1,
  "slotId": 10,
  "visitorCount": 2,
  "contactName": "张三",
  "contactPhone": "13800000000",
  "sourceType": "FRONTEND",
  "clientRequestId": "frontend-20260504-0001",
  "remark": "用户前端预约"
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `userId` | 是 | 当前普通用户 ID |
| `slotId` | 是 | 预约时段 ID |
| `visitorCount` | 是 | 预约人数，必须大于 0 |
| `contactName` | 否 | 联系人姓名 |
| `contactPhone` | 否 | 联系人手机号 |
| `sourceType` | 否 | 预约来源，前端传 `FRONTEND`，不传默认 `FRONTEND` |
| `agentSessionCode` | 否 | Agent 会话编码，前端普通预约不用传 |
| `clientRequestId` | 否 | 幂等请求 ID，建议前端每次提交生成唯一值 |
| `remark` | 否 | 备注 |

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1001,
    "reservationNo": "YY20260504123456789",
    "userId": 1,
    "nickname": "游客A",
    "scenicAreaId": 1,
    "scenicName": "古城景区",
    "spotId": 1,
    "spotName": "古城博物馆",
    "slotId": 10,
    "visitDate": "2026-05-05",
    "startTime": "10:00:00",
    "endTime": "12:00:00",
    "visitorCount": 2,
    "contactName": "张三",
    "contactPhone": "13800000000",
    "status": "CONFIRMED",
    "sourceType": "FRONTEND",
    "agentSessionCode": null,
    "clientRequestId": "frontend-20260504-0001",
    "remark": "用户前端预约",
    "cancelReason": null,
    "cancelTime": null,
    "createTime": "2026-05-04T13:00:00",
    "updateTime": "2026-05-04T13:00:00"
  }
}
```

幂等说明：

- 如果 `clientRequestId` 已存在，后端会直接返回已有订单详情
- 前端可使用 `Date.now()`、`crypto.randomUUID()` 等方式生成

---

### 3.4 查询我的预约

```http
GET /api/reservation/orders/my
```

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `userId` | 是 | 当前普通用户 ID |
| `status` | 否 | 订单状态 |
| `pageNum` | 否 | 页码，默认 `1` |
| `pageSize` | 否 | 每页数量，默认 `10` |

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 1,
    "records": [
      {
        "id": 1001,
        "reservationNo": "YY20260504123456789",
        "userId": 1,
        "nickname": "游客A",
        "scenicAreaId": 1,
        "scenicName": "古城景区",
        "spotId": 1,
        "spotName": "古城博物馆",
        "slotId": 10,
        "visitDate": "2026-05-05",
        "startTime": "10:00:00",
        "endTime": "12:00:00",
        "visitorCount": 2,
        "contactName": "张三",
        "contactPhone": "13800000000",
        "status": "CONFIRMED",
        "sourceType": "FRONTEND",
        "createTime": "2026-05-04T13:00:00"
      }
    ]
  }
}
```

---

### 3.5 取消预约

```http
POST /api/reservation/orders/{reservationNo}/cancel
```

路径参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `reservationNo` | 是 | 预约编号 |

请求体：

```json
{
  "userId": 1,
  "cancelReason": "行程有变"
}
```

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

取消规则：

- 只能取消归属于当前 `userId` 的订单
- 只有 `PENDING`、`CONFIRMED` 状态允许取消
- 取消成功后会回滚时段已预约人数

---

## 4. 管理端接口

### 4.1 创建预约规则

```http
POST /api/admin/reservation/rules
```

请求体：

```json
{
  "scenicAreaId": 1,
  "spotId": 1,
  "startTime": "10:00:00",
  "endTime": "12:00:00",
  "totalCapacity": 100,
  "weekDays": "1,2,3,4,5,6,7",
  "advanceDays": 7,
  "remark": "上午预约时段",
  "createBy": 1
}
```

响应 `data` 为预约规则对象：

```json
{
  "id": 1,
  "scenicAreaId": 1,
  "spotId": 1,
  "spotName": "古城博物馆",
  "startTime": "10:00:00",
  "endTime": "12:00:00",
  "totalCapacity": 100,
  "weekDays": "1,2,3,4,5,6,7",
  "advanceDays": 7,
  "status": 1,
  "remark": "上午预约时段",
  "createTime": "2026-05-04T13:00:00",
  "updateTime": "2026-05-04T13:00:00"
}
```

---

### 4.2 修改预约规则

```http
PUT /api/admin/reservation/rules/{id}
```

请求体：

```json
{
  "startTime": "10:00:00",
  "endTime": "12:00:00",
  "totalCapacity": 120,
  "weekDays": "1,2,3,4,5",
  "advanceDays": 7,
  "status": 1,
  "remark": "工作日开放",
  "updateBy": 1
}
```

说明：

- 修改规则只影响后续生成的时段
- 已生成的预约时段不会自动覆盖

---

### 4.3 启用或停用预约规则

```http
PUT /api/admin/reservation/rules/{id}/status
```

请求体：

```json
{
  "status": 0,
  "updateBy": 1
}
```

响应：

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

---

### 4.4 查询预约规则列表

```http
GET /api/admin/reservation/rules
```

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `scenicAreaId` | 否 | 景区 ID |
| `spotId` | 否 | 景点 ID |
| `status` | 否 | 状态 |
| `pageNum` | 否 | 页码 |
| `pageSize` | 否 | 每页数量 |

响应 `data.records` 为规则对象数组。

---

### 4.5 手动生成预约时段

```http
POST /api/admin/reservation/slots/generate
```

请求体：

```json
{
  "spotId": 1,
  "days": 7
}
```

也可以按景区生成：

```json
{
  "scenicAreaId": 1,
  "days": 7
}
```

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "generatedCount": 14,
    "skipCount": 2
  }
}
```

说明：

- `generatedCount`：本次新生成的时段数量
- `skipCount`：因星期不匹配或时段已存在而跳过的数量

---

### 4.6 新增临时预约时段

```http
POST /api/admin/reservation/slots
```

请求体：

```json
{
  "scenicAreaId": 1,
  "spotId": 1,
  "visitDate": "2026-05-05",
  "startTime": "16:00:00",
  "endTime": "18:00:00",
  "totalCapacity": 60,
  "remark": "节假日临时加场",
  "createBy": 1
}
```

响应 `data` 为预约时段对象。

---

### 4.7 修改预约时段

```http
PUT /api/admin/reservation/slots/{id}
```

请求体：

```json
{
  "totalCapacity": 80,
  "status": 1,
  "remark": "临时调整容量",
  "updateBy": 1
}
```

说明：

- `totalCapacity` 不能小于当前 `reservedCount`
- 关闭时段请传 `status = 0`

---

### 4.8 查询预约时段列表

```http
GET /api/admin/reservation/slots
```

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `scenicAreaId` | 否 | 景区 ID |
| `spotId` | 否 | 景点 ID |
| `visitDate` | 否 | 指定日期 |
| `startDate` | 否 | 开始日期 |
| `endDate` | 否 | 结束日期 |
| `status` | 否 | 时段状态 |
| `pageNum` | 否 | 页码 |
| `pageSize` | 否 | 每页数量 |

响应 `data.records` 为预约时段对象数组。

---

### 4.9 查询预约订单列表

```http
GET /api/admin/reservation/orders
```

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `scenicAreaId` | 否 | 景区 ID |
| `spotId` | 否 | 景点 ID |
| `userId` | 否 | 用户 ID |
| `visitDate` | 否 | 预约日期 |
| `status` | 否 | 订单状态 |
| `sourceType` | 否 | 预约来源 |
| `reservationNo` | 否 | 预约编号 |
| `pageNum` | 否 | 页码 |
| `pageSize` | 否 | 每页数量 |

响应 `data.records` 为预约订单对象数组。

---

## 5. 前端 TypeScript 类型建议

```ts
export interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export interface ReservationEnabledSpot {
  spotId: number
  scenicAreaId: number
  spotName: string
  shortIntro?: string
  reservationEnabled: number
  reservationNotice?: string
  advanceReservationDays: number
  minAdvanceMinutes: number
}

export interface ReservationSlot {
  id: number
  slotId: number
  scenicAreaId: number
  scenicName?: string
  spotId: number
  spotName?: string
  ruleId?: number
  visitDate: string
  startTime: string
  endTime: string
  totalCapacity: number
  reservedCount: number
  remainingCount: number
  available: boolean
  status: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface ReservationOrder {
  id: number
  reservationNo: string
  userId: number
  nickname?: string
  scenicAreaId: number
  scenicName?: string
  spotId: number
  spotName?: string
  slotId: number
  visitDate: string
  startTime: string
  endTime: string
  visitorCount: number
  contactName?: string
  contactPhone?: string
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'EXPIRED'
  sourceType: 'FRONTEND' | 'AGENT' | 'ADMIN'
  agentSessionCode?: string
  clientRequestId?: string
  remark?: string
  cancelReason?: string
  cancelTime?: string
  createTime?: string
  updateTime?: string
}
```

---

## 6. 前端封装示例

```ts
import request from './request'

export function listReservationEnabledSpots(params?: {
  scenicAreaId?: number
  keyword?: string
}) {
  return request.get('/api/reservation/spots/enabled', { params })
}

export function listReservationSlots(params: {
  spotId: number
  visitDate: string
}) {
  return request.get('/api/reservation/slots', { params })
}

export function createReservationOrder(data: {
  userId: number
  slotId: number
  visitorCount: number
  contactName?: string
  contactPhone?: string
  sourceType?: 'FRONTEND'
  clientRequestId?: string
  remark?: string
}) {
  return request.post('/api/reservation/orders', data)
}

export function pageMyReservationOrders(params: {
  userId: number
  status?: string
  pageNum?: number
  pageSize?: number
}) {
  return request.get('/api/reservation/orders/my', { params })
}

export function cancelReservationOrder(
  reservationNo: string,
  data: { userId: number; cancelReason?: string },
) {
  return request.post(`/api/reservation/orders/${reservationNo}/cancel`, data)
}
```

---

## 7. 常见失败消息

当前业务失败通常仍返回 HTTP 200，但响应体 `code = 500`。常见 `msg`：

- `景点不存在`
- `景点不支持预约`
- `预约时段不存在`
- `预约日期超出可预约范围`
- `当前时段不满足最少提前预约时间`
- `剩余名额不足或当前时段不可预约`
- `用户不存在或状态异常`
- `预约订单不存在`
- `当前状态不允许取消`
- `预约容量不能小于已预约人数`
- `预约时段已存在`

前端处理建议：

- 所有接口统一拦截 `res.code !== 200`
- 下单按钮提交后禁用，接口返回后再恢复
- 下单前生成 `clientRequestId`，避免重复点击或网络重试造成重复预约
- 取消成功后刷新“我的预约”和对应时段剩余名额
