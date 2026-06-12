<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <el-icon :size="48" color="var(--steam-light-blue)"><Platform /></el-icon>
        <h1>登录 STEAM</h1>
        <p>登录您的账户，畅享游戏世界</p>
      </div>
      
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading"
            style="width: 100%"
            @click="handleSubmit"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="auth-footer">
        <p>还没有账户？<router-link to="/register">立即注册</router-link></p>
      </div>
      
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

async function handleSubmit() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await userStore.login(form.username, form.password)
      ElMessage.success('登录成功')
      
      // 加载购物车
      await cartStore.fetchCart()
      
      // 跳转
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } catch (error) {
      // 错误已在拦截器处理
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 40px;
  border: 1px solid var(--border-color);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
  
  h1 {
    font-size: 24px;
    color: var(--text-white);
    margin: 16px 0 8px;
  }
  
  p {
    color: var(--text-secondary);
    font-size: 14px;
  }
}

.auth-footer {
  text-align: center;
  margin-top: 24px;
  
  p {
    color: var(--text-secondary);
    font-size: 14px;
    
    a {
      color: var(--steam-light-blue);
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
}

.test-account {
  margin-top: 24px;
  padding: 16px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: var(--radius-md);
  
  p {
    color: var(--text-secondary);
    font-size: 13px;
    margin: 4px 0;
    text-align: center;
  }
}

:deep(.el-divider__text) {
  background: var(--bg-card);
  color: var(--text-secondary);
}
</style>
