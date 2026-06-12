<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-brand">
        <div class="logo">
          <el-icon :size="32"><Platform /></el-icon>
          <span>STEAM</span>
        </div>
        <p class="description">全球最大的PC游戏平台，数万款精品游戏等你来玩</p>
      </div>
      
      <div class="footer-links">
        <div class="link-group">
          <h4>快速链接</h4>
          <router-link to="/">首页</router-link>
          <router-link to="/store">商店</router-link>
          <router-link to="/library">游戏库</router-link>
        </div>
        
        <div class="link-group">
          <h4>用户中心</h4>
          <router-link to="/cart">购物车</router-link>
          <router-link to="/wishlist">愿望单</router-link>
          <router-link to="/orders">订单记录</router-link>
        </div>
        
        <div class="link-group">
          <h4>帮助支持</h4>
          <a href="#">客服中心</a>
          <a href="#">常见问题</a>
          <a href="#">退款政策</a>
        </div>
      </div>
    </div>
    
    <div class="footer-bottom">
      <p>&copy; {{ new Date().getFullYear() }} Steam游戏商城 - 仅供学习参考</p>
    </div>
  </footer>
</template>

<script setup lang="ts">
</script>

<style lang="scss" scoped>
.app-footer {
  background: var(--steam-darker);
  margin-top: auto;
  border-top: 1px solid var(--border-color);
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 40px;
}

.footer-brand {
  .logo {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--text-white);
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 16px;
  }
  
  .description {
    color: var(--text-secondary);
    line-height: 1.6;
  }
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  
  .link-group {
    h4 {
      color: var(--text-white);
      font-size: 16px;
      margin-bottom: 16px;
    }
    
    a {
      display: block;
      color: var(--text-secondary);
      text-decoration: none;
      padding: 6px 0;
      transition: color 0.3s;
      
      &:hover {
        color: var(--steam-light-blue);
      }
    }
  }
}

.footer-bottom {
  border-top: 1px solid var(--border-color);
  padding: 20px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 32px;
    padding: 32px 16px;
  }
  
  .footer-links {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .footer-links {
    grid-template-columns: 1fr;
  }
}
</style>
