# Agent 景点预约接口对接文档

本文档用于 Agent 服务对接后端景点预约工具接口。聊天主链路保持不变：前端调用后端 `ChatController`，后端转发给 Agent 服务；Agent 服务识别预约意图后，再调用本文档中的预约工具接口完成查询、匹配和下单。

## 1. 基础信息

- 接口用途：供 Agent 服务调用，辅助完成景点预约
- 接口前缀：`/reservation/agent`
- 请求格式：`application/json`
- 响应格式：`application/json`
- 日期格式：`yyyy-MM-dd`，例如 `2026-05-05`
- 时间格式：`HH:mm:ss`，例如 `10:00:00`
- 统一响应结构：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

说明：

- `code = 200` 表示成功
- `code = 500` 表示业务失败或系统异常
- Agent 服务应优先根据 `code` 判断工具调用是否成功
- 如果线上网关统一加 `/api` 前缀，则实际路径为 `/api/reservation/agent/...`

## 2. 推荐调用流程

用户示例：

```text
帮我预约明天10点的景点1，两个人
```

Agent 推荐流程：

1. 识别用户意图为景点预约
2. 从自然语言中提取 `spotId`、`visitDate`、`targetTime`、`visitorCount`
3. 如果用户只说了景点名称，没有明确 `spotId`，先调用景点搜索接口
4. 调用时段匹配接口，获取可预约 `slotId`
5. 如果 `matchedSlot` 为空，向用户说明无可用时段，或引导用户选择其他时间
6. 如果 `userId`、预约人数等必要字段缺失，Agent 应先向用户追问或从会话上下文补齐
7. 字段齐全后调用创建预约接口
8. 创建成功后，优先使用后端返回的 `replyText` 作为最终回复

## 3. Agent 根据景点名称搜索可预约景点

```http
GET /reservation/agent/spots/search
```

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `keyword` | 是 | 景点名称关键词 |
| `scenicAreaId` | 否 | 景区 ID |

请求示例：

```http
GET /reservation/agent/spots/search?keyword=博物馆&scenicAreaId=1
```

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "spotId": 1,
      "spotName": "古城博物馆",
      "scenicAreaId": 1,
      "scenicName": "古城景区",
      "reservationEnabled": 1,
      "reservationNotice": "请提前30分钟到场",
      "advanceReservationDays": 7,
      "minAdvanceMinutes": 30,
      "matchType": "NAME_CONTAINS",
      "confidence": 0.8
    }
  ]
}
```

Agent 使用建议：

- 当用户直接提供 `spotId` 时，可以跳过该接口
- 当返回多个景点时，Agent 应结合用户上下文选择最可能的景点；无法判断时应向用户确认
- 当返回空数组时，说明没有匹配到可预约景点，Agent 应提示用户换一个景点名称
- `confidence` 越接近 `1`，说明名称匹配程度越高；低置信度时建议向用户确认

`matchType` 说明：

| 值 | 说明 |
|---|---|
| `NAME_EXACT` | 景点名称和关键词完全一致 |
| `NAME_CONTAINS` | 景点名称包含关键词 |
| `NAME_FUZZY` | 模糊匹配结果 |
| `UNKNOWN` | 无法判断匹配方式 |

## 4. Agent 查询指定时间附近的可预约时段

```http
GET /reservation/agent/slots/match
```

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `spotId` | 是 | 景点 ID |
| `visitDate` | 是 | 预约日期，格式 `yyyy-MM-dd` |
| `targetTime` | 否 | 用户期望时间，格式 `HH:mm:ss` |
| `visitorCount` | 否 | 预约人数，默认 `1` |

请求示例：

```http
GET /reservation/agent/slots/match?spotId=1&visitDate=2026-05-05&targetTime=10:00:00&visitorCount=2
```

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "matchedSlot": {
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
    },
    "candidateSlots": [
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
    ],
    "matchType": "EXACT",
    "replyText": "已找到古城博物馆在2026年5月5日 10:00-12:00的可预约时段，当前剩余80个名额，可预约2人。"
  }
}
```

`matchType` 说明：

| 值 | 说明 |
|---|---|
| `EXACT` | `targetTime` 正好落在某个可预约时段内 |
| `NEAREST` | 没有精确命中，返回距离 `targetTime` 最近的可用时段 |
| `FIRST_AVAILABLE` | 未传 `targetTime`，返回当天第一个可用时段 |
| `NO_AVAILABLE` | 没有满足人数要求的可用时段 |

无可用时段示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "matchedSlot": null,
    "candidateSlots": [],
    "matchType": "NO_AVAILABLE",
    "replyText": "古城博物馆在2026年5月5日暂无满足2人的可预约时段。"
  }
}
```

Agent 使用建议：

- 创建预约订单必须使用 `matchedSlot.slotId`
- 如果 `matchedSlot` 为 `null`，不要调用创建预约接口
- 如果 `matchType = NEAREST`，建议先让用户确认是否接受最近时段，再下单
- `candidateSlots` 只包含 `available = true` 且 `remainingCount >= visitorCount` 的时段

## 5. Agent 创建预约订单

```http
POST /reservation/agent/orders
```

请求体：

```json
{
  "userId": 1,
  "slotId": 10,
  "visitorCount": 2,
  "agentSessionCode": "session-20260504-001",
  "clientRequestId": "agent-session-20260504-001-tool-001",
  "remark": "用户自然语言触发预约"
}
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `userId` | 是 | 当前普通用户 ID |
| `slotId` | 是 | 预约时段 ID，来自时段匹配接口的 `matchedSlot.slotId` |
| `visitorCount` | 是 | 预约人数，必须大于 `0` |
| `agentSessionCode` | 否 | Agent 会话编码 |
| `clientRequestId` | 否 | 幂等请求 ID，强烈建议传 |
| `remark` | 否 | 备注 |

后端处理规则：

- 后端内部固定 `sourceType = AGENT`
- 后端根据 `userId` 查询用户信息，使用用户 `nickname` 作为联系人姓名，使用用户 `phone` 作为联系人手机号；如果 `nickname` 为空，则使用 `username`
- 如果 `clientRequestId` 已存在，后端会直接返回已有订单，避免重复预约
- 后端会校验用户状态、景点预约状态、预约日期范围、最少提前预约时间、剩余名额等规则

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "success": true,
    "reservationNo": "YY202605050001",
    "status": "CONFIRMED",
    "scenicAreaId": 1,
    "scenicName": "古城景区",
    "spotId": 1,
    "spotName": "古城博物馆",
    "slotId": 10,
    "visitDate": "2026-05-05",
    "startTime": "10:00:00",
    "endTime": "12:00:00",
    "visitorCount": 2,
    "replyText": "已为你预约成功：古城博物馆，2026年5月5日 10:00-12:00，2人，预约编号 YY202605050001。"
  }
}
```

Agent 使用建议：

- 成功后优先将 `data.replyText` 返回给用户
- 不要自行修改预约编号、日期、时段、人数等关键字段
- 每次真实下单工具调用都应生成稳定且唯一的 `clientRequestId`
- 如果是同一次工具调用的网络重试，应复用同一个 `clientRequestId`

## 6. Agent 查询用户近期预约

```http
GET /reservation/agent/orders/recent
```

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `userId` | 是 | 当前普通用户 ID |
| `status` | 否 | 预约订单状态，例如 `CONFIRMED`、`CANCELLED` |
| `limit` | 否 | 返回数量，默认 `5`，最大 `20` |

请求示例：

```http
GET /reservation/agent/orders/recent?userId=1&limit=5
```

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "reservationNo": "YY202605050001",
      "userId": 1,
      "scenicAreaId": 1,
      "scenicName": "古城景区",
      "spotId": 1,
      "spotName": "古城博物馆",
      "slotId": 10,
      "visitDate": "2026-05-05",
      "startTime": "10:00:00",
      "endTime": "12:00:00",
      "visitorCount": 2,
      "status": "CONFIRMED",
      "sourceType": "AGENT"
    }
  ]
}
```

Agent 使用建议：

- 用户问“我刚才预约了吗”“我明天有什么预约”时调用
- 用户没有提供预约编号但想取消预约时，先调用该接口辅助定位订单
- 如果返回多条可能订单，Agent 应让用户确认具体取消哪一条

## 7. Agent 推荐可预约时段

```http
GET /reservation/agent/slots/recommend
```

查询参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `spotId` | 是 | 景点 ID |
| `startDate` | 是 | 推荐起始日期，格式 `yyyy-MM-dd` |
| `days` | 否 | 向后查询天数，默认 `7`，最大 `30` |
| `targetTime` | 否 | 用户期望时间，格式 `HH:mm:ss` |
| `visitorCount` | 否 | 预约人数，默认 `1` |
| `limit` | 否 | 返回数量，默认 `5`，最大 `20` |

请求示例：

```http
GET /reservation/agent/slots/recommend?spotId=1&startDate=2026-05-05&days=7&targetTime=10:00:00&visitorCount=2&limit=3
```

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "recommendedSlots": [
      {
        "slotId": 10,
        "spotId": 1,
        "spotName": "古城博物馆",
        "visitDate": "2026-05-05",
        "startTime": "10:00:00",
        "endTime": "12:00:00",
        "remainingCount": 80,
        "available": true
      }
    ],
    "replyText": "已为你找到古城博物馆在2026年5月5日 10:00-12:00等1个可预约时段，可预约2人。"
  }
}
```

Agent 使用建议：

- 当 `/reservation/agent/slots/match` 返回 `NO_AVAILABLE` 时调用
- 推荐结果按日期和与 `targetTime` 的接近程度排序
- 用户接受某个推荐时段后，再使用该时段 `slotId` 调用创建预约接口

## 8. Agent 取消预约订单

```http
POST /reservation/agent/orders/{reservationNo}/cancel
```

路径参数：

| 参数 | 必填 | 说明 |
|---|---|---|
| `reservationNo` | 是 | 预约编号 |

请求体：

```json
{
  "userId": 1,
  "cancelReason": "用户通过Agent取消预约"
}
```

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "success": true,
    "reservationNo": "YY202605050001",
    "replyText": "已为你取消预约，预约编号 YY202605050001。"
  }
}
```

Agent 使用建议：

- 用户提供预约编号时，可直接调用取消接口
- 用户没有提供预约编号时，先调用近期预约接口辅助定位
- 如果近期预约中有多条候选，不要直接取消，应先让用户确认

## 9. 常见失败情况

失败响应示例：

```json
{
  "code": 500,
  "msg": "景点名称关键词不能为空",
  "data": null
}
```

常见 `msg`：

- `景点名称关键词不能为空`
- `预约人数必须大于0`
- `景点ID和预约日期不能为空`
- `景点不存在`
- `景点不支持预约`
- `预约日期超出可预约范围`
- `当前时段不满足最少提前预约时间`
- `剩余名额不足或当前时段不可预约`
- `用户不存在或状态异常`
- `预约来源不支持`

Agent 处理建议：

- 参数缺失类错误：向用户追问缺失信息
- 无可用时段：引导用户选择其他日期或时间
- 用户状态异常：提示用户先检查登录状态或账户状态
- 其他业务错误：将后端 `msg` 转成自然语言说明，不要继续重复下单

## 7. Agent 工具定义参考

```ts
type SearchReservationSpotsInput = {
  keyword: string
  scenicAreaId?: number
}

type MatchReservationSlotInput = {
  spotId: number
  visitDate: string
  targetTime?: string
  visitorCount?: number
}

type CreateAgentReservationOrderInput = {
  userId: number
  slotId: number
  visitorCount: number
  agentSessionCode?: string
  clientRequestId?: string
  remark?: string
}

type ListRecentReservationOrdersInput = {
  userId: number
  status?: string
  limit?: number
}

type RecommendReservationSlotsInput = {
  spotId: number
  startDate: string
  days?: number
  targetTime?: string
  visitorCount?: number
  limit?: number
}

type CancelAgentReservationOrderInput = {
  reservationNo: string
  userId: number
  cancelReason?: string
}
```
