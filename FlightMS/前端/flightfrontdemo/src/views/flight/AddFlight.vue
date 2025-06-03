<template>
  <div class="add-flight-container">
    <div class="page-header">
      <h1 class="page-title">添加航班</h1>
      <el-button @click="goBack">
        <el-icon><Back /></el-icon>
        返回列表
      </el-button>
    </div>
    
    <el-card shadow="hover" class="form-card">
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
          <el-button type="primary" :loading="loading" @click="submitForm">提交</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Back } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../services/api'
import { useFlightStore } from '../../stores/flight'

const router = useRouter()
const flightStore = useFlightStore()
const flightFormRef = ref(null)
const loading = computed(() => flightStore.isLoading)
const cityOptions = computed(() => flightStore.getAllCities)
const flightNumExists = ref(false)

// 航班表单
const flightForm = reactive({
  flightnum: '',
  flydate: '',
  startcity: '',
  endcity: '',
  starttime: '',
  flytime: '',
  seatnum: 150
})

// 自定义校验规则：检查航班号是否已存在
const validateFlightNum = (rule, value, callback) => {
  if (flightNumExists.value) {
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

// 检查航班号是否已存在
const checkFlightNum = async () => {
  if (!flightForm.flightnum) return
  
  try {
    if (!flightStore.getAllFlights.length) {
      await flightStore.fetchAllFlights()
    }
    
    flightNumExists.value = flightStore.getAllFlights.some(flight => 
      flight.flightnum.toLowerCase() === flightForm.flightnum.toLowerCase()
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
        await flightStore.createFlight(flightForm)
        ElMessage.success('添加航班成功')
        
        // 询问是否继续添加
        ElMessageBox.confirm('航班添加成功，是否继续添加?', '提示', {
          confirmButtonText: '继续添加',
          cancelButtonText: '返回列表',
          type: 'success'
        }).then(() => {
          resetForm()
        }).catch(() => {
          router.push('/flights')
        })
      } catch (error) {
        console.error('添加航班失败:', error)
      }
    } else {
      ElMessage.warning('请正确填写表单')
      return false
    }
  })
}

// 重置表单
const resetForm = () => {
  flightFormRef.value.resetFields()
  flightNumExists.value = false
}

// 返回列表页
const goBack = () => {
  router.push('/flights')
}

onMounted(() => {
  fetchCities()
})
</script>

<style scoped>
.add-flight-container {
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
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  gap: 20px;
}
</style> 