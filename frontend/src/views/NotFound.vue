<template>
  <div class="not-found-page">
    <div class="content">
      <h1>404</h1>
      <p>抱歉，您访问的页面不存在</p>
      <router-link to="/">
        <el-button type="primary" size="large">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style lang="scss" scoped>
.not-found-page {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.content {
  text-align: center;
  
  h1 {
    font-size: 120px;
    font-weight: 700;
    color: var(--steam-light-blue);
    margin-bottom: 16px;
    text-shadow: 0 0 40px rgba(102, 192, 244, 0.3);
  }
  
  p {
    font-size: 18px;
    color: var(--text-secondary);
    margin-bottom: 32px;
  }
}
</style>
