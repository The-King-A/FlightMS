<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="dashboard-header">
          <h1 class="dashboard-title">航班数据控制台</h1>
          <div class="header-actions">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :shortcuts="dateShortcuts"
              @change="handleDateChange"
            />
            <el-button type="primary" @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 数据概览卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <el-icon class="stat-icon flight-icon"><Tickets /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.totalFlights }}</div>
              <div class="stat-label">航班总数</div>
            </div>
          </div>
          <div class="stat-trend" :class="{'up': dashboardData.flightTrend > 0, 'down': dashboardData.flightTrend < 0}">
            <el-icon v-if="dashboardData.flightTrend > 0"><ArrowUp /></el-icon>
            <el-icon v-else-if="dashboardData.flightTrend < 0"><ArrowDown /></el-icon>
            <span>{{ Math.abs(dashboardData.flightTrend) }}%</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <el-icon class="stat-icon city-icon"><Location /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.totalCities }}</div>
              <div class="stat-label">覆盖城市</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <el-icon class="stat-icon seat-icon"><SuitcaseLine /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.totalSeats }}</div>
              <div class="stat-label">总座位数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <el-icon class="stat-icon today-icon"><Calendar /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.todayFlights }}</div>
              <div class="stat-label">今日航班</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>航班趋势</span>
              <el-radio-group v-model="trendTimeUnit" size="small" @change="updateFlightTrendChart">
                <el-radio-button label="day">日</el-radio-button>
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="flightTrendChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>航班分布</span>
            </div>
          </template>
          <div ref="flightDistributionChart" class="chart-container pie-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热门城市</span>
            </div>
          </template>
          <div ref="topCitiesChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>座位容量分布</span>
            </div>
          </template>
          <div ref="seatCapacityChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { Tickets, Location, SuitcaseLine, Calendar, Refresh, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import api from '../services/api'

// 日期范围快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    },
  },
]

// 图表实例
let flightTrendChartInstance = null
let flightDistributionChartInstance = null
let topCitiesChartInstance = null
let seatCapacityChartInstance = null

// 响应式数据
const dateRange = ref([new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000), new Date()])
const trendTimeUnit = ref('day')
const flightTrendChart = ref(null)
const flightDistributionChart = ref(null)
const topCitiesChart = ref(null)
const seatCapacityChart = ref(null)

// 仪表盘数据
const dashboardData = reactive({
  totalFlights: 0,
  totalCities: 0,
  totalSeats: 0,
  todayFlights: 0,
  flightTrend: 5, // 假设增长了5%
  flightTrendData: [],
  flightDistributionData: [],
  topCitiesData: [],
  seatCapacityData: []
})

// 获取仪表盘数据
const fetchDashboardData = async () => {
  try {
    // 获取航班数据
    const flights = await api.flights.getAllWithCityNames()
    
    // 获取城市数据
    const cities = await api.cities.getAll()
    
    // 更新仪表盘数据
    dashboardData.totalFlights = flights.length
    dashboardData.totalCities = cities.length
    
    // 计算总座位数
    dashboardData.totalSeats = flights.reduce((total, flight) => total + flight.seatnum, 0)
    
    // 计算今日航班数量
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    
    dashboardData.todayFlights = flights.filter(flight => {
      const flightDate = new Date(flight.flydate)
      flightDate.setHours(0, 0, 0, 0)
      return flightDate.getTime() === today.getTime()
    }).length
    
    // 处理航班趋势数据
    processFlightTrendData(flights)
    
    // 处理航班分布数据
    processFlightDistributionData(flights, cities)
    
    // 处理热门城市数据
    processTopCitiesData(flights, cities)
    
    // 处理座位容量分布数据
    processSeatCapacityData(flights)
    
    // 更新所有图表
    updateAllCharts()
    
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    ElMessage.error('获取仪表盘数据失败')
  }
}

// 处理航班趋势数据
const processFlightTrendData = (flights) => {
  // 按日期分组航班
  const flightsByDate = {}
  
  flights.forEach(flight => {
    const date = new Date(flight.flydate)
    date.setHours(0, 0, 0, 0)
    const dateStr = date.toISOString().split('T')[0]
    
    if (!flightsByDate[dateStr]) {
      flightsByDate[dateStr] = []
    }
    
    flightsByDate[dateStr].push(flight)
  })
  
  // 生成日期范围内的所有日期
  const start = new Date(dateRange.value[0])
  const end = new Date(dateRange.value[1])
  const dateList = []
  const countList = []
  
  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    const dateStr = d.toISOString().split('T')[0]
    dateList.push(dateStr)
    countList.push(flightsByDate[dateStr]?.length || 0)
  }
  
  dashboardData.flightTrendData = {
    dates: dateList,
    counts: countList
  }
}

// 处理航班分布数据
const processFlightDistributionData = (flights, cities) => {
  // 创建城市ID到名称的映射
  const cityMap = {}
  cities.forEach(city => {
    cityMap[city.cityid] = city.cityname
  })
  
  // 按起始城市对统计航班
  const routeCount = {}
  
  flights.forEach(flight => {
    const startCity = cityMap[flight.startcity] || '未知'
    const endCity = cityMap[flight.endcity] || '未知'
    const route = `${startCity} → ${endCity}`
    
    routeCount[route] = (routeCount[route] || 0) + 1
  })
  
  // 转换为图表数据格式
  const distributionData = Object.entries(routeCount)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 10) // 只取前10条航线
  
  dashboardData.flightDistributionData = distributionData
}

// 处理热门城市数据
const processTopCitiesData = (flights, cities) => {
  // 创建城市ID到名称的映射
  const cityMap = {}
  cities.forEach(city => {
    cityMap[city.cityid] = city.cityname
  })
  
  // 统计每个城市的航班数量（出发和到达）
  const cityCount = {}
  
  flights.forEach(flight => {
    const startCity = cityMap[flight.startcity] || '未知'
    const endCity = cityMap[flight.endcity] || '未知'
    
    cityCount[startCity] = (cityCount[startCity] || 0) + 1
    cityCount[endCity] = (cityCount[endCity] || 0) + 1
  })
  
  // 转换为图表数据格式并排序
  const topCities = Object.entries(cityCount)
    .map(([city, count]) => ({ city, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10) // 只取前10个城市
  
  dashboardData.topCitiesData = topCities
}

// 处理座位容量分布数据
const processSeatCapacityData = (flights) => {
  // 按座位数量范围分组
  const seatRanges = {
    '小型 (< 100)': 0,
    '中型 (100-200)': 0,
    '大型 (> 200)': 0
  }
  
  flights.forEach(flight => {
    const seatNum = flight.seatnum
    
    if (seatNum < 100) {
      seatRanges['小型 (< 100)']++
    } else if (seatNum <= 200) {
      seatRanges['中型 (100-200)']++
    } else {
      seatRanges['大型 (> 200)']++
    }
  })
  
  // 转换为图表数据格式
  dashboardData.seatCapacityData = Object.entries(seatRanges)
    .map(([name, value]) => ({ name, value }))
}

// 更新航班趋势图表
const updateFlightTrendChart = () => {
  if (!flightTrendChartInstance) return
  
  // 根据时间单位处理数据
  let dates = [...dashboardData.flightTrendData.dates]
  let counts = [...dashboardData.flightTrendData.counts]
  
  if (trendTimeUnit.value === 'week' || trendTimeUnit.value === 'month') {
    const aggregated = {}
    
    dashboardData.flightTrendData.dates.forEach((date, index) => {
      let key
      
      if (trendTimeUnit.value === 'week') {
        // 按周聚合
        const d = new Date(date)
        const weekStart = new Date(d)
        weekStart.setDate(d.getDate() - d.getDay())
        key = weekStart.toISOString().split('T')[0]
      } else {
        // 按月聚合
        key = date.substring(0, 7)
      }
      
      if (!aggregated[key]) {
        aggregated[key] = 0
      }
      
      aggregated[key] += dashboardData.flightTrendData.counts[index]
    })
    
    dates = Object.keys(aggregated).sort()
    counts = dates.map(date => aggregated[date])
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '航班数量'
    },
    series: [
      {
        name: '航班数量',
        type: 'line',
        smooth: true,
        data: counts,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(64, 158, 255, 0.5)'
              },
              {
                offset: 1,
                color: 'rgba(64, 158, 255, 0.1)'
              }
            ]
          }
        }
      }
    ]
  }
  
  flightTrendChartInstance.setOption(option)
}

// 更新航班分布图表
const updateFlightDistributionChart = () => {
  if (!flightDistributionChartInstance) return
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      top: 'center',
      type: 'scroll',
      formatter: (name) => {
        // 限制名称长度
        if (name.length > 15) {
          return name.substring(0, 12) + '...'
        }
        return name
      }
    },
    series: [
      {
        name: '航线分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '14',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: dashboardData.flightDistributionData
      }
    ]
  }
  
  flightDistributionChartInstance.setOption(option)
}

// 更新热门城市图表
const updateTopCitiesChart = () => {
  if (!topCitiesChartInstance) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '航班数量'
    },
    yAxis: {
      type: 'category',
      data: dashboardData.topCitiesData.map(item => item.city),
      axisLabel: {
        formatter: (value) => {
          if (value.length > 10) {
            return value.substring(0, 8) + '...'
          }
          return value
        }
      }
    },
    series: [
      {
        name: '航班数量',
        type: 'bar',
        data: dashboardData.topCitiesData.map(item => item.count),
        itemStyle: {
          color: function(params) {
            // 颜色渐变
            const colorList = ['#83bff6', '#188df0', '#005eaa', '#0a2d66']
            const index = params.dataIndex % colorList.length
            return colorList[index]
          }
        }
      }
    ]
  }
  
  topCitiesChartInstance.setOption(option)
}

// 更新座位容量分布图表
const updateSeatCapacityChart = () => {
  if (!seatCapacityChartInstance) return
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '0%',
      left: 'center'
    },
    series: [
      {
        name: '座位容量',
        type: 'pie',
        radius: ['30%', '60%'],
        center: ['50%', '40%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}: {c} ({d}%)'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '14',
            fontWeight: 'bold'
          }
        },
        data: dashboardData.seatCapacityData,
        color: ['#67C23A', '#E6A23C', '#F56C6C']
      }
    ]
  }
  
  seatCapacityChartInstance.setOption(option)
}

// 更新所有图表
const updateAllCharts = () => {
  updateFlightTrendChart()
  updateFlightDistributionChart()
  updateTopCitiesChart()
  updateSeatCapacityChart()
}

// 处理日期变化
const handleDateChange = () => {
  fetchDashboardData()
}

// 刷新数据
const refreshData = () => {
  ElMessage.success('正在刷新数据...')
  fetchDashboardData()
}

// 初始化图表
const initCharts = () => {
  // 销毁已有的图表实例
  if (flightTrendChartInstance) flightTrendChartInstance.dispose()
  if (flightDistributionChartInstance) flightDistributionChartInstance.dispose()
  if (topCitiesChartInstance) topCitiesChartInstance.dispose()
  if (seatCapacityChartInstance) seatCapacityChartInstance.dispose()
  
  // 创建新的图表实例
  flightTrendChartInstance = echarts.init(flightTrendChart.value)
  flightDistributionChartInstance = echarts.init(flightDistributionChart.value)
  topCitiesChartInstance = echarts.init(topCitiesChart.value)
  seatCapacityChartInstance = echarts.init(seatCapacityChart.value)
  
  // 窗口大小变化时重新调整图表大小
  window.addEventListener('resize', () => {
    flightTrendChartInstance.resize()
    flightDistributionChartInstance.resize()
    topCitiesChartInstance.resize()
    seatCapacityChartInstance.resize()
  })
}

onMounted(() => {
  initCharts()
  fetchDashboardData()
})

onUnmounted(() => {
  // 销毁图表实例
  if (flightTrendChartInstance) flightTrendChartInstance.dispose()
  if (flightDistributionChartInstance) flightDistributionChartInstance.dispose()
  if (topCitiesChartInstance) topCitiesChartInstance.dispose()
  if (seatCapacityChartInstance) seatCapacityChartInstance.dispose()
  
  // 移除事件监听器
  window.removeEventListener('resize', () => {})
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dashboard-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
  margin-bottom: 20px;
}

.stat-card-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 48px;
  margin-right: 15px;
  padding: 10px;
  border-radius: 8px;
}

.flight-icon {
  background-color: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.city-icon {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.seat-icon {
  background-color: rgba(230, 162, 60, 0.1);
  color: #E6A23C;
}

.today-icon {
  background-color: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.stat-info {
  flex-grow: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  line-height: 1;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-trend {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.stat-trend.up {
  color: #67C23A;
}

.stat-trend.down {
  color: #F56C6C;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 350px;
  width: 100%;
}

.pie-chart {
  height: 350px;
}
</style> 