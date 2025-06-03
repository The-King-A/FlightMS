<template>
  <div class="flight-list-container">
    <div class="page-header">
      <h1 class="page-title">航班列表</h1>
      <el-button type="primary" @click="goToAddFlight">
        <el-icon><Plus /></el-icon>
        添加航班
      </el-button>
    </div>
    
    <!-- 搜索筛选区域 -->
    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" label-width="80px" inline>
        <el-form-item label="航班号">
          <el-input 
            v-model="searchForm.flightnum" 
            placeholder="请输入航班号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        
        <el-form-item label="出发城市">
          <el-select 
            v-model="searchForm.startcity" 
            placeholder="请选择出发城市"
            clearable
          >
            <el-option 
              v-for="city in cityOptions" 
              :key="city.cityid" 
              :label="city.cityname" 
              :value="city.cityid" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="到达城市">
          <el-select 
            v-model="searchForm.endcity" 
            placeholder="请选择到达城市"
            clearable
          >
            <el-option 
              v-for="city in cityOptions" 
              :key="city.cityid" 
              :label="city.cityname" 
              :value="city.cityid" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="飞行日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="exportToCSV">
            <el-icon><Download /></el-icon>
            导出CSV
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="card-header">
          <span>航班数据</span>
          <div class="header-actions">
            <el-button 
              type="danger" 
              size="small" 
              :disabled="selectedFlights.length === 0"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button 
              type="primary" 
              size="small"
              @click="refreshTable"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="paginatedFlights"
        style="width: 100%"
        row-key="flightid"
        @selection-change="handleSelectionChange"
        :row-class-name="tableRowClassName"
        border
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="flightnum" label="航班号" sortable>
          <template #default="{ row }">
            <el-tag>{{ row.flightnum }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="出发/到达城市" min-width="180">
          <template #default="{ row }">
            <div class="city-info">
              <span class="city-name">{{ row.startCityName }}</span>
              <el-icon class="arrow-icon"><Right /></el-icon>
              <span class="city-name">{{ row.endCityName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="flydate" label="飞行日期" sortable width="120">
          <template #default="{ row }">
            {{ formatDate(row.flydate) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="starttime" label="起飞时间" width="100" />
        
        <el-table-column prop="flytime" label="飞行时长" width="100" />
        
        <el-table-column prop="seatnum" label="座位数" sortable width="100">
          <template #default="{ row }">
            <el-tag :type="getSeatTagType(row.seatnum)">
              {{ row.seatnum }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleEdit(row)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredFlights.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, Refresh, Download, Delete, Edit, Right } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../services/api'
import { saveAs } from 'file-saver'
import { useFlightStore } from '../../stores/flight'

const router = useRouter()
const flightStore = useFlightStore()
const loading = computed(() => flightStore.isLoading)
const flights = computed(() => flightStore.getAllFlights)
const cityOptions = computed(() => flightStore.getAllCities)
const selectedFlights = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  flightnum: '',
  startcity: '',
  endcity: '',
  dateRange: []
})

// 获取航班数据
const fetchFlights = async () => {
  try {
    await flightStore.fetchAllFlights()
  } catch (error) {
    console.error('获取航班数据失败:', error)
  }
}

// 获取城市数据
const fetchCities = async () => {
  try {
    await flightStore.fetchAllCities()
  } catch (error) {
    console.error('获取城市数据失败:', error)
  }
}

// 过滤航班数据
const filteredFlights = computed(() => {
  let result = [...flights.value]
  
  // 航班号筛选
  if (searchForm.flightnum) {
    result = result.filter(flight => 
      flight.flightnum.toLowerCase().includes(searchForm.flightnum.toLowerCase())
    )
  }
  
  // 出发城市筛选
  if (searchForm.startcity) {
    result = result.filter(flight => flight.startcity === searchForm.startcity)
  }
  
  // 到达城市筛选
  if (searchForm.endcity) {
    result = result.filter(flight => flight.endcity === searchForm.endcity)
  }
  
  // 日期范围筛选
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    const startDate = new Date(searchForm.dateRange[0])
    const endDate = new Date(searchForm.dateRange[1])
    endDate.setHours(23, 59, 59, 999) // 设置为当天结束时间
    
    result = result.filter(flight => {
      const flightDate = new Date(flight.flydate)
      return flightDate >= startDate && flightDate <= endDate
    })
  }
  
  return result
})

// 分页数据
const paginatedFlights = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredFlights.value.slice(start, end)
})

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedFlights.value = selection
}

// 表格行样式
const tableRowClassName = ({ row, rowIndex }) => {
  if (rowIndex % 2 === 1) {
    return 'row-striped'
  }
  return ''
}

// 获取座位标签类型
const getSeatTagType = (seatnum) => {
  if (seatnum < 100) {
    return 'info'
  } else if (seatnum <= 200) {
    return 'success'
  } else {
    return 'warning'
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toISOString().split('T')[0]
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 重置搜索
const resetSearch = () => {
  searchForm.flightnum = ''
  searchForm.startcity = ''
  searchForm.endcity = ''
  searchForm.dateRange = []
  currentPage.value = 1
}

// 刷新表格
const refreshTable = () => {
  fetchFlights()
  ElMessage.success('数据已刷新')
}

// 导出CSV
const exportToCSV = () => {
  // 准备CSV数据
  const headers = ['航班号', '出发城市', '到达城市', '飞行日期', '起飞时间', '飞行时长', '座位数']
  
  const csvData = filteredFlights.value.map(flight => [
    flight.flightnum,
    flight.startCityName,
    flight.endCityName,
    formatDate(flight.flydate),
    flight.starttime,
    flight.flytime,
    flight.seatnum
  ])
  
  // 添加表头
  csvData.unshift(headers)
  
  // 生成CSV内容
  const csvContent = csvData.map(row => row.join(',')).join('\n')
  
  // 创建Blob对象
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  
  // 下载文件
  saveAs(blob, `航班数据_${new Date().toISOString().split('T')[0]}.csv`)
  
  ElMessage.success('CSV文件导出成功')
}

// 编辑航班
const handleEdit = (row) => {
  router.push(`/flights/edit/${row.flightid}`)
}

// 删除航班
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除航班 ${row.flightnum} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await flightStore.deleteFlight(row.flightid)
      ElMessage.success('删除成功')
    } catch (error) {
      console.error('删除航班失败:', error)
    }
  }).catch(() => {})
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedFlights.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedFlights.value.length} 条记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = selectedFlights.value.map(flight => flight.flightid)
      await flightStore.batchDeleteFlights(ids)
      ElMessage.success('批量删除成功')
    } catch (error) {
      console.error('批量删除失败:', error)
    }
  }).catch(() => {})
}

// 跳转到添加航班页面
const goToAddFlight = () => {
  router.push('/flights/add')
}

onMounted(() => {
  fetchFlights()
  fetchCities()
})
</script>

<style scoped>
.flight-list-container {
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.city-info {
  display: flex;
  align-items: center;
}

.city-name {
  font-weight: 500;
}

.arrow-icon {
  margin: 0 8px;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 表格行交替颜色 */
:deep(.row-striped) {
  background-color: #fafafa;
}
</style> 