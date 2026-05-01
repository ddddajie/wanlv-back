# Agent 服务接入用户数字画像改造说明

本文档给 Agent 服务端使用，说明后端聊天接口现在会在 `/chat` 请求中携带用户数字画像 `user_profile`，Agent 需要同步接收并在回答时合理利用该字段。

## 1. 背景

后端已经新增用户数字画像表 `user_digital_profile`，并在生成日报总结后自动刷新该用户画像。

当用户再次提问时，后端会查询该用户画像，并在调用 Agent 服务 `/chat` 时追加：

```json
{
  "user_profile": {
    "interestTags": ["历史文化爱好者", "亲子旅游"],
    "focusTopics": ["路线规划", "历史建筑介绍"],
    "serviceNeeds": ["建议补充天气提醒"],
    "knowledgeGaps": ["未确认具体出游日期"],
    "travelStyle": "文化深度游",
    "activityLevel": "中活跃",
    "sentimentTendency": "positive",
    "sentimentScoreAvg": 86.50,
    "profileScore": 78,
    "sourceSessionCount": 3,
    "lastAnalyzedDate": "2026-04-26"
  }
}
```

如果用户暂无画像，后端不会传 `user_profile` 字段。

## 2. `/chat` 请求字段变更

原有字段保持不变，新增可选字段：

| 字段名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `user_profile` | `object` | 否 | 用户数字画像。新用户或暂无画像时可能不存在。 |

完整请求示例：

```json
{
  "query": "帮我推荐一条适合孩子玩的路线",
  "session_id": "session_20260501_1",
  "user_id": 1,
  "report_date": "2026-05-01",
  "scenic_area_id": 1001,
  "user_nickname": "小王",
  "age": 32,
  "gender": "1",
  "message_type": "text",
  "voice_text": null,
  "user_profile": {
    "interestTags": ["亲子旅游", "历史文化爱好者"],
    "focusTopics": ["路线规划", "景区服务"],
    "serviceNeeds": ["需要儿童适配路线"],
    "knowledgeGaps": ["未确认具体出游日期"],
    "travelStyle": "亲子游",
    "activityLevel": "中活跃",
    "sentimentTendency": "positive",
    "sentimentScoreAvg": 86.50,
    "profileScore": 78,
    "sourceSessionCount": 3,
    "lastAnalyzedDate": "2026-04-26"
  }
}
```

## 3. Agent 服务端需要修改的点

### 3.1 请求模型新增字段

如果 Agent 服务使用 FastAPI + Pydantic，请在 `/chat` 请求模型中新增：

```python
from typing import Any, Dict, Optional

class ChatRequest(BaseModel):
    query: str
    session_id: str
    user_id: int
    report_date: str
    scenic_area_id: Optional[int] = None
    user_nickname: Optional[str] = None
    age: Optional[int] = None
    gender: Optional[str] = None
    message_type: Optional[str] = None
    voice_text: Optional[str] = None
    user_profile: Optional[Dict[str, Any]] = None
```

要求：

1. `user_profile` 必须是可选字段。
2. 不能因为缺少 `user_profile` 影响普通聊天。
3. 如果当前 Pydantic 模型禁止额外字段，需要显式加入该字段，否则后端新增字段可能导致请求校验失败。

### 3.2 Prompt 中加入画像上下文

建议在构造系统提示词或用户上下文时加入用户画像，但不要把画像内容原样暴露给用户。

示例：

```text
用户数字画像：
- 兴趣标签：{interestTags}
- 高频关注主题：{focusTopics}
- 服务需求：{serviceNeeds}
- 常见知识缺口：{knowledgeGaps}
- 出游风格：{travelStyle}
- 活跃度：{activityLevel}
- 情绪倾向：{sentimentTendency}

回答要求：
1. 回答时可以参考用户画像进行个性化推荐。
2. 优先结合用户的兴趣标签、关注主题、出游风格和服务需求。
3. 如果画像中存在知识缺口，可以自然追问关键信息。
4. 不要直接告诉用户“根据你的用户画像”或暴露画像内部字段。
5. 画像只作为辅助上下文，不能覆盖用户当前明确表达的需求。
```

### 3.3 空值兼容

建议逻辑：

```python
profile = request.user_profile or {}

if profile:
    # 将画像摘要加入 prompt
else:
    # 不加入画像上下文，按普通聊天处理
```

注意：后端对新用户不会传 `user_profile`，所以 Agent 必须允许该字段不存在。

## 4. 建议使用策略

Agent 回答时建议优先使用以下画像字段：

| 字段 | 推荐用途 |
| --- | --- |
| `interestTags` | 判断用户长期兴趣，例如亲子、历史文化、慢游等 |
| `focusTopics` | 判断用户常问问题，例如路线规划、门票、景区服务 |
| `serviceNeeds` | 主动补充用户可能需要的服务信息 |
| `knowledgeGaps` | 用于自然追问关键缺失信息 |
| `travelStyle` | 调整推荐风格，例如亲子游、文化深度游、路线规划型 |
| `activityLevel` | 高活跃用户可以减少基础解释，低活跃用户可以更详细说明 |
| `sentimentTendency` | 情绪偏负向时回答更耐心、明确，避免生硬拒绝 |

## 5. 不建议的行为

1. 不要直接向用户展示完整 `user_profile`。
2. 不要说“根据你的数字画像/系统画像显示”。
3. 不要在用户当前问题很明确时，强行套用旧画像。
4. 不要把 `knowledgeGaps` 当作事实，只能当作“可能需要补充确认的信息”。
5. 不要因为没有画像就拒绝回答。

## 6. 验收建议

可以用以下场景验证：

1. 无 `user_profile` 请求：Agent 正常回答。
2. 有亲子游画像：用户问路线时，Agent 优先推荐适合儿童、节奏较轻的路线。
3. 有文化深度游画像：用户问景点时，Agent 适当补充历史文化讲解。
4. 有知识缺口：用户没有给日期或人数时，Agent 自然追问，而不是直接报错。
5. 有负向情绪倾向：Agent 回答更简洁、安抚、明确。

## 7. 兼容说明

后端新增字段为 `user_profile`，字段命名使用 snake_case。Agent 服务如果内部使用 camelCase，可以在请求模型里做别名映射，但外部接口字段名应兼容 `user_profile`。
