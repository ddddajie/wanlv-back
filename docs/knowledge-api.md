# 知识库管理接口文档

本文档给前端 Codex 接入知识库文档上传和训练功能使用。当前接口由后端转发到 Agent 服务，文件实际保存目录、允许文件类型和向量化训练逻辑以 Agent 的 `config/chroma.yml` 配置为准。

## 1. 基础信息

- 后端默认地址：`http://127.0.0.1:8080`
- 接口前缀：`/knowledge`
- 鉴权方式：需要携带管理员登录后的 JWT
- 权限要求：仅超级管理员可操作

请求头：

```http
Authorization: Bearer <token>
```

重要说明：

- 这两个接口统一使用后端通用 `Result<T>` 响应结构。
- 前端判断成功时优先判断响应体里的 `code === 200`。
- 业务数据在 `data` 字段中。

## 2. 上传知识库文档

### 2.1 接口地址

```http
POST /knowledge/upload
Content-Type: multipart/form-data
```

完整示例：

```http
POST http://127.0.0.1:8080/knowledge/upload
```

### 2.2 功能说明

上传一个或多个知识库文档。后端会将文件转发给 Agent 的 `/knowledge/upload` 接口。

Agent 返回上传成功的 `saved_files` 后，后端会把这些文件写入 `knowledge_document` 表；被 Agent 拒绝的 `rejected_files` 不入库。

### 2.3 表单字段

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `files` | `File[]` | 是 | 知识库文档文件列表。当前默认支持 `txt`、`pdf`，实际以 Agent 的 `config/chroma.yml` 中 `allow_knowledge_file_type` 为准 |
| `scenicAreaId` | `number` | 否 | 关联景区 ID。不传时后端默认写入 `0`，表示全局知识库文档 |

### 2.4 入库字段说明

后端只对 `saved_files` 里的文件入库，字段规则如下：

| 数据库字段 | 取值规则 |
| --- | --- |
| `scenic_area_id` | 表单 `scenicAreaId`；未传时为 `0` |
| `doc_name` | 前端上传的原始文件名去掉最后一个后缀 |
| `file_name` | 前端上传的原始文件名 |
| `file_size` | 上传文件大小 |
| `file_suffix` | 前端上传的原始文件后缀，小写，例如 `txt`、`pdf` |
| `parse_status` | `pending` |
| `publish_status` | `draft` |
| `uploaded_by` | 当前登录管理员 ID |
| `deleted` | `0` |
| `create_time` | 后端当前时间 |

### 2.5 curl 示例

```bash
curl -X POST "http://127.0.0.1:8080/knowledge/upload" \
  -H "Authorization: Bearer <token>" \
  -F "scenicAreaId=1" \
  -F "files=@./data/景区介绍.txt" \
  -F "files=@./data/游览指南.pdf"
```

如果上传全局知识库文档，可以不传 `scenicAreaId`：

```bash
curl -X POST "http://127.0.0.1:8080/knowledge/upload" \
  -H "Authorization: Bearer <token>" \
  -F "files=@./data/景区介绍.txt"
```

### 2.6 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "code": 200,
    "message": "知识库文档上传成功",
    "saved_files": ["景区介绍.txt", "游览指南.pdf"],
    "rejected_files": []
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
外层字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | `number` | 后端统一业务状态码，`200` 表示成功 |
| `msg` | `string` | 后端统一响应消息，成功时通常为 `success` |
| `data` | `object` | 上传结果数据 |

`data` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | `number` | Agent 返回码，`200` 表示成功 |
| `message` | `string` | 上传结果消息 |
| `saved_files` | `string[]` | Agent 已保存且后端已入库的文件名 |
| `rejected_files` | `string[]` | Agent 拒绝的文件名，一般是不支持的文件类型 |

### 2.7 常见失败响应

未登录或 token 无效：

```json
{
  "code": 401,
  "msg": "请先登录"
}
```

非超级管理员：

```json
{
  "code": 500,
  "msg": "仅超级管理员可操作",
  "data": null
}
```

未选择文件：

```json
{
  "code": 500,
  "msg": "请至少上传一个知识库文档",
  "data": null
}
```

上传空文件：

```json
{
  "code": 500,
  "msg": "上传的知识库文档不能为空",
  "data": null
}
```

Agent 服务不可用：

```json
{
  "code": 500,
  "msg": "调用 Agent 知识库文档上传服务失败",
  "data": null
}
```

## 3. 训练知识库

### 3.1 接口地址

```http
POST /knowledge/train
```

完整示例：

```http
POST http://127.0.0.1:8080/knowledge/train
```

### 3.2 功能说明

触发 Agent 处理 `data_path` 目录中的知识库文档，并调用现有向量化逻辑写入 Chroma 向量库。

Agent 侧会使用 `md5.text` 去重，已经处理过且内容未变化的文档会跳过。

### 3.3 请求参数

无请求体。

请求头：

```http
Authorization: Bearer <token>
```

### 3.4 curl 示例

```bash
curl -X POST "http://127.0.0.1:8080/knowledge/train" \
  -H "Authorization: Bearer <token>"
```

### 3.5 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "code": 200,
    "message": "知识库训练完成"
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
外层字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | `number` | 后端统一业务状态码，`200` 表示成功 |
| `msg` | `string` | 后端统一响应消息，成功时通常为 `success` |
| `data` | `object` | 训练结果数据 |

`data` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | `number` | Agent 返回码，`200` 表示成功 |
| `message` | `string` | 训练结果消息 |

### 3.6 常见失败响应

非超级管理员：

```json
{
  "code": 500,
  "msg": "仅超级管理员可操作",
  "data": null
}
```

Agent 服务不可用：

```json
{
  "code": 500,
  "msg": "调用 Agent 知识库训练服务失败",
  "data": null
}
```

## 4. 前端实现建议

### 4.1 TypeScript 类型

```ts
export interface Result<T> {
  code: number
  msg: string
  data: T
}

export interface KnowledgeUploadResponse {
  code: number
  message: string
  saved_files: string[]
  rejected_files: string[]
}

export interface KnowledgeTrainResponse {
  code: number
  message: string
}
```

### 4.2 axios 上传示例

```ts
export async function uploadKnowledgeFiles(files: File[], scenicAreaId?: number) {
  const formData = new FormData()

  if (scenicAreaId !== undefined && scenicAreaId !== null) {
    formData.append('scenicAreaId', String(scenicAreaId))
  }

  files.forEach((file) => {
    formData.append('files', file)
  })

  const response = await request.post<Result<KnowledgeUploadResponse>>(
    '/knowledge/upload',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
  )

  return response.data
}
```

注意：

- 多文件上传时，所有文件都使用同一个字段名 `files`。
- 如果项目已有 axios 拦截器会自动展开 `response.data`，则根据现有封装调整返回值。
- 上传成功后可以展示 `saved_files` 和 `rejected_files`，让管理员知道哪些文件已进入知识库。

### 4.3 axios 训练示例

```ts
export async function trainKnowledge() {
  const response = await request.post<Result<KnowledgeTrainResponse>>('/knowledge/train')
  return response.data
}
```

### 4.4 页面交互建议

- 上传按钮仅允许超级管理员可见或可点击。
- 文件选择器建议限制 `.txt,.pdf`，但最终允许类型以后端和 Agent 返回为准。
- 上传成功后展示：
  - 已保存文件：`saved_files`
  - 被拒绝文件：`rejected_files`
- 训练接口可能耗时较长，前端应展示 loading 状态并避免重复点击。
- 建议把“上传文档”和“训练知识库”做成两个明确动作：先上传，再训练。
