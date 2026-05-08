# wanlv-back 地图业务接口联调文档

本文档用于前端基于 MapLibre GL JS + Turf.js 对接后端地图业务接口。
当前后端已补齐以下业务能力：

1. 景区管理
2. 景点管理
3. 路线管理
4. 路线几何数据管理
5. 景区空间要素管理
6. 地图初始化接口
7. 景点详情接口
8. 路线详情接口
9. 地图交互日志记录

---

## 1. 基础信息

- 项目名称：`wanlv-back`
- 本地默认地址：`http://127.0.0.1:8080`
- 接口前缀：`/map`
- 请求格式：`application/json`
- 响应格式：`application/json`
- 当前已接入 JWT 登录态校验；公开读接口和 Agent 工具白名单以 `docs/jwt-auth-api.md` 为准，管理类接口需要管理员 token
- 前端仍然必须优先根据响应体中的 `code` 判断成功或失败

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

## 2. axios 调用建议

推荐前端统一封装：

```ts
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://127.0.0.1:8080',
  timeout: 15000,
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

---

## 3. 数据模型

### 3.1 景区对象 `ScenicAreaVO`

```ts
export interface ScenicAreaVO {
  id: number
  scenicName: string
  scenicCode: string | null
  province: string | null
  city: string | null
  district: string | null
  address: string | null
  longitude: number | null
  latitude: number | null
  description: string | null
  openingHours: string | null
  contactPhone: string | null
  coverImageUrl: string | null
  mapBaseImageUrl: string | null
  mapCenterLng: number | null
  mapCenterLat: number | null
  defaultZoom: number | null
  minZoom: number | null
  maxZoom: number | null
  mapBoundsJson: string | null
  status: number
  createTime: string | null
  updateTime: string | null
}
```

说明：

- `mapBaseImageUrl`：前端自定义底图地址
- `mapCenterLng` / `mapCenterLat`：地图初始化中心点
- `defaultZoom` / `minZoom` / `maxZoom`：MapLibre 初始化缩放参数
- `mapBoundsJson`：地图边界，建议前端按 JSON 字符串再解析

### 3.2 景点对象 `ScenicSpotVO`

```ts
export interface ScenicSpotVO {
  id: number
  scenicAreaId: number
  spotName: string
  poiType: string | null
  iconType: string | null
  spotCode: string | null
  shortIntro: string | null
  description: string | null
  longitude: number | null
  latitude: number | null
  stayDurationMinutes: number | null
  openingHours: string | null
  coverImageUrl: string | null
  audioUrl: string | null
  videoUrl: string | null
  knowledgeDocId: number | null
  recommendedLevel: number | null
  sortNo: number | null
  status: number
  createTime: string | null
  updateTime: string | null
}
```

说明：

- `poiType`：点位类型，常见值如 `SCENIC_SPOT`、`TOILET`、`ENTRANCE`、`PARKING`、`SERVICE`
- `iconType`：前端图标映射类型
- `recommendedLevel`：推荐等级，`0` 普通，`1` 推荐，`2` 重点推荐

### 3.3 路线对象 `TourRouteVO`

```ts
export interface TourRouteVO {
  id: number
  scenicAreaId: number
  routeName: string
  routeType: string | null
  suitableCrowd: string | null
  durationMinutes: number | null
  distanceMeters: number | null
  description: string | null
  recommendedReason: string | null
  status: number
  createTime: string | null
  updateTime: string | null
}
```

### 3.4 路线几何对象 `TourRouteGeoVO`

```ts
export interface TourRouteGeoVO {
  id: number
  routeId: number
  scenicAreaId: number
  geojson: string
  version: number
  status: number
  createTime: string | null
  updateTime: string | null
}
```

说明：
- `scenicAreaId` 是路线轨迹冗余的所属景区 ID，后端会根据 `routeId` 对应路线的 `scenicAreaId` 写入。
- 新增/更新轨迹时前端可以传 `scenicAreaId` 用于校验；如果传入值和路线所属景区不一致，后端会返回失败。
- 地图初始化和路线详情只会使用与路线所属景区一致的轨迹数据。

### 3.5 空间要素对象 `ScenicGeoFeatureVO`

```ts
export interface ScenicGeoFeatureVO {
  id: number
  scenicAreaId: number
  featureName: string
  featureType: string
  geometryType: string
  featureSubType: string | null
  lengthMeters: number | null
  propertiesJson: string | null
  geojson: string
  status: number
  deleted: number
  createTime: string | null
  updateTime: string | null
}
```

说明：

- `featureType` 常见值：`BOUNDARY`、`ZONE`、`RESTRICTED`、`ENTRANCE_AREA`、`ROAD`
- `geometryType` 常见值：`POINT`、`LINE`、`POLYGON`
- 区域类要素使用 `Polygon` / `MultiPolygon`
- 道路类要素 `ROAD` 使用 `LineString` / `MultiLineString`

### 3.6 地图初始化对象 `MapInitVO`

```ts
export interface MapRouteVO {
  id: number
  scenicAreaId: number
  routeName: string
  routeType: string | null
  suitableCrowd: string | null
  durationMinutes: number | null
  distanceMeters: number | null
  description: string | null
  recommendedReason: string | null
  geojson: string | null
  geoVersion: number | null
}

export interface MapInitVO {
  scenicArea: ScenicAreaVO
  geoFeatures: ScenicGeoFeatureVO[]
  spots: ScenicSpotVO[]
  routes: MapRouteVO[]
}
```

### 3.7 路线详情对象 `RouteDetailVO`

```ts
export interface RouteSpotDetailVO {
  relationId: number
  routeId: number
  spotId: number
  sortNo: number | null
  stayDurationMinutes: number | null
  isMustVisit: number
  remark: string | null
  spotName: string
  poiType: string | null
  iconType: string | null
  spotCode: string | null
  shortIntro: string | null
  description: string | null
  coverImageUrl: string | null
  audioUrl: string | null
  videoUrl: string | null
  knowledgeDocId: number | null
  recommendedLevel: number | null
  longitude: number | null
  latitude: number | null
}

export interface RouteDetailVO {
  route: TourRouteVO
  routeGeo: TourRouteGeoVO | null
  spots: RouteSpotDetailVO[]
}
```

说明：

- `routeGeo` 取当前路线最新启用版本的几何数据
- `spots` 已按 `sortNo` 排序，可直接用于前端路线详情展示

---

## 4. 接口清单

### 4.1 景区管理

- `POST /map/scenic-areas`
- `PUT /map/scenic-areas`
- `GET /map/scenic-areas/page`
- `GET /map/scenic-areas/{id}`
- `DELETE /map/scenic-areas/{id}`

### 4.2 景点管理

- `POST /map/spots`
- `PUT /map/spots`
- `GET /map/spots/page`
- `GET /map/spots/{id}`
- `DELETE /map/spots/{id}`

### 4.3 路线管理

- `POST /map/routes`
- `PUT /map/routes`
- `GET /map/routes/page`
- `GET /map/routes/{id}`
- `DELETE /map/routes/{id}`

### 4.4 路线几何管理

- `POST /map/route-geos`
- `PUT /map/route-geos`
- `GET /map/route-geos/route/{routeId}`

### 4.5 景区空间要素管理

- `POST /map/geo-features`
- `PUT /map/geo-features`
- `GET /map/geo-features?scenicAreaId=...`
- `DELETE /map/geo-features/{id}`

### 4.6 地图初始化

- `GET /map/init/{scenicAreaId}`

### 4.7 交互日志

- `POST /map/interaction-logs`

---

## 5. 接口详情

## 5.1 新增景区

- 路径：`POST /map/scenic-areas`
- 用途：新增景区基础信息和地图配置

请求示例：

```json
{
  "scenicName": "西湖景区",
  "scenicCode": "XIHU001",
  "province": "浙江省",
  "city": "杭州市",
  "district": "西湖区",
  "address": "浙江省杭州市西湖区龙井路1号",
  "longitude": 120.155161,
  "latitude": 30.236581,
  "description": "杭州核心景区",
  "openingHours": "全天开放",
  "contactPhone": "0571-12345678",
  "coverImageUrl": "https://example.com/scenic/xihu-cover.jpg",
  "mapBaseImageUrl": "https://example.com/map/xihu.png",
  "mapCenterLng": 120.155161,
  "mapCenterLat": 30.236581,
  "defaultZoom": 15.00,
  "minZoom": 12.00,
  "maxZoom": 20.00,
  "mapBoundsJson": "{\"west\":120.10,\"south\":30.20,\"east\":120.20,\"north\":30.28}",
  "status": 1
}
```

成功响应：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "scenicName": "西湖景区",
    "mapBaseImageUrl": "https://example.com/map/xihu.png",
    "mapCenterLng": 120.155161,
    "mapCenterLat": 30.236581,
    "defaultZoom": 15.00,
    "minZoom": 12.00,
    "maxZoom": 20.00,
    "status": 1
  }
}
```

常见失败：

- `景区名称不能为空`

## 5.2 更新景区

- 路径：`PUT /map/scenic-areas`
- 用途：更新景区基础信息或地图配置

请求示例：

```json
{
  "id": 1,
  "mapBaseImageUrl": "https://example.com/map/xihu-v2.png",
  "defaultZoom": 16.00,
  "minZoom": 13.00,
  "maxZoom": 20.00
}
```

## 5.3 景区分页

- 路径：`GET /map/scenic-areas/page?pageNum=1&pageSize=10&scenicName=西湖&status=1`
- 用途：景区管理页分页查询

响应 `data` 为：

```json
{
  "total": 1,
  "records": [
    {
      "id": 1,
      "scenicName": "西湖景区",
      "status": 1
    }
  ]
}
```

## 5.4 景区详情

- 路径：`GET /map/scenic-areas/{id}`
- 用途：获取单个景区完整配置

---

## 5.5 新增景点

- 路径：`POST /map/spots`
- 用途：新增景点点位

请求示例：

```json
{
  "scenicAreaId": 1,
  "spotName": "雷峰塔",
  "poiType": "SCENIC_SPOT",
  "iconType": "tower",
  "spotCode": "LEIFENGT",
  "shortIntro": "西湖标志性景点",
  "description": "雷峰塔景点详细介绍",
  "longitude": 120.148721,
  "latitude": 30.231066,
  "stayDurationMinutes": 40,
  "openingHours": "08:00-17:30",
  "coverImageUrl": "https://example.com/spot/leifengta.jpg",
  "audioUrl": "https://example.com/audio/leifengta.mp3",
  "videoUrl": "https://example.com/video/leifengta.mp4",
  "knowledgeDocId": 101,
  "recommendedLevel": 2,
  "sortNo": 1,
  "status": 1
}
```

常见失败：

- `景点参数不能为空`
- `所属景区ID不能为空`
- `景点名称不能为空`

## 5.6 更新景点

- 路径：`PUT /map/spots`
- 用途：更新景点信息、点位、素材地址

## 5.7 景点分页

- 路径：`GET /map/spots/page?pageNum=1&pageSize=10&scenicAreaId=1&spotName=雷峰&status=1`
- 用途：景点管理页分页查询

## 5.8 景点详情

- 路径：`GET /map/spots/{id}`
- 用途：前台景点详情页获取单个景点信息

成功响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 11,
    "scenicAreaId": 1,
    "spotName": "雷峰塔",
    "poiType": "SCENIC_SPOT",
    "iconType": "tower",
    "description": "雷峰塔景点详细介绍",
    "longitude": 120.148721,
    "latitude": 30.231066,
    "audioUrl": "https://example.com/audio/leifengta.mp3",
    "videoUrl": "https://example.com/video/leifengta.mp4",
    "knowledgeDocId": 101,
    "recommendedLevel": 2
  }
}
```

说明：

- 只返回启用状态的景点
- 如果景点已停用或不存在，后端返回业务失败

---

## 5.9 新增路线

- 路径：`POST /map/routes`
- 用途：新增路线基础信息，并可同时保存路线景点关联

请求示例：

```json
{
  "scenicAreaId": 1,
  "routeName": "经典半日游",
  "routeType": "official",
  "suitableCrowd": "家庭游客、首次来访游客",
  "durationMinutes": 240,
  "distanceMeters": 3500,
  "description": "适合半天游览的经典路线",
  "recommendedReason": "景点密集，体验完整",
  "status": 1,
  "routeSpots": [
    {
      "spotId": 11,
      "sortNo": 1,
      "stayDurationMinutes": 40,
      "isMustVisit": 1,
      "remark": "优先推荐"
    },
    {
      "spotId": 12,
      "sortNo": 2,
      "stayDurationMinutes": 30,
      "isMustVisit": 0,
      "remark": "可拍照打卡"
    }
  ]
}
```

常见失败：

- `路线参数不能为空`
- `所属景区ID不能为空`
- `路线名称不能为空`
- `路线景点中的景点ID不能为空`
- `路线景点必须属于同一景区`

## 5.10 更新路线

- 路径：`PUT /map/routes`
- 用途：更新路线基础信息；若携带 `routeSpots`，会整条重建路线景点关联

## 5.11 路线分页

- 路径：`GET /map/routes/page?pageNum=1&pageSize=10&scenicAreaId=1&routeName=经典&status=1`
- 用途：路线管理页分页查询

## 5.12 路线详情

- 路径：`GET /map/routes/{id}`
- 用途：返回路线基础信息、最新启用几何数据、路线景点详情列表

成功响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "route": {
      "id": 21,
      "scenicAreaId": 1,
      "routeName": "经典半日游",
      "routeType": "official",
      "durationMinutes": 240,
      "distanceMeters": 3500
    },
    "routeGeo": {
      "id": 31,
      "routeId": 21,
      "scenicAreaId": 1,
      "geojson": "{\"type\":\"LineString\",\"coordinates\":[[120.1,30.2],[120.2,30.3]]}",
      "version": 2,
      "status": 1
    },
    "spots": [
      {
        "relationId": 41,
        "routeId": 21,
        "spotId": 11,
        "sortNo": 1,
        "isMustVisit": 1,
        "spotName": "雷峰塔",
        "iconType": "tower",
        "longitude": 120.148721,
        "latitude": 30.231066
      }
    ]
  }
}
```

说明：

- `routeGeo` 可能为 `null`，前端要兼容“路线存在但几何尚未录入”的情况

---

## 5.13 新增路线几何数据

- 路径：`POST /map/route-geos`
- 用途：新增路线 GeoJSON 数据

请求示例：

```json
{
  "routeId": 21,
  "scenicAreaId": 1,
  "geojson": "{\"type\":\"LineString\",\"coordinates\":[[120.1,30.2],[120.2,30.3]]}",
  "version": 1,
  "status": 1
}
```

说明：

- 如果不传 `version`，后端会自动按当前最大版本号递增
- `scenicAreaId` 建议前端传入当前页面景区 ID，后端会校验它必须和 `routeId` 对应路线的所属景区一致；最终入库值以后端查询到的路线所属景区为准。

## 5.14 更新路线几何数据

- 路径：`PUT /map/route-geos`
- 用途：更新指定版本的路线几何数据

请求示例：

```json
{
  "id": 8,
  "routeId": 21,
  "scenicAreaId": 1,
  "geojson": "{\"type\":\"LineString\",\"coordinates\":[[120.1,30.2],[120.2,30.3]]}",
  "version": 2,
  "status": 1
}
```

说明：

- `routeId` 可不传；不传时沿用原轨迹绑定的路线。
- 如果传 `routeId`，后端会按新路线重新同步 `scenicAreaId`。
- 如果传 `scenicAreaId`，必须和最终路线所属景区一致。

## 5.15 路线几何列表

- 路径：`GET /map/route-geos/route/{routeId}`
- 用途：查看某条路线的所有版本几何数据

---

## 5.16 新增景区空间要素

- 路径：`POST /map/geo-features`
- 用途：新增景区边界、分区、限制区域、道路等 GeoJSON 要素

请求示例：

```json
{
  "scenicAreaId": 1,
  "featureName": "景区边界",
  "featureType": "BOUNDARY",
  "geometryType": "POLYGON",
  "geojson": "{\"type\":\"Polygon\",\"coordinates\":[[[120.1,30.2],[120.2,30.2],[120.2,30.3],[120.1,30.2]]]}",
  "status": 1,
  "deleted": 0
}
```

道路要素示例：

```json
{
  "scenicAreaId": 1,
  "featureName": "湖边步道A段",
  "featureType": "ROAD",
  "geometryType": "LINE",
  "featureSubType": "WALK",
  "lengthMeters": 128,
  "propertiesJson": "{\"bidirectional\":true,\"passable\":true}",
  "geojson": "{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[113.976425,30.125581],[113.9771,30.1262]]},\"properties\":{\"roadType\":\"WALK\"}}",
  "status": 1,
  "deleted": 0
}
```

## 5.17 更新景区空间要素

- 路径：`PUT /map/geo-features`

## 5.18 空间要素列表

- 路径：`GET /map/geo-features?scenicAreaId=1`
- 用途：管理端查看景区所有空间要素

---

## 5.19 地图初始化

- 路径：`GET /map/init/{scenicAreaId}`
- 用途：地图页初始化时一次拿到景区地图配置、空间要素、景点点位、路线列表

这是前端地图页最核心的接口，建议页面进入后优先调用。

成功响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "scenicArea": {
      "id": 1,
      "scenicName": "西湖景区",
      "mapBaseImageUrl": "https://example.com/map/xihu.png",
      "mapCenterLng": 120.155161,
      "mapCenterLat": 30.236581,
      "defaultZoom": 15.00,
      "minZoom": 12.00,
      "maxZoom": 20.00,
      "mapBoundsJson": "{\"west\":120.10,\"south\":30.20,\"east\":120.20,\"north\":30.28}"
    },
    "geoFeatures": [
      {
        "id": 101,
        "featureName": "景区边界",
        "featureType": "BOUNDARY",
        "geojson": "{\"type\":\"Polygon\"}"
      }
    ],
    "spots": [
      {
        "id": 11,
        "spotName": "雷峰塔",
        "poiType": "SCENIC_SPOT",
        "iconType": "tower",
        "longitude": 120.148721,
        "latitude": 30.231066,
        "recommendedLevel": 2
      }
    ],
    "routes": [
      {
        "id": 21,
        "routeName": "经典半日游",
        "routeType": "official",
        "geojson": "{\"type\":\"LineString\"}",
        "geoVersion": 2
      }
    ]
  }
}
```

前端使用建议：

1. 用 `scenicArea` 初始化地图中心点、缩放级别、底图地址
2. 用 `geoFeatures` 渲染边界和功能分区
3. 用 `spots` 生成点位图层和 marker
4. 用 `routes` 生成路线图层，若 `geojson` 为空则前端隐藏该路线轨迹

---

## 5.20 交互日志记录

- 路径：`POST /map/interaction-logs`
- 用途：记录地图交互行为，如点击景点、查看路线、播放讲解、推荐路线

请求示例：

```json
{
  "userId": 10001,
  "sessionId": "session-map-001",
  "scenicAreaId": 1,
  "spotId": 11,
  "routeId": null,
  "actionType": "CLICK_SPOT",
  "actionSource": "MAP",
  "agentResultJson": "{\"intent\":\"view_spot\",\"confidence\":0.93}",
  "remark": "用户点击地图上的雷峰塔"
}
```

成功响应：

```json
{
  "code": 200,
  "msg": "success",
  "data": 501
}
```

说明：

- `data` 返回日志主键 ID
- `actionType` 为必填
- `actionSource` 推荐值：`MAP`、`AGENT`、`SYSTEM`

常见失败：

- `交互日志参数不能为空`
- `景区ID不能为空`
- `操作类型不能为空`

---

## 6. 前端联调建议

### 6.1 地图页初始化顺序

推荐页面初始化流程：

1. 调用 `GET /map/init/{scenicAreaId}`
2. 渲染底图和地图视野
3. 渲染景区空间要素图层
4. 渲染景点 marker / symbol layer
5. 渲染路线折线图层
6. 用户点击景点时调用 `GET /map/spots/{id}`
7. 用户点击路线时调用 `GET /map/routes/{id}`
8. 用户每次关键交互后调用 `POST /map/interaction-logs`

### 6.2 MapLibre GL JS 使用建议

- `mapBaseImageUrl` 可作为图片源或自定义 raster source
- `mapBoundsJson` 建议前端 `JSON.parse` 后再转成 `fitBounds` 参数
- `spots` 建议根据 `iconType` 选择不同 sprite 或 marker 图标
- `routes.geojson` 可以直接作为 `GeoJSONSource` 数据

### 6.3 Turf.js 使用建议

- 前端可用 `routes.geojson` 直接计算路线长度、裁剪、缓冲区
- 可结合 `spots` 与 `routeGeo.geojson` 做“距离最近景点”或“路线吸附点位”处理
- `geoFeatures` 中的 `BOUNDARY` / `RESTRICTED` 可以用于前端做区域判断

---

## 7. 推荐前端接口封装

```ts
import request from '@/utils/request'

export const createScenicAreaApi = (data: any) =>
  request.post('/map/scenic-areas', data)

export const updateScenicAreaApi = (data: any) =>
  request.put('/map/scenic-areas', data)

export const pageScenicAreasApi = (params: {
  pageNum?: number
  pageSize?: number
  scenicName?: string
  status?: number
}) => request.get('/map/scenic-areas/page', { params })

export const createSpotApi = (data: any) =>
  request.post('/map/spots', data)

export const updateSpotApi = (data: any) =>
  request.put('/map/spots', data)

export const pageSpotsApi = (params: {
  pageNum?: number
  pageSize?: number
  scenicAreaId?: number
  spotName?: string
  status?: number
}) => request.get('/map/spots/page', { params })

export const getSpotDetailApi = (id: number) =>
  request.get(`/map/spots/${id}`)

export const createRouteApi = (data: any) =>
  request.post('/map/routes', data)

export const updateRouteApi = (data: any) =>
  request.put('/map/routes', data)

export const pageRoutesApi = (params: {
  pageNum?: number
  pageSize?: number
  scenicAreaId?: number
  routeName?: string
  status?: number
}) => request.get('/map/routes/page', { params })

export const getRouteDetailApi = (id: number) =>
  request.get(`/map/routes/${id}`)

export interface RouteGeoPayload {
  id?: number
  routeId?: number
  scenicAreaId?: number
  geojson?: string
  version?: number
  status?: number
}

export const createRouteGeoApi = (data: RouteGeoPayload) =>
  request.post('/map/route-geos', data)

export const updateRouteGeoApi = (data: RouteGeoPayload) =>
  request.put('/map/route-geos', data)

export const listRouteGeosApi = (routeId: number) =>
  request.get(`/map/route-geos/route/${routeId}`)

export const createGeoFeatureApi = (data: any) =>
  request.post('/map/geo-features', data)

export const updateGeoFeatureApi = (data: any) =>
  request.put('/map/geo-features', data)

export const listGeoFeaturesApi = (scenicAreaId: number) =>
  request.get('/map/geo-features', { params: { scenicAreaId } })

export const getMapInitApi = (scenicAreaId: number) =>
  request.get(`/map/init/${scenicAreaId}`)

export const createInteractionLogApi = (data: any) =>
  request.post('/map/interaction-logs', data)
```

---

## 8. 当前注意事项

- 当前管理接口未做权限隔离，联调时前端自行区分“管理端”和“游客端”
- 删除接口已支持景区、景点、路线、空间要素的逻辑删除；删除景区会同步逻辑删除其下景点、路线、空间要素，并禁用路线轨迹
- `mapBoundsJson`、`geojson`、`agentResultJson` 这些字段本质上都是字符串，前端需要自行 `JSON.parse`
- 路线详情接口中的 `routeGeo` 可能为空，前端不能假设每条路线都已录入轨迹
- 路线轨迹对象 `TourRouteGeoVO` 已包含 `scenicAreaId`；前端新增/更新轨迹时建议带上当前景区 ID，后端会做一致性校验
- 路线更新时如果传了 `routeSpots`，后端会按新的数组整体覆盖旧的路线景点关系

---

## 9. 适合直接交给前端 Codex 的任务说明

```text
请基于 docs/map-api.md 开发景区地图页面和管理页面，使用 MapLibre GL JS + Turf.js + Vue3 + Element Plus。

优先完成：
1. 地图初始化页，调用 GET /map/init/{scenicAreaId}
2. 景点点击详情弹窗，调用 GET /map/spots/{id}
3. 路线点击详情弹窗，调用 GET /map/routes/{id}
4. 地图交互日志记录，调用 POST /map/interaction-logs
5. 景区、景点、路线后台管理页，分别对接分页、新增、更新接口

要求：
1. 所有请求统一使用 axios 封装
2. 所有接口都按响应体 code === 200 判断成功
3. geojson、mapBoundsJson 在前端自行解析
4. 页面结构要同时兼顾地图展示和后台数据维护
5. 失败提示直接显示后端 msg
```
