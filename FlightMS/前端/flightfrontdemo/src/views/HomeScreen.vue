<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1 class="welcome-title">欢迎使用航班管理系统</h1>
      <p class="welcome-subtitle">高效管理航班信息，优化航班调度</p>
    </div>

    <el-row :gutter="20" class="feature-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><DataAnalysis /></el-icon>
              <span>数据概览</span>
            </div>
          </template>
          <div class="card-content">
            <div class="stat-item">
              <span class="stat-label">航班总数</span>
              <span class="stat-value">{{ flightStats.total }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">今日航班</span>
              <span class="stat-value">{{ flightStats.today }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">城市覆盖</span>
              <span class="stat-value">{{ flightStats.cities }}</span>
            </div>
            <el-button type="primary" @click="goToDashboard" class="card-button">
              查看详情
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><Tickets /></el-icon>
              <span>航班管理</span>
            </div>
          </template>
          <div class="card-content">
            <p class="card-description">
              快速查询、添加、编辑和删除航班信息，支持多条件筛选和批量操作。
            </p>
            <el-button type="primary" @click="goToFlights" class="card-button">
              管理航班
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><PieChart /></el-icon>
              <span>统计分析</span>
            </div>
          </template>
          <div class="card-content">
            <p class="card-description">
              查看航班统计数据，包括航线分布、座位利用率等多维度分析图表。
            </p>
            <el-button type="primary" @click="goToStatistics" class="card-button">
              查看统计
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <div class="quick-actions">
      <h2 class="section-title">快捷操作</h2>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-button type="success" @click="goToAddFlight" class="action-button">
            <el-icon><Plus /></el-icon>
            添加航班
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="goToFlights" class="action-button">
            <el-icon><Search /></el-icon>
            查询航班
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="warning" @click="goToDashboard" class="action-button">
            <el-icon><DataAnalysis /></el-icon>
            查看仪表盘
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="info" @click="goToStatistics" class="action-button">
            <el-icon><PieChart /></el-icon>
            数据分析
          </el-button>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { DataAnalysis, Tickets, PieChart, ArrowRight, Plus, Search } from '@element-plus/icons-vue'
import api from '../services/api'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 航班统计数据
const flightStats = ref({
  total: 0,
  today: 0,
  cities: 0
})

// 获取航班统计数据
const fetchFlightStats = async () => {
  try {
    // 获取所有航班
    const flights = await api.flights.getAllWithCityNames()
    
    // 计算统计数据
    flightStats.value.total = flights.length
    
    // 计算今日航班数量
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    
    flightStats.value.today = flights.filter(flight => {
      const flightDate = new Date(flight.flydate)
      flightDate.setHours(0, 0, 0, 0)
      return flightDate.getTime() === today.getTime()
    }).length
    
    // 获取城市数量
    const cities = await api.cities.getAll()
    flightStats.value.cities = cities.length
  } catch (error) {
    console.error('获取航班统计数据失败:', error)
    ElMessage.error('获取航班统计数据失败')
  }
}

// 页面导航
const goToDashboard = () => router.push('/dashboard')
const goToFlights = () => router.push('/flights')
const goToStatistics = () => router.push('/statistics')
const goToAddFlight = () => router.push('/flights/add')

onMounted(() => {
  fetchFlightStats()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-section {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 0;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border-radius: 8px;
  color: white;
}

.welcome-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 10px;
}

.welcome-subtitle {
  font-size: 18px;
  opacity: 0.9;
}

.feature-cards {
  margin-bottom: 40px;
}

.feature-card {
  height: 100%;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 500;
}

.header-icon {
  margin-right: 8px;
  font-size: 20px;
}

.card-content {
  display: flex;
  flex-direction: column;
  height: 200px;
}

.card-description {
  flex-grow: 1;
  color: #606266;
  line-height: 1.6;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #eee;
}

.stat-label {
  color: #606266;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #409EFF;
}

.card-button {
  margin-top: auto;
}

.section-title {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 20px;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
}

.quick-actions {
  margin-top: 40px;
}

.action-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
}

.action-button .el-icon {
  margin-right: 5px;
  font-size: 18px;
}
</style> 