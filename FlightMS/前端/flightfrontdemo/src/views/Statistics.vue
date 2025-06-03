<template>
  <div class="statistics-container">
    <div class="page-header">
      <h1 class="page-title">航班统计分析</h1>
      <div class="header-actions">
        <el-select v-model="timeRange" placeholder="选择时间范围" @change="handleTimeRangeChange">
          <el-option label="最近一周" value="week" />
          <el-option label="最近一个月" value="month" />
          <el-option label="最近三个月" value="quarter" />
          <el-option label="最近一年" value="year" />
          <el-option label="全部" value="all" />
        </el-select>
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon-container flight-bg">
              <el-icon class="stat-icon"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statisticsData.totalFlights }}</div>
              <div class="stat-label">总航班数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon-container city-bg">
              <el-icon class="stat-icon"><Location /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statisticsData.routeCount }}</div>
              <div class="stat-label">航线数量</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon-container seat-bg">
              <el-icon class="stat-icon"><SuitcaseLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statisticsData.totalSeats }}</div>
              <div class="stat-label">总座位数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-content">
            <div class="stat-icon-container avg-bg">
              <el-icon class="stat-icon"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statisticsData.avgSeatsPerFlight }}</div>
              <div class="stat-label">平均座位数/航班</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>航班数量趋势</span>
            </div>
          </template>
          <div ref="flightTrendChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>航班座位分布</span>
            </div>
          </template>
          <div ref="seatDistributionChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热门出发城市</span>
            </div>
          </template>
          <div ref="departureChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热门到达城市</span>
            </div>
          </template>
          <div ref="arrivalChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热门航线</span>
            </div>
          </template>
          <div ref="routesChart" class="chart-container large-chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { Tickets, Location, SuitcaseLine, DataAnalysis, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import api from '../services/api'
import { useFlightStore } from '../stores/flight'

// 图表实例
let flightTrendChartInstance = null
let seatDistributionChartInstance = null
let departureChartInstance = null
let arrivalChartInstance = null
let routesChartInstance = null

// 使用flightStore
const flightStore = useFlightStore()

// 响应式数据
const timeRange = ref('month')
const flightTrendChart = ref(null)
const seatDistributionChart = ref(null)
const departureChart = ref(null)
const arrivalChart = ref(null)
const routesChart = ref(null)

// 统计数据
const statisticsData = reactive({
  totalFlights: 0,
  routeCount: 0,
  totalSeats: 0,
  avgSeatsPerFlight: 0,
  flightTrendData: [],
  seatDistributionData: [],
  departureData: [],
  arrivalData: [],
  routesData: []
})

// 数据更新回调函数
const handleDataUpdate = () => {
  fetchStatisticsData()
}

// 处理时间范围变化
const handleTimeRangeChange = () => {
  fetchStatisticsData()
}

// 获取统计数据
const fetchStatisticsData = async () => {
  try {
    // 确保航班和城市数据已加载
    if (!flightStore.getAllFlights.length) {
      await flightStore.fetchAllFlights()
    }
    
    if (!flightStore.getAllCities.length) {
      await flightStore.fetchAllCities()
    }
    
    // 获取数据
    const flights = flightStore.getAllFlights
    const cities = flightStore.getAllCities
    
    // 按时间范围过滤数据
    const filteredFlights = filterFlightsByTimeRange(flights)
    
    // 计算基本统计数据
    calculateBasicStatistics(filteredFlights)
    
    // 处理航班趋势数据
    processFlightTrendData(filteredFlights)
    
    // 处理座位分布数据
    processSeatDistributionData(filteredFlights)
    
    // 处理出发城市数据
    processDepartureData(filteredFlights, cities)
    
    // 处理到达城市数据
    processArrivalData(filteredFlights, cities)
    
    // 处理航线数据
    processRoutesData(filteredFlights, cities)
    
    // 更新所有图表
    updateAllCharts()
    
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 按时间范围过滤航班数据
const filterFlightsByTimeRange = (flights) => {
  if (timeRange.value === 'all') {
    return flights
  }
  
  const now = new Date()
  let startDate
  
  switch (timeRange.value) {
    case 'week':
      startDate = new Date(now)
      startDate.setDate(now.getDate() - 7)
      break
    case 'month':
      startDate = new Date(now)
      startDate.setMonth(now.getMonth() - 1)
      break
    case 'quarter':
      startDate = new Date(now)
      startDate.setMonth(now.getMonth() - 3)
      break
    case 'year':
      startDate = new Date(now)
      startDate.setFullYear(now.getFullYear() - 1)
      break
    default:
      startDate = new Date(0) // 从1970年开始
  }
  
  return flights.filter(flight => {
    const flightDate = new Date(flight.flydate)
    return flightDate >= startDate && flightDate <= now
  })
}

// 计算基本统计数据
const calculateBasicStatistics = (flights) => {
  statisticsData.totalFlights = flights.length
  
  // 计算总座位数
  statisticsData.totalSeats = flights.reduce((total, flight) => total + flight.seatnum, 0)
  
  // 计算平均座位数
  statisticsData.avgSeatsPerFlight = flights.length > 0 
    ? Math.round(statisticsData.totalSeats / flights.length) 
    : 0
  
  // 计算航线数量
  const routes = new Set()
  flights.forEach(flight => {
    const routeKey = `${flight.startcity}-${flight.endcity}`
    routes.add(routeKey)
  })
  statisticsData.routeCount = routes.size
}

// 处理航班趋势数据
const processFlightTrendData = (flights) => {
  // 按日期分组航班
  const flightsByDate = {}
  
  flights.forEach(flight => {
    const date = new Date(flight.flydate)
    const dateStr = date.toISOString().split('T')[0]
    
    if (!flightsByDate[dateStr]) {
      flightsByDate[dateStr] = []
    }
    
    flightsByDate[dateStr].push(flight)
  })
  
  // 生成数据
  const dates = Object.keys(flightsByDate).sort()
  const counts = dates.map(date => flightsByDate[date].length)
  
  statisticsData.flightTrendData = { dates, counts }
}

// 处理座位分布数据
const processSeatDistributionData = (flights) => {
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
  statisticsData.seatDistributionData = Object.entries(seatRanges)
    .map(([name, value]) => ({ name, value }))
}

// 处理出发城市数据
const processDepartureData = (flights, cities) => {
  // 创建城市ID到名称的映射
  const cityMap = {}
  cities.forEach(city => {
    cityMap[city.cityid] = city.cityname
  })
  
  // 按出发城市统计航班数量
  const departureCounts = {}
  
  flights.forEach(flight => {
    const cityName = cityMap[flight.startcity] || '未知'
    
    if (!departureCounts[cityName]) {
      departureCounts[cityName] = 0
    }
    
    departureCounts[cityName]++
  })
  
  // 转换为图表数据格式并排序
  statisticsData.departureData = Object.entries(departureCounts)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 10) // 只取前10个城市
}

// 处理到达城市数据
const processArrivalData = (flights, cities) => {
  // 创建城市ID到名称的映射
  const cityMap = {}
  cities.forEach(city => {
    cityMap[city.cityid] = city.cityname
  })
  
  // 按到达城市统计航班数量
  const arrivalCounts = {}
  
  flights.forEach(flight => {
    const cityName = cityMap[flight.endcity] || '未知'
    
    if (!arrivalCounts[cityName]) {
      arrivalCounts[cityName] = 0
    }
    
    arrivalCounts[cityName]++
  })
  
  // 转换为图表数据格式并排序
  statisticsData.arrivalData = Object.entries(arrivalCounts)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 10) // 只取前10个城市
}

// 处理航线数据
const processRoutesData = (flights, cities) => {
  // 创建城市ID到名称的映射
  const cityMap = {}
  cities.forEach(city => {
    cityMap[city.cityid] = city.cityname
  })
  
  // 按航线统计航班数量
  const routeCounts = {}
  
  flights.forEach(flight => {
    const startCity = cityMap[flight.startcity] || '未知'
    const endCity = cityMap[flight.endcity] || '未知'
    const routeName = `${startCity} → ${endCity}`
    
    if (!routeCounts[routeName]) {
      routeCounts[routeName] = 0
    }
    
    routeCounts[routeName]++
  })
  
  // 转换为图表数据格式并排序
  statisticsData.routesData = Object.entries(routeCounts)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 15) // 只取前15条航线
}

// 更新航班趋势图表
const updateFlightTrendChart = () => {
  if (!flightTrendChartInstance) return
  
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
      data: statisticsData.flightTrendData.dates,
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
        type: 'bar',
        data: statisticsData.flightTrendData.counts,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ]
          }
        },
        emphasis: {
          itemStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#188df0' },
                { offset: 0.7, color: '#188df0' },
                { offset: 1, color: '#83bff6' }
              ]
            }
          }
        }
      }
    ]
  }
  
  flightTrendChartInstance.setOption(option)
}

// 更新座位分布图表
const updateSeatDistributionChart = () => {
  if (!seatDistributionChartInstance) return
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      top: 'center'
    },
    series: [
      {
        name: '座位分布',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: statisticsData.seatDistributionData,
        color: ['#67C23A', '#E6A23C', '#F56C6C']
      }
    ]
  }
  
  seatDistributionChartInstance.setOption(option)
}

// 更新出发城市图表
const updateDepartureChart = () => {
  if (!departureChartInstance) return
  
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
      data: statisticsData.departureData.map(item => item.name),
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
        data: statisticsData.departureData.map(item => item.value),
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: '#67C23A' },
              { offset: 1, color: '#95D475' }
            ]
          }
        }
      }
    ]
  }
  
  departureChartInstance.setOption(option)
}

// 更新到达城市图表
const updateArrivalChart = () => {
  if (!arrivalChartInstance) return
  
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
      data: statisticsData.arrivalData.map(item => item.name),
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
        data: statisticsData.arrivalData.map(item => item.value),
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: '#E6A23C' },
              { offset: 1, color: '#F3D19E' }
            ]
          }
        }
      }
    ]
  }
  
  arrivalChartInstance.setOption(option)
}

// 更新航线图表
const updateRoutesChart = () => {
  if (!routesChartInstance) return
  
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
      data: statisticsData.routesData.map(item => item.name),
      axisLabel: {
        interval: 0,
        rotate: 45,
        formatter: (value) => {
          if (value.length > 15) {
            return value.substring(0, 12) + '...'
          }
          return value
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '航班数量'
    },
    series: [
      {
        name: '航班数量',
        type: 'bar',
        data: statisticsData.routesData.map(item => item.value),
        itemStyle: {
          color: function(params) {
            // 颜色渐变
            const colorList = [
              '#5470c6', '#91cc75', '#fac858', '#ee6666',
              '#73c0de', '#3ba272', '#fc8452', '#9a60b4'
            ]
            const index = params.dataIndex % colorList.length
            return colorList[index]
          }
        }
      }
    ]
  }
  
  routesChartInstance.setOption(option)
}

// 更新所有图表
const updateAllCharts = () => {
  updateFlightTrendChart()
  updateSeatDistributionChart()
  updateDepartureChart()
  updateArrivalChart()
  updateRoutesChart()
}

// 刷新数据
const refreshData = async () => {
  ElMessage.success('正在刷新数据...')
  try {
    await flightStore.fetchAllFlights()
    await flightStore.fetchAllCities()
    fetchStatisticsData()
  } catch (error) {
    console.error('刷新数据失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  // 销毁已有的图表实例
  if (flightTrendChartInstance) flightTrendChartInstance.dispose()
  if (seatDistributionChartInstance) seatDistributionChartInstance.dispose()
  if (departureChartInstance) departureChartInstance.dispose()
  if (arrivalChartInstance) arrivalChartInstance.dispose()
  if (routesChartInstance) routesChartInstance.dispose()
  
  // 创建新的图表实例
  flightTrendChartInstance = echarts.init(flightTrendChart.value)
  seatDistributionChartInstance = echarts.init(seatDistributionChart.value)
  departureChartInstance = echarts.init(departureChart.value)
  arrivalChartInstance = echarts.init(arrivalChart.value)
  routesChartInstance = echarts.init(routesChart.value)
  
  // 窗口大小变化时重新调整图表大小
  window.addEventListener('resize', handleResize)
}

// 处理窗口大小变化
const handleResize = () => {
  flightTrendChartInstance?.resize()
  seatDistributionChartInstance?.resize()
  departureChartInstance?.resize()
  arrivalChartInstance?.resize()
  routesChartInstance?.resize()
}

onMounted(() => {
  initCharts()
  // 注册数据更新监听器
  flightStore.registerUpdateListener(handleDataUpdate)
  fetchStatisticsData()
})

onUnmounted(() => {
  // 销毁图表实例
  flightTrendChartInstance?.dispose()
  seatDistributionChartInstance?.dispose()
  departureChartInstance?.dispose()
  arrivalChartInstance?.dispose()
  routesChartInstance?.dispose()
  
  // 移除事件监听器
  window.removeEventListener('resize', handleResize)
  
  // 移除数据更新监听器
  flightStore.removeUpdateListener(handleDataUpdate)
})

// 监听flightStore中的lastUpdated变化
watch(() => flightStore.getUpdateTimestamp, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    fetchStatisticsData()
  }
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
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
  margin-bottom: 20px;
}

.stat-card-content {
  display: flex;
  align-items: center;
}

.stat-icon-container {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 15px;
}

.flight-bg {
  background-color: rgba(64, 158, 255, 0.1);
}

.city-bg {
  background-color: rgba(103, 194, 58, 0.1);
}

.seat-bg {
  background-color: rgba(230, 162, 60, 0.1);
}

.avg-bg {
  background-color: rgba(144, 147, 153, 0.1);
}

.stat-icon {
  font-size: 28px;
}

.flight-bg .stat-icon {
  color: #409EFF;
}

.city-bg .stat-icon {
  color: #67C23A;
}

.seat-bg .stat-icon {
  color: #E6A23C;
}

.avg-bg .stat-icon {
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

.large-chart {
  height: 400px;
}
</style> 