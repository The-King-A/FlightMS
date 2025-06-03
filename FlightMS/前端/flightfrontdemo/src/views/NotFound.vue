<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <img src="../assets/airplane.svg" class="not-found-icon" alt="404" />
      <h1 class="not-found-title">404</h1>
      <h2 class="not-found-subtitle">页面未找到</h2>
      <p class="not-found-text">抱歉，您访问的页面不存在或已被移除</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
}

.not-found-content {
  text-align: center;
  padding: 40px;
  max-width: 500px;
}

.not-found-icon {
  width: 120px;
  height: 120px;
  opacity: 0.6;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

.not-found-title {
  font-size: 72px;
  font-weight: 700;
  color: #409EFF;
  margin: 0;
  line-height: 1;
}

.not-found-subtitle {
  font-size: 28px;
  font-weight: 500;
  color: #303133;
  margin: 10px 0;
}

.not-found-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-15px);
  }
  100% {
    transform: translateY(0px);
  }
}
</style> 