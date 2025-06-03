import { defineStore } from 'pinia'
import api from '../services/api'
import { ElMessage } from 'element-plus'

export const useFlightStore = defineStore('flight', {
  state: () => ({
    flights: [],
    cities: [],
    loading: false,
    lastUpdated: new Date(),
    listeners: []
  }),
  
  getters: {
    getAllFlights: (state) => state.flights,
    getAllCities: (state) => state.cities,
    isLoading: (state) => state.loading,
    getUpdateTimestamp: (state) => state.lastUpdated
  },
  
  actions: {
    // 注册数据更新的监听器
    registerUpdateListener(callback) {
      if (typeof callback === 'function' && !this.listeners.includes(callback)) {
        this.listeners.push(callback)
        return true
      }
      return false
    },
    
    // 移除监听器
    removeUpdateListener(callback) {
      const index = this.listeners.indexOf(callback)
      if (index !== -1) {
        this.listeners.splice(index, 1)
        return true
      }
      return false
    },
    
    // 通知所有监听器数据已更新
    notifyDataUpdated() {
      this.lastUpdated = new Date()
      this.listeners.forEach(callback => {
        try {
          callback()
        } catch (error) {
          console.error('更新监听器执行出错:', error)
        }
      })
    },
    
    // 获取所有航班数据
    async fetchAllFlights() {
      this.loading = true
      try {
        const response = await api.flights.getAllWithCityNames()
        this.flights = response
        this.notifyDataUpdated()
        return response
      } catch (error) {
        console.error('获取航班数据失败:', error)
        ElMessage.error('获取航班数据失败')
        throw error
      } finally {
        this.loading = false
      }
    },
    
    // 获取所有城市数据
    async fetchAllCities() {
      try {
        const response = await api.cities.getAll()
        this.cities = response
        return response
      } catch (error) {
        console.error('获取城市数据失败:', error)
        ElMessage.error('获取城市数据失败')
        throw error
      }
    },
    
    // 创建航班
    async createFlight(flightData) {
      this.loading = true
      try {
        const result = await api.flights.create(flightData)
        // 更新航班数据
        await this.fetchAllFlights()
        return result
      } catch (error) {
        console.error('创建航班失败:', error)
        ElMessage.error('创建航班失败')
        throw error
      } finally {
        this.loading = false
      }
    },
    
    // 更新航班
    async updateFlight(id, flightData) {
      this.loading = true
      try {
        const result = await api.flights.update(id, flightData)
        // 更新航班数据
        await this.fetchAllFlights()
        return result
      } catch (error) {
        console.error('更新航班失败:', error)
        ElMessage.error('更新航班失败')
        throw error
      } finally {
        this.loading = false
      }
    },
    
    // 删除航班
    async deleteFlight(id) {
      this.loading = true
      try {
        const result = await api.flights.delete(id)
        // 更新航班数据
        await this.fetchAllFlights()
        return result
      } catch (error) {
        console.error('删除航班失败:', error)
        ElMessage.error('删除航班失败')
        throw error
      } finally {
        this.loading = false
      }
    },
    
    // 批量删除航班
    async batchDeleteFlights(ids) {
      this.loading = true
      try {
        const deletePromises = ids.map(id => api.flights.delete(id))
        const results = await Promise.all(deletePromises)
        // 更新航班数据
        await this.fetchAllFlights()
        return results
      } catch (error) {
        console.error('批量删除航班失败:', error)
        ElMessage.error('批量删除航班失败')
        throw error
      } finally {
        this.loading = false
      }
    }
  }
}) 