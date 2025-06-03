import axios from 'axios'
import Cookies from 'js-cookie'
import router from '../router'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 获取token并添加到请求头
    const token = Cookies.get('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    console.log('发送请求到:', config.url, '数据:', config.data || config.params)
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    console.log('请求成功:', response.config.url, '响应数据:', response.data)
    return response.data
  },
  error => {
    if (error.response) {
      console.error('API错误:', error.response.config.url, '状态码:', error.response.status, '数据:', error.response.data)
      
      switch (error.response.status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          Cookies.remove('token')
          router.push('/login')
          break
        case 500:
          console.error('服务器内部错误:', error.response.data)
          break
        default:
          // 其他错误
          console.error('API错误:', error.response.data)
      }
    } else if (error.request) {
      console.error('未收到响应:', error.request)
    } else {
      console.error('请求配置错误:', error.message)
    }
    return Promise.reject(error)
  }
)

// API接口
export default {
  // 用户认证相关
  auth: {
    login: (credentials) => api.post('/auth/login', credentials),
    register: (userData) => api.post('/auth/register', userData),
    checkUsername: (userid) => api.get(`/auth/check/${userid}`)
  },
  
  // 航班管理相关
  flights: {
    getAll: () => api.get('/flights'),
    getAllWithCityNames: () => api.get('/flights/with-city-names'),
    getById: (id) => api.get(`/flights/${id}`),
    getByIdWithCityNames: (id) => api.get(`/flights/${id}/with-city-names`),
    create: (flightData) => api.post('/flights', flightData),
    update: (id, flightData) => api.put(`/flights/${id}`, flightData),
    delete: (id) => api.delete(`/flights/${id}`)
  },
  
  // 城市数据相关
  cities: {
    getAll: () => api.get('/cities'),
    getById: (id) => api.get(`/cities/${id}`)
  }
} 