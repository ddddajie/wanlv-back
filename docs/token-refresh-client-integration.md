# Web 与 Android Token 无感刷新快速接入

本文档用于 Web 和 Android 客户端快速接入普通用户登录态刷新。接口的完整字段定义见 [jwt-auth-api.md](./jwt-auth-api.md)。

## 1. 接入流程

```text
登录成功
  -> 保存 token + refreshToken
  -> 业务请求携带 Authorization: Bearer <token>
  -> 业务请求返回 HTTP 401
  -> 用 refreshToken 刷新（并发时只允许一个刷新请求）
     -> 成功：同时保存新 token 和新 refreshToken，重放原请求
     -> 失败：清理登录态，跳转登录页
```

三个必须遵守的规则：

1. 刷新成功后必须同时替换两个 Token，旧 refreshToken 已作废。
2. 多个请求同时返回 `401` 时必须合并为一次刷新，否则只有第一个刷新请求能成功。
3. 刷新接口自身返回 `401` 或原请求重试后仍返回 `401` 时，不能继续刷新。

## 2. 公共数据结构

```ts
export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

export interface TokenPair {
  token: string
  refreshToken: string
  expireSeconds: number
  refreshExpireSeconds: number
}

export interface NormalUserLoginVO extends TokenPair {
  id: number
  username: string
  phone?: string | null
  displayName: string
  userType: 'normal'
  role: 'normal_user'
  status: number
  realNameStatus?: number | null
  lastLoginTime?: string | null
}
```

## 3. Web 快速接入（Axios）

### 3.1 Token 存储

下面示例沿用当前项目的 `localStorage` 方案。不要把 Token 输出到控制台、埋点或错误上报中。

```ts
// src/utils/token-store.ts
import type { TokenPair } from '@/api/token'

const ACCESS_TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'

export const tokenStore = {
  getAccessToken: () => localStorage.getItem(ACCESS_TOKEN_KEY) || '',
  getRefreshToken: () => localStorage.getItem(REFRESH_TOKEN_KEY) || '',
  save(pair: TokenPair) {
    localStorage.setItem(ACCESS_TOKEN_KEY, pair.token)
    localStorage.setItem(REFRESH_TOKEN_KEY, pair.refreshToken)
  },
  clear() {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem('userInfo')
  },
}
```

### 3.2 接口封装

```ts
// src/api/token.ts
import refreshRequest from '@/utils/refresh-request'

export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

export interface TokenPair {
  token: string
  refreshToken: string
  expireSeconds: number
  refreshExpireSeconds: number
}

export const refreshTokenApi = (refreshToken: string) =>
  refreshRequest.post<any, ApiResponse<TokenPair>>('/user/normal/token/refresh', { refreshToken })
```

退出接口可以继续放在普通用户接口文件中：

```ts
// src/api/user.ts
import request from '@/utils/request'
import type { ApiResponse } from './token'

export const logoutApi = (refreshToken: string) =>
  request.post<any, ApiResponse<null>>('/user/normal/logout', { refreshToken })
```

刷新请求使用不带业务响应拦截器的独立实例，避免递归刷新：

```ts
// src/utils/refresh-request.ts
import axios from 'axios'

const refreshRequest = axios.create({
  baseURL: 'http://127.0.0.1:8080',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

refreshRequest.interceptors.response.use((response) => response.data)

export default refreshRequest
```

### 3.3 Axios 单飞刷新与请求重放

```ts
// src/utils/request.ts
import axios, { type InternalAxiosRequestConfig } from 'axios'
import { refreshTokenApi, type TokenPair } from '@/api/token'
import { tokenStore } from './token-store'

interface RetryConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

const request = axios.create({
  baseURL: 'http://127.0.0.1:8080',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

let refreshing: Promise<TokenPair> | null = null

export function clearAndGoLogin() {
  tokenStore.clear()
  if (window.location.pathname !== '/login') {
    window.location.replace('/login')
  }
}

request.interceptors.request.use((config) => {
  const token = tokenStore.getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body.code !== 200) {
      return Promise.reject(body)
    }
    return body
  },
  async (error) => {
    const original = error.config as RetryConfig | undefined
    const refreshToken = tokenStore.getRefreshToken()

    if (error.response?.status !== 401 || !original || original._retry || !refreshToken) {
      if (error.response?.status === 401) clearAndGoLogin()
      return Promise.reject(error)
    }

    original._retry = true
    const requestAuthorization = original.headers?.Authorization
    const latestAccessToken = tokenStore.getAccessToken()
    if (latestAccessToken && requestAuthorization !== `Bearer ${latestAccessToken}`) {
      // 当前请求使用的是旧 Token，说明另一个请求已经刷新完成，直接重放即可。
      return request(original)
    }

    try {
      // 重点：并发 401 共用同一个刷新 Promise，避免旧 refreshToken 被重复提交。
      refreshing ??= refreshTokenApi(refreshToken).then((res) => {
        if (res.code !== 200) return Promise.reject(res)
        tokenStore.save(res.data)
        return res.data
      }).finally(() => {
        refreshing = null
      })

      await refreshing
      return request(original)
    } catch (refreshError) {
      clearAndGoLogin()
      return Promise.reject(refreshError)
    }
  },
)

export default request
```

如果项目已有 Pinia，请在 `tokenStore.save(...)` 和 `clearAndGoLogin()` 中同步更新 Store，保证页面状态和持久化状态一致。

### 3.4 登录与退出

```ts
// 登录成功
const res = await normalPhoneCodeLoginApi(form)
tokenStore.save(res.data)
localStorage.setItem('userInfo', JSON.stringify(res.data))

// 退出时先保留 refreshToken，调用完成后再清理本地状态
export async function logout() {
  const refreshToken = tokenStore.getRefreshToken()
  try {
    if (refreshToken) await logoutApi(refreshToken)
  } finally {
    clearAndGoLogin()
  }
}
```

## 4. Android 快速接入（Retrofit + OkHttp）

### 4.1 数据结构和接口

```kotlin
data class ApiResponse<T>(
    val code: Int,
    val msg: String,
    val data: T?
)

data class TokenPair(
    val token: String,
    val refreshToken: String,
    val expireSeconds: Long,
    val refreshExpireSeconds: Long
)

data class RefreshTokenRequest(val refreshToken: String)

interface TokenApi {
    @POST("user/normal/token/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): retrofit2.Response<ApiResponse<TokenPair>>

    @POST("user/normal/logout")
    suspend fun logout(@Body body: RefreshTokenRequest): ApiResponse<Any?>
}
```

登录接口返回的 `token` 和 `refreshToken` 应保存到由 Android Keystore 保护的安全存储中。下面用接口隔离具体存储实现：

```kotlin
interface TokenStore {
    fun accessToken(): String?
    fun refreshToken(): String?
    fun save(pair: TokenPair)
    fun clear()
}
```

### 4.2 请求携带 accessToken

```kotlin
class AccessTokenInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.accessToken()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                header("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}
```

### 4.3 401 自动刷新

`TokenApi` 必须使用一个没有配置该 `Authenticator` 的独立 OkHttpClient，否则刷新接口返回 `401` 时会递归调用自己。

```kotlin
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val refreshApi: TokenApi,
    private val onLoginExpired: () -> Unit
) : Authenticator {

    private val refreshLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        synchronized(refreshLock) {
            val requestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
            val latestToken = tokenStore.accessToken()

            // 其他请求已经完成刷新时，直接使用最新 accessToken 重试。
            if (!latestToken.isNullOrBlank() && latestToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latestToken")
                    .build()
            }

            val refreshToken = tokenStore.refreshToken() ?: return loginExpired()
            val refreshResponse = runBlocking {
                refreshApi.refresh(RefreshTokenRequest(refreshToken))
            }
            val pair = refreshResponse.body()?.data

            if (!refreshResponse.isSuccessful || refreshResponse.body()?.code != 200 || pair == null) {
                return loginExpired()
            }

            // 重点：必须保存服务端轮换后的新 refreshToken。
            tokenStore.save(pair)
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${pair.token}")
                .build()
        }
    }

    private fun loginExpired(): Request? {
        tokenStore.clear()
        onLoginExpired()
        return null
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
```

业务 OkHttpClient：

```kotlin
val businessClient = OkHttpClient.Builder()
    .addInterceptor(AccessTokenInterceptor(tokenStore))
    .authenticator(TokenAuthenticator(tokenStore, refreshApi) {
        // 切回主线程后跳转登录页或发送全局登录过期事件。
    })
    .build()
```

### 4.4 Android 退出

```kotlin
suspend fun logout(tokenApi: TokenApi, tokenStore: TokenStore) {
    val refreshToken = tokenStore.refreshToken()
    try {
        if (!refreshToken.isNullOrBlank()) {
            tokenApi.logout(RefreshTokenRequest(refreshToken))
        }
    } finally {
        tokenStore.clear()
    }
}
```

## 5. 联调检查清单

- 两种普通用户登录方式都能获得 `token`、`refreshToken`、`7200`、`259200`。
- 普通业务请求携带 accessToken 后可以正常访问。
- 手动使用错误 accessToken 时，只产生一个刷新请求，原业务请求随后成功重放。
- 刷新成功后，本地 refreshToken 已替换，旧 refreshToken 再调用刷新接口返回 HTTP 401。
- 多个并发业务请求同时返回 401 时，不会并发调用多次刷新接口。
- refreshToken 无效或过期时会清理本地状态并进入登录页，不出现刷新死循环。
- 退出当前设备后，该 refreshToken 无法再次刷新，但其他设备仍保持登录。
- 客户端日志、网络埋点和崩溃上报中没有明文 Token。
