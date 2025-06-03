<template>
  <div class="main-layout">
    <el-container>
      <el-aside width="220px">
        <el-menu
          :router="true"
          :default-active="activeMenu"
          class="el-menu-vertical"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#409EFF">
          
          <div class="logo-container">
            <img src="../assets/airplane.svg" class="logo-icon" alt="Logo" />
            <h2 class="logo-text">航班管理系统</h2>
          </div>
          
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>控制台</span>
          </el-menu-item>
          
          <el-menu-item index="/flights">
            <el-icon><Tickets /></el-icon>
            <span>航班列表</span>
          </el-menu-item>
          
          <el-menu-item index="/statistics">
            <el-icon><PieChart /></el-icon>
            <span>统计分析</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container class="right-container">
        <el-header height="60px">
          <div class="header-content">
            <el-breadcrumb separator="/" class="breadcrumb">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
                {{ item }}
              </el-breadcrumb-item>
            </el-breadcrumb>
            
            <div class="user-menu">
              <el-dropdown trigger="click" @command="handleCommand">
                <span class="el-dropdown-link">
                  <el-avatar :size="32" icon="User" />
                  {{ userStore.currentUser?.userid || '用户' }}
                  <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                    <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                    <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, Tickets, DataAnalysis, PieChart } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const breadcrumbs = computed(() => {
  const path = route.path
  const matched = route.matched.filter(item => item.path !== '/')
  return matched.map(item => item.name || item.path.replace('/', ''))
})

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
    }).catch(() => {})
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.logo-container {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 16px;
  color: #fff;
  background-color: #001529;
}

.logo-icon {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.logo-text {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.el-menu {
  border-right: none;
  height: calc(100vh - 64px);
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
}

.header-content {
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: center;
}

.user-menu {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
}

.el-avatar {
  margin-right: 8px;
  background-color: #409EFF;
}

.right-container {
  overflow: hidden;
}

.el-main {
  background-color: #f5f7fa;
  height: calc(100vh - 60px);
  overflow-y: auto;
  padding: 20px;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style> 