# Agent 定制路线轨迹接口文档

本文档用于 Agent 服务对接后端地图路线工具接口。聊天主链路保持不变：前端调用后端 `/agent/chat`，后端转发给 Agent 服务；Agent 识别到定制路线意图并生成景点游玩顺序后，再调用本文接口生成并保存用户定制路线轨迹。

## 1. 基础信息

- 接口用途：供 Agent 服务根据用户 ID 和有序景点列表生成定制路线，并保存到 `agent_route_geo`
- 接口前缀：`/map/agent`
- 请求格式：`application/json`
- 响应格式：`application/json`
- 统一响应结构：

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

说明：

- `code = 200` 表示成功
- `code = 500` 表示业务失败或系统异常
- 本接口不创建 `tour_route`，不创建 `tour_route_geo`，不更新官方路线距离
- 本接口会把 Agent 生成的用户定制路线保存到 `agent_route_geo`
- 成功响应只返回 `true`，不会返回 GeoJSON、景点明细或告警明细
- Agent 只需要传景区名称和景点名称顺序，后端负责转换为真实景区 ID 和景点 ID
- 该接口不走 JWT，Agent 必须显式传入 `userId` 用于落库

## 2. Agent 定制路线轨迹生成

```http
POST /map/agent/routes/geo/generate
```

请求参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `userId` | 是 | 所属用户 ID；该接口不走 JWT，必须由 Agent 显式携带 |
| `scenicName` | 是 | 景区名称，后端会匹配已启用景区 |
| `routeName` | 否 | 路线名称；不传时默认为“智能定制路线” |
| `spotNames` | 是 | Agent 已排好序的景点名称数组，至少 2 个；后端按数组顺序生成路线 |
| `roadTypes` | 否 | 道路子类型过滤，例如 `["WALK"]` |
| `snapToleranceMeters` | 否 | 景点吸附到道路的建议容忍距离，默认 80 米 |
| `fallbackStrategy` | 否 | 兜底策略：`DIRECT_SEGMENT` 或 `FAIL`，默认 `DIRECT_SEGMENT` |

请求示例：

```json
{
  "userId": 1,
  "scenicName": "西湖景区",
  "routeName": "亲子慢游路线",
  "spotNames": ["游客中心", "亲子乐园", "湖边步道"],
  "roadTypes": ["WALK"],
  "snapToleranceMeters": 80,
  "fallbackStrategy": "DIRECT_SEGMENT"
}
```

成功响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": true
}
```

保存说明：

- 后端会按 `spotNames` 顺序匹配真实景点，生成路线 GeoJSON。
- 生成结果保存到 `agent_route_geo`，核心字段包括 `user_id`、`scenic_area_id`、`route_name`、`geojson`、`spot_ids_json`、`spot_names_json`、`distance_meters`、`spot_count`、`road_segment_count`、`create_time`。
- 生成过程中的非阻断告警只用于后端判断和日志，不返回给 Agent。

## 3. 前端获取最新专属路线

```http
GET /map/agent-route-geos/latest?userId=1&scenicAreaId=1
```

说明：

- 该接口面向前端，不在 `/map/agent/**` 下，会经过 JWT 拦截器。
- 后端根据 `userId + scenicAreaId` 查询该用户在该景区最新生成的专属路线。
- `userId` 必填；普通用户只能查询自己的路线，管理员可查询任意用户路线。
- `scenicAreaId` 必填；路线属于指定用户在指定景区下的定制结果。
- 如果该用户在该景区还没有生成过专属路线，`data` 返回 `null`。

成功响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 10,
    "userId": 1,
    "scenicAreaId": 1,
    "routeName": "亲子慢游路线",
    "geojson": "{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[113.976425,30.125581],[113.9771,30.1262]]}}",
    "spotIdsJson": "[3,8,12]",
    "spotNamesJson": "[\"游客中心\",\"亲子乐园\",\"湖边步道\"]",
    "distanceMeters": 860.42,
    "spotCount": 3,
    "roadSegmentCount": 12,
    "createTime": "2026-05-08T16:30:00",
    "updateTime": "2026-05-08T16:30:00"
  }
}
```

## 4. Agent 使用建议

1. Agent 先根据用户画像、对话上下文和向量数据库结果确定景区名称与游玩顺序。
2. Agent 必须携带当前用户的 `userId`，否则后端无法保存用户定制路线。
3. 将最终顺序按 `spotNames` 传入本接口，不要依赖接口重新排序。
4. 如果 `fallbackStrategy = FAIL`，道路不连通或景点无法吸附到道路会直接返回失败，适合严格要求只走景区道路的场景。
5. 如果返回“匹配到多个结果”，Agent 应继续向用户澄清更准确的景区名或景点名。

## 5. 常见失败

| 场景 | 返回说明 |
| --- | --- |
| `userId` 为空 | 用户ID不能为空 |
| 查询他人专属路线 | 无权操作其他用户数据 |
| 查询专属路线时 `scenicAreaId` 为空 | 景区ID不能为空 |
| `scenicName` 为空 | 景区名称不能为空 |
| `spotNames` 少于 2 个 | 定制路线至少需要 2 个有效景点 |
| 景区名称无匹配 | 未匹配到启用景区 |
| 景区名称多匹配 | 景区名称匹配到多个结果，请提供更准确的景区名称 |
| 景点名称无匹配 | 未匹配到当前景区下的启用景点 |
| 景点名称多匹配 | 景点名称匹配到多个结果，请提供更准确的景点名称 |
| 景点缺少经纬度 | 路线中存在缺少经纬度的景点 |
| 当前景区没有道路 | 当前景区没有可用于计算的道路 |
