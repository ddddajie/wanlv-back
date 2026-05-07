# 实名预约前端接口变更文档

本文档用于前端同步实现“实名认证 + 预约游客身份明细”功能。后端不新建数据库，已在原用户和预约业务中扩展实名字段与游客明细表。

## 1. 普通用户实名认证

### 接口

```http
POST /user/normal/real-name/verify
Content-Type: application/json
```

### 请求体

```json
{
  "userId": 1,
  "realName": "张三",
  "idCardNo": "110101199001011234"
}
```

### 字段说明

| 字段 | 必填 | 说明 |
|---|---|---|
| `userId` | 是 | 当前普通用户 ID |
| `realName` | 是 | 真实姓名 |
| `idCardNo` | 是 | 身份证号，前端提交明文，后端只保存脱敏值和哈希 |

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "user001",
    "nickname": "游客A",
    "phone": "13800000000",
    "realNameStatus": 1,
    "realName": "张三",
    "idCardMasked": "110101********1234",
    "realNameTime": "2026-05-06T16:30:00"
  }
}
```

### 前端处理建议

- 用户进入预约前，如果 `realNameStatus !== 1`，先引导到实名认证。
- 身份证号只在提交表单时使用，不要在页面状态、日志、URL 中长期保留。
- 接口失败时直接展示后端 `msg`，常见失败包括“身份证号格式不正确”“真实姓名和身份证号不能为空”。

## 2. 用户信息返回字段变化

普通用户详情、登录返回中会增加实名状态字段。

### `UserLoginVO` 新增字段

```ts
interface UserLoginVO {
  id: number
  username: string
  displayName: string
  userType: 'normal' | 'admin'
  role: string
  status: number
  realNameStatus?: 0 | 1 | 2
  lastLoginTime?: string
}
```

### `NormalUserVO` 新增字段

```ts
interface NormalUserVO {
  id: number
  username: string
  nickname?: string
  phone?: string
  email?: string
  avatarUrl?: string
  gender?: number
  age?: number
  interestTags?: string
  status: number
  realNameStatus: 0 | 1 | 2
  realName?: string
  idCardMasked?: string
  realNameTime?: string
  lastLoginTime?: string
  createTime?: string
  updateTime?: string
}
```

实名状态含义：

| 值 | 含义 |
|---|---|
| `0` | 未实名 |
| `1` | 已实名 |
| `2` | 实名失败 |

## 3. 创建预约订单接口变更

### 接口

```http
POST /reservation/orders
Content-Type: application/json
```

### 新请求体

```json
{
  "userId": 1,
  "slotId": 10,
  "visitorCount": 2,
  "contactName": "张三",
  "contactPhone": "13800000000",
  "visitors": [
    {
      "realName": "张三",
      "idCardNo": "110101199001011234",
      "booker": true
    },
    {
      "realName": "李四",
      "idCardNo": "110101199002021234",
      "booker": false
    }
  ],
  "sourceType": "FRONTEND",
  "clientRequestId": "frontend-20260506-0001",
  "remark": "前端预约"
}
```

### 字段说明

| 字段 | 必填 | 说明 |
|---|---|---|
| `userId` | 是 | 当前普通用户 ID |
| `slotId` | 是 | 预约时段 ID |
| `visitorCount` | 建议传 | 预约人数。传了就必须等于 `visitors.length` |
| `contactName` | 否 | 联系人姓名 |
| `contactPhone` | 否 | 联系人手机号 |
| `visitors` | 多人必填 | 入园人实名信息列表 |
| `visitors[].realName` | 是 | 入园人真实姓名 |
| `visitors[].idCardNo` | 是 | 入园人身份证号 |
| `visitors[].booker` | 是 | 是否为当前账号本人 |
| `sourceType` | 否 | 前端传 `FRONTEND`，不传默认 `FRONTEND` |
| `clientRequestId` | 建议传 | 幂等 ID，避免重复提交 |
| `remark` | 否 | 备注 |

### 重要规则

- 下单用户必须先完成实名认证。
- `visitors` 中必须包含当前账号本人，并且该项 `booker = true`。
- `booker = true` 的身份证必须与当前账号实名信息一致。
- 同一预约单不能重复填写同一身份证。
- 同一身份证在同一景点、同一游玩日期下，只能有一个未取消预约。
- 单次预约最多 5 名游客。
- 如果旧页面只预约 1 人，可以暂时不传 `visitors`，后端会自动使用当前实名用户本人；多人预约必须传 `visitors`。

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1001,
    "reservationNo": "YY20260506123456789",
    "userId": 1,
    "nickname": "游客A",
    "scenicAreaId": 1,
    "scenicName": "古城景区",
    "spotId": 10,
    "spotName": "古城博物馆",
    "slotId": 20,
    "visitDate": "2026-05-07",
    "startTime": "10:00:00",
    "endTime": "12:00:00",
    "visitorCount": 2,
    "visitors": [
      {
        "realName": "张三",
        "idCardMasked": "110101********1234",
        "booker": true,
        "status": "CONFIRMED"
      },
      {
        "realName": "李四",
        "idCardMasked": "110101********1234",
        "booker": false,
        "status": "CONFIRMED"
      }
    ],
    "contactName": "张三",
    "contactPhone": "13800000000",
    "status": "CONFIRMED",
    "sourceType": "FRONTEND",
    "clientRequestId": "frontend-20260506-0001",
    "remark": "前端预约",
    "createTime": "2026-05-06T16:35:00"
  }
}
```

## 4. 查询预约订单返回变化

以下接口返回的订单对象会增加 `visitors` 字段：

```http
GET /reservation/orders/my
GET /reservation/admin/orders
```

订单类型建议更新为：

```ts
interface ReservationVisitor {
  realName: string
  idCardMasked: string
  booker: boolean
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'EXPIRED'
}

interface ReservationOrder {
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
  visitors?: ReservationVisitor[]
  contactName?: string
  contactPhone?: string
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'EXPIRED'
  sourceType: 'FRONTEND' | 'AGENT' | 'ADMIN'
  clientRequestId?: string
  remark?: string
  cancelReason?: string
  cancelTime?: string
  createTime?: string
  updateTime?: string
}
```

## 5. 前端页面改造建议

- 个人中心增加实名认证状态展示和实名表单。
- 预约确认页增加“入园人信息”区域：
  - 默认带出当前实名用户本人，并标记为预约本人。
  - 支持添加同行人，最多总计 5 人。
  - 每个同行人填写姓名和身份证号。
- 下单按钮前置校验：
  - 当前用户未实名时，不允许提交预约。
  - 入园人为空时，不允许提交多人预约。
  - `booker = true` 必须且只能有一个。
  - `visitorCount` 使用 `visitors.length` 自动计算。
- 订单详情页展示游客姓名和脱敏身份证号，不展示完整身份证号。

## 6. 前端 API 封装示例

```ts
export function verifyNormalUserRealName(data: {
  userId: number
  realName: string
  idCardNo: string
}) {
  return request.post('/user/normal/real-name/verify', data)
}

export function createReservationOrder(data: {
  userId: number
  slotId: number
  visitorCount: number
  contactName?: string
  contactPhone?: string
  visitors?: Array<{
    realName: string
    idCardNo: string
    booker: boolean
  }>
  sourceType?: 'FRONTEND'
  clientRequestId?: string
  remark?: string
}) {
  return request.post('/reservation/orders', data)
}
```
