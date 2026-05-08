# 手机验证码登录前端实现参考

本文档给前端实现普通用户手机号验证码登录/自动注册使用。示例基于 Vue 3 + TypeScript + Pinia + Element Plus。

## 1. 接口封装

建议新增或更新 `src/api/user.ts`：

```ts
import request from '@/utils/request'

export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

export interface UserLoginVO {
  id: number
  username: string
  displayName: string
  userType: 'admin' | 'normal'
  role: 'super_admin' | 'admin' | 'normal_user'
  status: number
  realNameStatus: number | null
  token: string
  lastLoginTime: string | null
}

export interface PhoneCodeSendVO {
  phone: string
  code: string
  expireSeconds: number
  expireTime: string
}

export const sendNormalUserPhoneCodeApi = (data: { phone: string }) =>
  request.post<any, ApiResponse<PhoneCodeSendVO>>('/user/normal/code/send', data)

export const normalPhoneCodeLoginApi = (data: { phone: string; code: string }) =>
  request.post<any, ApiResponse<UserLoginVO>>('/user/normal/code/login', data)

export const normalLoginApi = (data: { username: string; password: string }) =>
  request.post<any, ApiResponse<UserLoginVO>>('/user/normal/login', data)
```

## 2. 请求拦截器

建议 `src/utils/request.ts` 统一携带 token：

```ts
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://127.0.0.1:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
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
    if (error?.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.error('请先登录')
      return Promise.reject(error)
    }
    ElMessage.error(error?.message || '网络异常')
    return Promise.reject(error)
  },
)

export default request
```

## 3. Pinia 登录态

建议新增或更新 `src/stores/auth.ts`：

```ts
import { defineStore } from 'pinia'
import type { UserLoginVO } from '@/api/user'

interface AuthState {
  userInfo: UserLoginVO | null
  token: string
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    token: localStorage.getItem('token') || '',
  }),
  getters: {
    isLogin: (state) => Boolean(state.token && state.userInfo),
    isNormalUser: (state) => state.userInfo?.userType === 'normal',
  },
  actions: {
    setLoginInfo(userInfo: UserLoginVO) {
      this.userInfo = userInfo
      this.token = userInfo.token
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      localStorage.setItem('token', userInfo.token)
    },
    logout() {
      this.userInfo = null
      this.token = ''
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
    },
  },
})
```

## 4. 普通用户登录页

建议普通用户默认展示验证码登录，账号密码登录作为备用 tab。示例 `src/views/user/UserLogin.vue`：

```vue
<template>
  <div class="login-page">
    <el-card class="login-panel" shadow="never">
      <h1>用户登录</h1>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="手机号登录" name="phone">
          <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" label-position="top">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="phoneForm.phone" maxlength="11" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="验证码" prop="code">
              <div class="code-row">
                <el-input v-model.trim="phoneForm.code" maxlength="6" placeholder="请输入验证码" />
                <el-button :disabled="countdown > 0" :loading="sending" @click="sendCode">
                  {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-alert
              v-if="devCode"
              :title="`开发联调验证码：${devCode}`"
              type="info"
              show-icon
              :closable="false"
            />

            <el-button class="submit-btn" type="primary" :loading="loggingIn" @click="phoneLogin">
              登录/注册
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="账号密码" name="password">
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
            <el-form-item label="账号" prop="username">
              <el-input v-model.trim="passwordForm.username" placeholder="请输入账号" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="passwordForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-button class="submit-btn" type="primary" :loading="loggingIn" @click="passwordLogin">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  normalLoginApi,
  normalPhoneCodeLoginApi,
  sendNormalUserPhoneCodeApi,
} from '@/api/user'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const phonePattern = /^1[3-9]\d{9}$/

const activeTab = ref<'phone' | 'password'>('phone')
const phoneFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const sending = ref(false)
const loggingIn = ref(false)
const countdown = ref(0)
const devCode = ref('')
let timer: number | undefined

const phoneForm = reactive({
  phone: '',
  code: '',
})

const passwordForm = reactive({
  username: '',
  password: '',
})

const phoneRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phonePattern, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
}

const passwordRules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const startCountdown = (seconds: number) => {
  countdown.value = seconds
  timer = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0 && timer) {
      window.clearInterval(timer)
      timer = undefined
    }
  }, 1000)
}

const sendCode = async () => {
  await phoneFormRef.value?.validateField('phone')
  sending.value = true
  try {
    const res = await sendNormalUserPhoneCodeApi({ phone: phoneForm.phone })
    devCode.value = res.data.code
    phoneForm.code = res.data.code
    startCountdown(res.data.expireSeconds)
    ElMessage.success('验证码已生成')
  } finally {
    sending.value = false
  }
}

const phoneLogin = async () => {
  await phoneFormRef.value?.validate()
  loggingIn.value = true
  try {
    const res = await normalPhoneCodeLoginApi(phoneForm)
    authStore.setLoginInfo(res.data)
    ElMessage.success('登录成功')
    router.replace('/home')
  } finally {
    loggingIn.value = false
  }
}

const passwordLogin = async () => {
  await passwordFormRef.value?.validate()
  loggingIn.value = true
  try {
    const res = await normalLoginApi(passwordForm)
    authStore.setLoginInfo(res.data)
    ElMessage.success('登录成功')
    router.replace('/home')
  } finally {
    loggingIn.value = false
  }
}

onBeforeUnmount(() => {
  if (timer) {
    window.clearInterval(timer)
  }
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #f6f8fb;
}

.login-panel {
  width: min(420px, calc(100vw - 32px));
  border-radius: 8px;
}

h1 {
  margin: 0 0 16px;
  font-size: 24px;
  color: #111827;
}

.code-row {
  display: grid;
  grid-template-columns: 1fr 112px;
  gap: 10px;
}

.submit-btn {
  width: 100%;
  margin-top: 16px;
}
</style>
```

## 5. 联调流程

1. 输入手机号，点击“发送验证码”。
2. 开发阶段后端响应会返回 `data.code`，页面可直接展示或自动填入。
3. 点击“登录/注册”，调用 `/user/normal/code/login`。
4. 新手机号会自动注册并返回 `UserLoginVO`，旧手机号直接登录。
5. 缓存 `data.token`，后续实名、预约、聊天等接口都带 `Authorization`。

## 6. 注意事项

- 验证码 5 分钟有效，登录成功后后端会移除验证码。
- 手机号账号初始没有密码，不能直接走账号密码登录。
- 后续如果前端提供“设置密码”入口，可以复用普通用户更新接口传 `id` 和 `password`。
- 预约前仍以 `realNameStatus === 1` 判断是否已实名认证。
