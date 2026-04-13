# wanlv-back Agent 聊天接口文档

本文档用于给前端联调和页面开发使用，描述当前后端新增的 Agent 聊天相关接口。

当前后端职责：

1. 接收前端提问
2. 按用户和日期创建或复用当天会话
3. 将问题转发给 Agent 服务
4. 接收 Agent 回复并返回前端
5. 将聊天消息入库
6. 在需要时触发会话分析
7. 在需要时绑定会话景区

---

## 1. 基础信息

- 项目名称：`wanlv-back`
- 本地默认地址：`http://127.0.0.1:8080`
- 接口前缀：`/agent`
- 请求格式：`application/json`
- 响应格式：`application/json`

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
- `data`：接口返回数据

前端判断成功的标准：

- 必须判断 `code === 200`
- 不能只看 HTTP 状态码

---

## 2. 接口总览

当前新增接口如下：

1. `POST /agent/chat`：聊天提问
2. `POST /agent/session-analysis`：触发当天会话分析
3. `POST /agent/session/scenic-area/bind`：为当天会话绑定景区

---

## 3. 聊天提问

### 3.1 接口地址

`POST /agent/chat`

完整示例：

`http://127.0.0.1:8080/agent/chat`

### 3.2 功能说明

前端调用该接口后，后端会完成以下流程：

1. 根据 `userId + 当天日期` 查询当天会话
2. 如果不存在，则自动创建一条当天会话
3. 将用户消息写入 `visitor_message`
4. 调用 Agent 服务 `/chat`
5. 将 Agent 回复写入 `visitor_message`
6. 返回问答结果给前端

### 3.3 请求参数

请求体 DTO：`ChatAskDTO`

```json
{
  "userId": 10001,
  "content": "灵山大佛适合带老人去吗？",
  "messageType": "text",
  "voiceText": null,
  "scenicAreaId": null,
  "scenicAreaSource": null,
  "scenicAreaConfirmed": 0,
  "sourceType": "GLOBAL_CHAT",
  "sourceId": null
}
```

字段说明：

| 字段名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | `number` | 是 | 当前登录用户 ID |
| `content` | `string` | 否 | 文本提问内容；如果是文本提问，建议传该字段 |
| `messageType` | `string` | 否 | 消息类型，建议传 `text`、`voice`、`image` |
| `voiceText` | `string \| null` | 否 | 语音转文字内容；如果是语音提问，可以只传该字段 |
| `scenicAreaId` | `number \| null` | 否 | 当前已明确选择的景区 ID；如果未确认景区则传 `null` |
| `scenicAreaSource` | `string \| null` | 否 | 景区来源，例如 `FRONTEND`、`USER_CONFIRMED` |
| `scenicAreaConfirmed` | `number` | 否 | 是否已确认景区，`0` 未确认，`1` 已确认 |
| `sourceType` | `string` | 否 | 提问入口，例如 `GLOBAL_CHAT`、`SCENIC_DETAIL`、`ROUTE_DETAIL` |
| `sourceId` | `string \| null` | 否 | 页面业务主体 ID，例如景区 ID、路线 ID |

### 3.4 请求规则

前端至少要保证：

1. `userId` 有值
2. `content` 和 `voiceText` 至少有一个有值

推荐规则：

1. 文本输入时传 `content`
2. 语音输入时传 `messageType = "voice"`，并传 `voiceText`
3. 如果用户已经在景区详情页内发问，建议同时传 `scenicAreaId`
4. 如果用户还没有明确景区，`scenicAreaId` 传 `null`

### 3.5 成功响应

响应体 `data` 对应 `ChatAnswerVO`

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "answer": "灵山大佛整体比较适合带老人游览，建议优先安排节奏较缓的路线，并注意休息点分布。",
    "sessionId": 12,
    "sessionCode": "session_20260413_10001",
    "reportDate": "2026-04-13",
    "sessionType": "CONSULTATION",
    "scenicAreaId": null,
    "scenicAreaConfirmed": 0,
    "detectedScenicAreaId": null,
    "detectedScenicAreaName": "灵山胜境",
    "detectionConfidence": 0.85,
    "needScenicAreaConfirm": false
  }
}
```

字段说明：

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `answer` | `string` | Agent 的问答结果 |
| `sessionId` | `number` | 后端本地会话主键 ID |
| `sessionCode` | `string` | 当天会话唯一编码，后端和 Agent 都用它关联会话 |
| `reportDate` | `string` | 当天会话日期 |
| `sessionType` | `string` | 当前会话类型，可能为 `CONSULTATION` 或 `SCENIC_SERVICE` |
| `scenicAreaId` | `number \| null` | 当前后端已绑定的景区 ID |
| `scenicAreaConfirmed` | `number` | 当前后端是否已确认景区，`0/1` |
| `detectedScenicAreaId` | `number \| null` | Agent 识别出的候选景区 ID，仅供建议 |
| `detectedScenicAreaName` | `string \| null` | Agent 识别出的候选景区名称，仅供建议 |
| `detectionConfidence` | `number \| null` | Agent 对候选景区识别的置信度 |
| `needScenicAreaConfirm` | `boolean` | 是否建议前端继续引导用户确认景区 |

### 3.6 失败响应

```json
{
  "code": 500,
  "msg": "用户ID不能为空",
  "data": null
}
```

或：

```json
{
  "code": 500,
  "msg": "调用Agent服务失败",
  "data": null
}
```

### 3.7 前端联调建议

前端收到响应后建议这样处理：

1. 将 `answer` 作为机器人回复展示
2. 将 `sessionId`、`sessionCode` 缓存在当前聊天状态中
3. 如果 `needScenicAreaConfirm = true`，可引导用户进一步确认景区
4. 如果 `detectedScenicAreaName` 有值，可展示“是否在问这个景区”的提示
5. 如果当前页面本身就是景区详情页，建议首条提问直接传 `scenicAreaId`

---

## 4. 会话分析

### 4.1 接口地址

`POST /agent/session-analysis`

完整示例：

`http://127.0.0.1:8080/agent/session-analysis`

### 4.2 功能说明

该接口用于触发“当天会话分析”。

后端会完成以下流程：

1. 根据 `userId + reportDate` 查询当天会话
2. 查询该会话下的所有消息
3. 调用 Agent 服务 `/analyze-session`
4. 将分析结果回写到 `visitor_session`
5. 返回分析结果给前端或调用方

### 4.3 请求参数

请求体 DTO：`SessionAnalysisTriggerDTO`

```json
{
  "userId": 10001,
  "reportDate": "2026-04-13"
}
```

字段说明：

| 字段名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | `number` | 是 | 用户 ID |
| `reportDate` | `string` | 否 | 要分析的日期；为空时默认当天 |

### 4.4 成功响应

响应体 `data` 对应 `AgentSessionAnalysisVO`

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "summary": "本次会话主要围绕西湖景区进行咨询，用户重点关注老人出行、门票和路线节奏。",
    "overallSentiment": "positive",
    "sentimentScore": 86.5,
    "focusTopics": [
      "门票优惠",
      "老人出行",
      "路线规划"
    ],
    "interestTags": [
      "长者出游",
      "西湖景区",
      "慢游"
    ],
    "serviceSuggestions": [
      "建议补充老人票政策",
      "建议补充慢节奏路线推荐"
    ],
    "knowledgeGapPoints": [
      "尚未确认具体出游日期"
    ]
  }
}
```

字段说明：

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `summary` | `string` | 会话总结 |
| `overallSentiment` | `string` | 整体情感倾向 |
| `sentimentScore` | `number` | 情感分值 |
| `focusTopics` | `string[]` | 关注主题列表 |
| `interestTags` | `string[]` | 兴趣标签列表 |
| `serviceSuggestions` | `string[]` | 服务建议列表 |
| `knowledgeGapPoints` | `string[]` | 知识缺口列表 |

### 4.5 失败响应

```json
{
  "code": 500,
  "msg": "当前日期暂无可分析的会话",
  "data": null
}
```

或：

```json
{
  "code": 500,
  "msg": "Agent会话分析失败",
  "data": null
}
```

### 4.6 前端使用建议

这个接口更适合：

1. 管理端手动触发分析
2. 用户端“查看今日总结”
3. 后端定时任务调用

如果是普通聊天页面，不建议每轮聊天后都调用一次分析接口。

---

## 5. 绑定会话景区

### 5.1 接口地址

`POST /agent/session/scenic-area/bind`

完整示例：

`http://127.0.0.1:8080/agent/session/scenic-area/bind`

### 5.2 功能说明

当用户已经明确当前咨询的是哪个景区时，前端可以调用该接口，把当天会话绑定到具体景区。

后端会更新当天会话中的：

1. `scenicAreaId`
2. `scenicAreaSource`
3. `scenicAreaConfirmed`
4. `sessionType`

### 5.3 请求参数

请求体 DTO：`SessionScenicAreaBindDTO`

```json
{
  "userId": 10001,
  "scenicAreaId": 3,
  "scenicAreaSource": "USER_CONFIRMED",
  "scenicAreaConfirmed": 1,
  "sessionType": "SCENIC_SERVICE"
}
```

字段说明：

| 字段名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | `number` | 是 | 当前用户 ID |
| `scenicAreaId` | `number` | 是 | 要绑定的景区 ID |
| `scenicAreaSource` | `string` | 否 | 景区来源，建议传 `USER_CONFIRMED` 或 `FRONTEND` |
| `scenicAreaConfirmed` | `number` | 否 | 是否确认，通常传 `1` |
| `sessionType` | `string` | 否 | 会话类型，通常传 `SCENIC_SERVICE` |

### 5.4 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": 12
}
```

说明：

- `data` 返回的是当天会话的主键 ID

### 5.5 失败响应

```json
{
  "code": 500,
  "msg": "景区ID不能为空",
  "data": null
}
```

或：

```json
{
  "code": 500,
  "msg": "当前日期暂无可绑定景区的会话",
  "data": null
}
```

### 5.6 前端使用建议

推荐使用场景：

1. 用户在聊天过程中明确确认景区后调用
2. 用户从景区详情页直接进入聊天，可首轮前就调用，也可以首轮消息直接带 `scenicAreaId`

如果你已经在 `/agent/chat` 请求里传了明确的 `scenicAreaId`，通常不一定要单独再调这个接口；只有当用户后续才确认景区，或者想把之前的会话切换成景区服务模式时，再调用此接口更合适。

---

## 6. 推荐前端调用顺序

### 6.1 普通聊天场景

1. 用户输入问题
2. 前端调用 `POST /agent/chat`
3. 渲染 `data.answer`
4. 如果 `data.needScenicAreaConfirm = true`，可展示“是否是某个景区”的继续追问

### 6.2 用户确认景区后的场景

1. 用户明确说出景区
2. 前端调用 `POST /agent/session/scenic-area/bind`
3. 之后继续调用 `POST /agent/chat`
4. 后续聊天会按景区服务模式进行

### 6.3 查看当天总结

1. 在会话结束、离开页面或用户主动点击“生成总结”时
2. 前端调用 `POST /agent/session-analysis`
3. 展示分析结果

---

## 7. 前端状态管理建议

建议前端在聊天页缓存以下状态：

```ts
interface ChatState {
  sessionId: number | null
  sessionCode: string | null
  reportDate: string | null
  scenicAreaId: number | null
  scenicAreaConfirmed: number
  sessionType: 'CONSULTATION' | 'SCENIC_SERVICE' | null
}
```

建议每轮聊天后更新：

1. `sessionId`
2. `sessionCode`
3. `reportDate`
4. `scenicAreaId`
5. `scenicAreaConfirmed`
6. `sessionType`

---

## 8. axios 调用示例

```ts
import request from '@/utils/request'

export const agentChatApi = (data: {
  userId: number
  content?: string
  messageType?: string
  voiceText?: string | null
  scenicAreaId?: number | null
  scenicAreaSource?: string | null
  scenicAreaConfirmed?: number
  sourceType?: string | null
  sourceId?: string | null
}) => request.post('/agent/chat', data)

export const analyzeSessionApi = (data: {
  userId: number
  reportDate?: string | null
}) => request.post('/agent/session-analysis', data)

export const bindSessionScenicAreaApi = (data: {
  userId: number
  scenicAreaId: number
  scenicAreaSource?: string
  scenicAreaConfirmed?: number
  sessionType?: string
}) => request.post('/agent/session/scenic-area/bind', data)
```

---

## 9. 联调注意事项

1. 聊天接口会自动创建当天会话，不需要前端先创建会话。
2. 当前会话唯一规则是：同一用户同一天只有一段会话。
3. 如果前端已经知道景区，建议在聊天请求中直接传 `scenicAreaId`。
4. Agent 返回的候选景区只是建议，不代表后端已绑定。
5. 真正的景区绑定结果以 `scenicAreaId` 和 `scenicAreaConfirmed` 为准。
6. 分析接口返回的列表字段为数组，不是 JSON 字符串。
7. 前端如果要展示“今日总结”，建议在用户结束聊天后再调用分析接口。

---

## 10. 适合交给前端 Codex 的开发说明

可以直接把下面这段交给前端 Codex：

```text
请基于 docs/agent-chat-api.md 开发聊天页面与会话分析功能。

要求：
1. 使用 Vue3 + Element Plus
2. 使用 axios 封装请求
3. 聊天页面调用 /agent/chat
4. 支持文本提问
5. 支持景区确认提示
6. 支持在需要时调用 /agent/session/scenic-area/bind
7. 支持查看当天会话分析结果，调用 /agent/session-analysis
8. 所有接口统一按 code === 200 判断成功
9. 失败时直接展示后端返回的 msg
```
