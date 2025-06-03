<template>
  <div class="edit-flight-container">
    <div class="page-header">
      <h1 class="page-title">编辑航班</h1>
      <el-button @click="goBack">
        <el-icon><Back /></el-icon>
        返回列表
      </el-button>
    </div>
    
    <el-card v-loading="pageLoading" shadow="hover" class="form-card">
      <el-form 
        ref="flightFormRef"
        :model="flightForm"
        :rules="flightRules"
        label-width="100px"
        status-icon
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="航班号" prop="flightnum">
              <el-input 
                v-model="flightForm.flightnum" 
                placeholder="请输入航班号"
                clearable
                @blur="checkFlightNum"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="飞行日期" prop="flydate">
              <el-date-picker
                v-model="flightForm.flydate"
                type="date"
                placeholder="选择飞行日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出发城市" prop="startcity">
              <el-select 
                v-model="flightForm.startcity" 
                placeholder="请选择出发城市"
                style="width: 100%"
                filterable
              >
                <el-option 
                  v-for="city in cityOptions" 
                  :key="city.cityid" 
                  :label="city.cityname" 
                  :value="city.cityid" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="到达城市" prop="endcity">
              <el-select 
                v-model="flightForm.endcity" 
                placeholder="请选择到达城市"
                style="width: 100%"
                filterable
              >
                <el-option 
                  v-for="city in cityOptions" 
                  :key="city.cityid" 
                  :label="city.cityname" 
                  :value="city.cityid" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="起飞时间" prop="starttime">
              <el-time-picker
                v-model="flightForm.starttime"
                placeholder="选择起飞时间"
                style="width: 100%"
                format="HH:mm"
                value-format="HH:mm"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="飞行时长" prop="flytime">
              <el-input 
                v-model="flightForm.flytime" 
                placeholder="请输入飞行时长 (如: 2h30m)"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="座位数" prop="seatnum">
              <el-input-number 
                v-model="flightForm.seatnum" 
                :min="1" 
                :max="500"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <div class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Back } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../services/api'
import { useFlightStore } from '../../stores/flight'

const router = useRouter()
const route = useRoute()
const flightId = route.params.id
const flightStore = useFlightStore()
const flightFormRef = ref(null)
const pageLoading = ref(true)
const submitLoading = computed(() => flightStore.isLoading)
const cityOptions = computed(() => flightStore.getAllCities)
const flightNumExists = ref(false)
const originalFlightNum = ref('')

// 航班表单
const flightForm = reactive({
  flightid: null,
  flightnum: '',
  flydate: '',
  startcity: '',
  endcity: '',
  starttime: '',
  flytime: '',
  seatnum: 0
})

// 自定义校验规则：检查航班号是否已存在
const validateFlightNum = (rule, value, callback) => {
  if (flightNumExists.value && value !== originalFlightNum.value) {
    callback(new Error('该航班号已存在'))
  } else {
    callback()
  }
}

// 自定义校验规则：检查出发城市和到达城市不能相同
const validateCities = (rule, value, callback) => {
  if (value && value === flightForm.startcity) {
    callback(new Error('出发城市和到达城市不能相同'))
  } else {
    callback()
  }
}

// 自定义校验规则：飞行时长格式
const validateFlightTime = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入飞行时长'))
    return
  }
  
  const pattern = /^(\d+h)?(\d+m)?$/
  if (!pattern.test(value)) {
    callback(new Error('格式错误，正确格式如: 2h30m'))
    return
  }
  
  callback()
}

// 表单验证规则
const flightRules = {
  flightnum: [
    { required: true, message: '请输入航班号', trigger: 'blur' },
    { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' },
    { validator: validateFlightNum, trigger: 'blur' }
  ],
  flydate: [
    { required: true, message: '请选择飞行日期', trigger: 'change' }
  ],
  startcity: [
    { required: true, message: '请选择出发城市', trigger: 'change' }
  ],
  endcity: [
    { required: true, message: '请选择到达城市', trigger: 'change' },
    { validator: validateCities, trigger: 'change' }
  ],
  starttime: [
    { required: true, message: '请选择起飞时间', trigger: 'change' }
  ],
  flytime: [
    { required: true, message: '请输入飞行时长', trigger: 'blur' },
    { validator: validateFlightTime, trigger: 'blur' }
  ],
  seatnum: [
    { required: true, message: '请输入座位数', trigger: 'blur' },
    { type: 'number', min: 1, message: '座位数必须大于0', trigger: 'blur' }
  ]
}

// 获取城市数据
const fetchCities = async () => {
  try {
    await flightStore.fetchAllCities()
  } catch (error) {
    console.error('获取城市数据失败:', error)
  }
}

// 获取航班数据
const fetchFlight = async () => {
  pageLoading.value = true
  try {
    // 确保有航班数据
    if (!flightStore.getAllFlights.length) {
      await flightStore.fetchAllFlights()
    }
    
    const flight = flightStore.getAllFlights.find(f => f.flightid == flightId)
    
    if (!flight) {
      // 如果本地没有找到，尝试从API单独获取
      const apiResponse = await api.flights.getById(flightId)
      if (apiResponse) {
        processFlightData(apiResponse)
      } else {
        throw new Error('找不到航班数据')
      }
    } else {
      processFlightData(flight)
    }
  } catch (error) {
    console.error('获取航班数据失败:', error)
    ElMessage.error('获取航班数据失败')
    router.push('/flights')
  } finally {
    pageLoading.value = false
  }
}

// 处理航班数据
const processFlightData = (flight) => {
  // 格式化日期
  if (flight.flydate) {
    const date = new Date(flight.flydate)
    flight.flydate = date.toISOString().split('T')[0]
  }
  
  // 保存原始航班号
  originalFlightNum.value = flight.flightnum
  
  // 填充表单
  Object.assign(flightForm, flight)
}

// 检查航班号是否已存在
const checkFlightNum = async () => {
  if (!flightForm.flightnum || flightForm.flightnum === originalFlightNum.value) return
  
  try {
    if (!flightStore.getAllFlights.length) {
      await flightStore.fetchAllFlights()
    }
    
    flightNumExists.value = flightStore.getAllFlights.some(flight => 
      flight.flightnum.toLowerCase() === flightForm.flightnum.toLowerCase() && 
      flight.flightid !== flightForm.flightid
    )
    
    if (flightNumExists.value) {
      ElMessage.warning('该航班号已存在')
    }
  } catch (error) {
    console.error('检查航班号失败:', error)
  }
}

// 提交表单
const submitForm = () => {
  flightFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await flightStore.updateFlight(flightId, flightForm)
        ElMessage.success('更新航班成功')
        router.push('/flights')
      } catch (error) {
        console.error('更新航班失败:', error)
      }
    } else {
      ElMessage.warning('请正确填写表单')
      return false
    }
  })
}

// 重置表单
const resetForm = () => {
  fetchFlight() // 重新获取原始数据
  ElMessage.info('已重置为原始数据')
}

// 返回列表页
const goBack = () => {
  router.push('/flights')
}

onMounted(() => {
  fetchCities()
  fetchFlight()
})
</script>

<style scoped>
.edit-flight-container {
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

.form-card {
  margin-bottom: 20px;
  min-height: 400px;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  gap: 20px;
}
</style> 