import { defineStore } from 'pinia'
import api from '../services/api'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'
import router from '../router'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: Cookies.get('token') || null,
    loading: false
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    currentUser: (state) => state.user
  },
  
  actions: {
    async login(credentials) {
      try {
        this.loading = true
        const response = await api.auth.login(credentials)
        
        if (response.success) {
          // 模拟JWT token，实际应用中从后端获取
          const token = btoa(JSON.stringify({
            userId: credentials.userid,
            exp: new Date().getTime() + 24 * 60 * 60 * 1000 // 24小时后过期
          }))
          
          this.token = token
          this.user = { userid: credentials.userid }
          
          // 将token保存到cookie中，有效期1天
          Cookies.set('token', token, { expires: 1 })
          
          ElMessage.success(response.message || '登录成功！')
          router.push('/home')
          return true
        }
      } catch (error) {
        const errorMsg = error.response?.data?.message || '登录失败，请检查用户名和密码'
        ElMessage.error(errorMsg)
        return false
      } finally {
        this.loading = false
      }
    },
    
    async register(userData) {
      try {
        this.loading = true
        const response = await api.auth.register(userData)
        
        if (response.success) {
          ElMessage.success(response.message || '注册成功！请登录')
          router.push('/login')
          return true
        }
      } catch (error) {
        const errorMsg = error.response?.data?.message || '注册失败，请稍后再试'
        ElMessage.error(errorMsg)
        return false
      } finally {
        this.loading = false
      }
    },
    
    logout() {
      this.user = null
      this.token = null
      Cookies.remove('token')
      router.push('/login')
      ElMessage.success('已成功退出登录')
    },
    
    async checkUsername(userid) {
      try {
        const response = await api.auth.checkUsername(userid)
        return response.exists
      } catch (error) {
        console.error('检查用户名出错:', error)
        return false
      }
    }
  }
}) 