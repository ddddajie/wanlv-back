# 会话日报总结接口文档

本文档用于说明本次新增的“按日期生成聊天日报总结”能力，包括：

1. 单个用户日报总结接口
2. 按日期批量日报总结接口
3. 定时任务自动生成日报总结
4. 数据表关系与处理逻辑

适用项目：`wanlv-back`  
默认服务地址：`http://127.0.0.1:8080`

---

## 1. 功能概述

本次功能的目标是：

1. 根据 `visitor_session.report_date` 找到某一天的会话
2. 使用 `visitor_session.id = visitor_message.session_id` 关联会话消息
3. 将该会话当天的聊天内容发送给 Agent 做日报总结
4. 将总结结果回写到 `visitor_session`
5. 支持超级管理员手动调用
6. 支持系统在每天结束时自动批量生成当天日报

---

## 2. 数据关系说明

本功能依赖以下两张表：

### 2.1 `visitor_session`

核心字段：

| 字段名 | 说明 |
| --- | --- |
| `id` | 会话主键 ID |
| `user_id` | 普通用户 ID |
| `session_code` | 发给 Agent 的业务会话编码 |
| `report_date` | 该会话所属的日报日期 |
| `session_status` | 会话状态，日报生成后会更新为 `ANALYZED` |
| `interaction_count` | 用户发言次数 |
| `summary` | 日报总结摘要 |
| `overall_sentiment` | 整体情感倾向 |
| `sentiment_score` | 情感分值 |
| `focus_topics` | 关注主题，JSON 字符串 |
| `interest_tags` | 兴趣标签，JSON 字符串 |
| `service_suggestions` | 服务建议，JSON 字符串 |
| `knowledge_gap_points` | 知识缺口，JSON 字符串 |

### 2.2 `visitor_message`

核心字段：

| 字段名 | 说明 |
| --- | --- |
| `id` | 消息主键 ID |
| `session_id` | 关联的会话主键，对应 `visitor_session.id` |
| `user_id` | 用户 ID |
| `message_no` | 消息序号 |
| `sender_type` | 发送方，通常为 `visitor` 或 `agent` |
| `message_type` | 消息类型 |
| `content` | 文本内容 |
| `voice_text` | 语音转写内容 |
| `create_time` | 消息创建时间 |

### 2.3 关系说明

注意：

1. `visitor_message.session_id` 存的是 `visitor_session.id`
2. 不是 `visitor_session.session_code`
3. 日报总结时先按 `report_date` 找会话，再按会话 `id` 找消息

即：

```text
visitor_session.report_date -> 找到当天会话
visitor_session.id -> visitor_message.session_id -> 找到当天会话下的全部消息
```

---

## 3. 权限说明

当前项目暂未接入统一登录拦截器或 token 鉴权，因此本次接口采用“显式传超级管理员账号密码”的方式做权限校验。

只有 `sys_admin_user.role = super_admin` 的管理员可以调用以下两个日报接口：

1. `POST /agent/session-analysis`
2. `POST /agent/session-analysis/daily`

请求体中必须传：

| 字段名 | 是否必填 | 说明 |
| --- | --- | --- |
| `operatorUsername` | 是 | 超级管理员账号 |
| `operatorPassword` | 是 | 超级管理员密码 |

---

## 4. 公共请求参数

两个日报接口都使用同一个请求 DTO：`SessionAnalysisTriggerDTO`

字段如下：

| 字段名 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | `number` | 单会话接口必填 | 要生成日报的普通用户 ID |
| `reportDate` | `string` | 否 | 报表日期，格式 `yyyy-MM-dd`，为空时默认当天 |
| `operatorUsername` | `string` | 是 | 超级管理员账号 |
| `operatorPassword` | `string` | 是 | 超级管理员密码 |
| `forceReanalyze` | `boolean` | 否 | 是否强制重新生成日报，默认 `false` |

`forceReanalyze` 行为说明：

1. `false`：如果会话已经有日报结果，则直接跳过
2. `true`：即使已经有日报结果，也会重新调用 Agent 覆盖原结果

---

## 5. 单个用户日报总结接口

### 5.1 接口地址

`POST /agent/session-analysis`

完整示例：

`http://127.0.0.1:8080/agent/session-analysis`

### 5.2 接口说明

该接口用于生成“某个普通用户在某一天的聊天日报总结”。

后端处理流程：

1. 校验超级管理员账号密码
2. 校验 `userId`
3. 根据 `userId + reportDate` 查询 `visitor_session`
4. 根据 `visitor_session.id` 查询 `visitor_message`
5. 调用 Agent 分析接口
6. 将结果回写到 `visitor_session`
7. 返回日报分析结果

### 5.3 请求示例

```json
{
  "operatorUsername": "admin",
  "operatorPassword": "123456",
  "userId": 10001,
  "reportDate": "2026-04-14",
  "forceReanalyze": false
}
```

### 5.4 成功响应

响应 `data` 对应 `AgentSessionAnalysisVO`

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "summary": "用户当天主要围绕景区出行、票务与路线安排进行咨询。",
    "overallSentiment": "positive",
    "sentimentScore": 88.5,
    "focusTopics": [
      "景区门票",
      "出行路线",
      "老年人游玩"
    ],
    "interestTags": [
      "慢游",
      "家庭出行"
    ],
    "serviceSuggestions": [
      "补充门票优惠说明",
      "补充老年游客路线推荐"
    ],
    "knowledgeGapPoints": [
      "尚未确认具体出行日期"
    ]
  }
}
```

### 5.5 返回字段说明

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `summary` | `string` | 当天会话总结 |
| `overallSentiment` | `string` | 整体情感倾向 |
| `sentimentScore` | `number` | 情感分值 |
| `focusTopics` | `string[]` | 关注话题 |
| `interestTags` | `string[]` | 兴趣标签 |
| `serviceSuggestions` | `string[]` | 服务建议 |
| `knowledgeGapPoints` | `string[]` | 知识缺口 |

### 5.6 失败场景

可能返回：

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
  "msg": "当前日期暂无可分析的会话",
  "data": null
}
```

或：

```json
{
  "code": 500,
  "msg": "超级管理员账号或密码错误",
  "data": null
}
```

---

## 6. 按日期批量日报总结接口

### 6.1 接口地址

`POST /agent/session-analysis/daily`

完整示例：

`http://127.0.0.1:8080/agent/session-analysis/daily`

### 6.2 接口说明

该接口用于生成“某一天所有会话的日报总结”。

后端处理流程：

1. 校验超级管理员账号密码
2. 根据 `reportDate` 查询当天全部 `visitor_session`
3. 遍历每个会话
4. 根据 `visitor_session.id` 查询该会话下全部消息
5. 调用 Agent 分析
6. 将结果回写到 `visitor_session`
7. 返回批量执行结果

### 6.3 请求示例

```json
{
  "operatorUsername": "admin",
  "operatorPassword": "123456",
  "reportDate": "2026-04-14",
  "forceReanalyze": false
}
```

### 6.4 成功响应

响应 `data` 对应 `SessionAnalysisBatchVO`

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "reportDate": "2026-04-14",
    "totalCount": 3,
    "successCount": 2,
    "skippedCount": 1,
    "failedCount": 0,
    "items": [
      {
        "sessionId": 12,
        "sessionCode": "session_20260414_10001",
        "userId": 10001,
        "success": true,
        "skipped": false,
        "message": "日报总结生成成功",
        "summary": "用户当天主要咨询景区门票和路线安排。"
      },
      {
        "sessionId": 13,
        "sessionCode": "session_20260414_10002",
        "userId": 10002,
        "success": true,
        "skipped": true,
        "message": "当前会话已生成日报总结，已跳过",
        "summary": "用户主要咨询亲子出游相关问题。"
      },
      {
        "sessionId": 14,
        "sessionCode": "session_20260414_10003",
        "userId": 10003,
        "success": true,
        "skipped": false,
        "message": "日报总结生成成功",
        "summary": "用户关注园区餐饮和路线换乘。"
      }
    ]
  }
}
```

### 6.5 返回字段说明

#### 顶层字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `reportDate` | `string` | 本次生成日报的日期 |
| `totalCount` | `number` | 当天命中的会话总数 |
| `successCount` | `number` | 成功生成日报的会话数 |
| `skippedCount` | `number` | 已生成日报因此跳过的会话数 |
| `failedCount` | `number` | 生成失败的会话数 |
| `items` | `array` | 每个会话的执行明细 |

#### `items` 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `sessionId` | `number` | 会话主键 ID |
| `sessionCode` | `string` | 业务会话编码 |
| `userId` | `number` | 用户 ID |
| `success` | `boolean` | 本次是否成功 |
| `skipped` | `boolean` | 是否被跳过 |
| `message` | `string` | 执行说明 |
| `summary` | `string` | 本次日报摘要，失败时可能为空 |

### 6.6 跳过规则

当满足以下条件时，默认判定会话已经生成过日报：

1. `visitor_session.session_status = ANALYZED`
2. `visitor_session.summary` 不为空

如果满足以上条件且 `forceReanalyze = false`，该会话会被跳过。

---

## 7. 定时任务说明

### 7.1 定时任务类

定时任务实现类：

`src/main/java/com/example/wanlvback/task/SessionAnalysisScheduleTask.java`

### 7.2 执行时间

默认配置：

```yaml
wanlv:
  agent:
    daily-analysis-cron: "59 59 23 * * ?"
    daily-analysis-zone: Asia/Shanghai
```

即：

1. 每天 `23:59:59`
2. 按 `Asia/Shanghai` 时区执行

### 7.3 定时任务行为

定时任务会：

1. 获取当天日期
2. 调用 `chatService.analyzeDailySessions(reportDate, false)`
3. 批量生成当天全部会话的日报总结
4. 跳过已经生成过日报的会话

### 7.4 定时任务与接口的关系

定时任务与手动接口复用同一套 service 逻辑：

1. 接口走 `analyzeDailySessions(SessionAnalysisTriggerDTO triggerDTO)`
2. 定时任务走 `analyzeDailySessions(LocalDate reportDate, boolean forceReanalyze)`

这样做的好处是：

1. 避免接口和定时任务逻辑分叉
2. 便于后续统一维护
3. 手动补跑和自动跑结果一致

---

## 8. 日报结果回写说明

Agent 返回日报结果后，会将以下字段写回 `visitor_session`：

| 字段名 | 回写内容 |
| --- | --- |
| `session_status` | `ANALYZED` |
| `interaction_count` | 用户发言次数 |
| `summary` | 总结摘要 |
| `overall_sentiment` | 整体情感 |
| `sentiment_score` | 情感分数 |
| `focus_topics` | JSON 字符串 |
| `interest_tags` | JSON 字符串 |
| `service_suggestions` | JSON 字符串 |
| `knowledge_gap_points` | JSON 字符串 |

说明：

1. 列表字段在数据库中按 JSON 字符串存储
2. 接口返回给前端时，会重新解析成数组

---

## 9. 设计说明

### 9.1 为什么按 `report_date` 查会话

因为本次日报总结的业务语义是“按天生成总结”，所以入口应该先根据 `visitor_session.report_date` 找到当天会话。

### 9.2 为什么按 `session_id` 查消息

因为 `visitor_message.session_id` 存的是 `visitor_session.id`，这是数据库主键关系，最稳定。

### 9.3 为什么要保留 `session_code`

`session_code` 是发给 Agent 服务的业务会话编码，用于保持 Agent 侧会话连续性；  
本地数据库关联仍以 `visitor_session.id` 为准。

### 9.4 为什么批量接口返回明细

批量日报生成可能出现：

1. 部分会话成功
2. 部分会话跳过
3. 部分会话失败

因此返回明细更方便管理端排查。

---

## 10. 建议联调方式

推荐按以下顺序联调：

1. 先通过 `/agent/chat` 产生当天聊天记录
2. 调用 `/agent/session-analysis` 测试单用户日报
3. 调用 `/agent/session-analysis/daily` 测试批量日报
4. 最后验证定时任务自动执行效果

---

## 11. 示例接口汇总

### 11.1 单用户日报

```http
POST /agent/session-analysis
Content-Type: application/json
```

```json
{
  "operatorUsername": "admin",
  "operatorPassword": "123456",
  "userId": 10001,
  "reportDate": "2026-04-14",
  "forceReanalyze": false
}
```

### 11.2 批量日报

```http
POST /agent/session-analysis/daily
Content-Type: application/json
```

```json
{
  "operatorUsername": "admin",
  "operatorPassword": "123456",
  "reportDate": "2026-04-14",
  "forceReanalyze": false
}
```

---

## 12. 本次改动涉及文件

核心代码文件：

1. `src/main/java/com/example/wanlvback/controller/ChatController.java`
2. `src/main/java/com/example/wanlvback/service/ChatService.java`
3. `src/main/java/com/example/wanlvback/service/impl/ChatServiceImpl.java`
4. `src/main/java/com/example/wanlvback/mapper/VisitorSessionMapper.java`
5. `src/main/resources/mapper/VisitorSessionMapper.xml`
6. `src/main/java/com/example/wanlvback/pojo/dto/SessionAnalysisTriggerDTO.java`
7. `src/main/java/com/example/wanlvback/pojo/vo/SessionAnalysisBatchVO.java`
8. `src/main/java/com/example/wanlvback/pojo/vo/SessionAnalysisBatchItemVO.java`
9. `src/main/java/com/example/wanlvback/task/SessionAnalysisScheduleTask.java`
10. `src/main/resources/application.yml`

文档文件：

1. `docs/session-daily-analysis-api.md`
