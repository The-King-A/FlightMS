<template>
  <div class="register-container">
    <!-- 动态背景元素 -->
    <div class="background-animation">
      <div class="flight-path" v-for="n in 8" :key="n"></div>
      <div class="particle" v-for="n in 20" :key="`p-${n}`"></div>
    </div>
    
    <!-- 飞机装饰元素 -->
    <div class="airplane-container">
      <div class="airplane airplane-1"></div>
      <div class="airplane airplane-2"></div>
      <div class="airplane airplane-3"></div>
    </div>
    
    <!-- 注册卡片 -->
    <div class="register-card" :class="{ 'form-active': formActive }">
      <div class="card-glow"></div>
      
      <div class="register-header">
        <div class="logo-container">
          <img src="../assets/airplane.svg" class="register-logo" alt="Logo" />
          <div class="logo-ring"></div>
          <div class="logo-pulse"></div>
        </div>
        <h2 class="register-title">注册新账号</h2>
        <p class="register-subtitle">成为航班管理系统的用户</p>
      </div>
      
      <el-form 
        ref="registerFormRef" 
        :model="registerForm" 
        :rules="registerRules" 
        class="register-form"
        label-position="right"
        label-width="0px"
        @focus="formActive = true"
        @blur="formActive = false">
        
        <el-form-item prop="userid" class="input-item">
          <el-input 
            v-model="registerForm.userid" 
            prefix-icon="User"
            placeholder="请输入用户名"
            clearable
            @focus="focusField = 'userid'"
            @blur="checkUserExists(); focusField = ''"
          >
          </el-input>
          <div class="input-focus-line" :class="{ active: focusField === 'userid' }"></div>
        </el-form-item>
        
        <el-form-item prop="userpwd" class="input-item">
          <el-input 
            v-model="registerForm.userpwd" 
            prefix-icon="Lock"
            placeholder="请输入密码" 
            show-password
            @focus="focusField = 'userpwd'"
            @blur="focusField = ''"
          >
          </el-input>
          <div class="input-focus-line" :class="{ active: focusField === 'userpwd' }"></div>
        </el-form-item>
        
        <el-form-item prop="confirmPwd" class="input-item">
          <el-input 
            v-model="registerForm.confirmPwd" 
            prefix-icon="Lock"
            placeholder="请确认密码" 
            show-password
            @focus="focusField = 'confirmPwd'"
            @blur="focusField = ''"
          >
          </el-input>
          <div class="input-focus-line" :class="{ active: focusField === 'confirmPwd' }"></div>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            class="register-button" 
            @click="handleRegister">
            <span class="button-text">注册</span>
            <div class="button-flight-trail"></div>
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const registerFormRef = ref(null)
const loading = ref(false)
const userExists = ref(false)
const formActive = ref(false)
const focusField = ref('')

const registerForm = reactive({
  userid: '',
  userpwd: '',
  confirmPwd: ''
})

// 自定义校验规则：检查用户名是否已存在
const validateUsername = (rule, value, callback) => {
  if (userExists.value) {
    callback(new Error('该用户名已被使用'))
  } else {
    callback()
  }
}

// 自定义校验规则：确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.userpwd) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  userid: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度至少为3个字符', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' }
  ],
  userpwd: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 检查用户名是否已存在
const checkUserExists = async () => {
  if (registerForm.userid && registerForm.userid.length >= 3) {
    userExists.value = await userStore.checkUsername(registerForm.userid)
    if (userExists.value) {
      ElMessage.warning('该用户名已被使用')
    }
  }
}

const handleRegister = () => {
  registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const userData = {
          userid: registerForm.userid,
          userpwd: registerForm.userpwd
        }
        await userStore.register(userData)
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请正确填写注册信息')
      return false
    }
  })
}

// 动态背景粒子效果
onMounted(() => {
  animateParticles()
})

const animateParticles = () => {
  const particles = document.querySelectorAll('.particle')
  particles.forEach((particle, index) => {
    // 随机定位和动画
    const size = Math.random() * 5 + 2
    const duration = Math.random() * 10 + 10
    const delay = Math.random() * 5
    
    particle.style.width = `${size}px`
    particle.style.height = `${size}px`
    particle.style.left = `${Math.random() * 100}%`
    particle.style.top = `${Math.random() * 100}%`
    particle.style.animationDuration = `${duration}s`
    particle.style.animationDelay = `${delay}s`
    particle.style.opacity = Math.random() * 0.6 + 0.2
  })
  
  const flightPaths = document.querySelectorAll('.flight-path')
  flightPaths.forEach((path, index) => {
    const duration = Math.random() * 15 + 10
    const delay = Math.random() * 10
    const top = Math.random() * 80 + 10
    
    path.style.top = `${top}%`
    path.style.animationDuration = `${duration}s`
    path.style.animationDelay = `${delay}s`
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #0a192f 0%, #0f2744 50%, #162b4b 100%);
  position: relative;
  overflow: hidden;
  perspective: 1000px;
}

/* 动态背景 */
.background-animation {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
}

.particle {
  position: absolute;
  background-color: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  animation: float 15s infinite linear;
}

.flight-path {
  position: absolute;
  width: 100%;
  height: 2px;
  background: linear-gradient(to right, transparent, rgba(48, 130, 255, 0.2), rgba(48, 130, 255, 0.5), rgba(48, 130, 255, 0.2), transparent);
  transform: translateX(-100%);
  animation: flightPath 20s infinite linear;
}

/* 飞机装饰元素 */
.airplane-container {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.airplane {
  position: absolute;
  width: 30px;
  height: 30px;
  background-image: url('../assets/airplane.svg');
  background-size: contain;
  background-repeat: no-repeat;
  opacity: 0.7;
  filter: brightness(1.5);
}

.airplane-1 {
  top: 25%;
  left: -50px;
  animation: flyAcross 35s linear infinite;
}

.airplane-2 {
  top: 60%;
  left: -30px;
  animation: flyAcross 28s linear infinite;
  animation-delay: 5s;
  transform: scale(0.7);
}

.airplane-3 {
  top: 40%;
  right: -40px;
  animation: flyAcrossReverse 42s linear infinite;
  animation-delay: 12s;
  transform: scale(0.8) scaleX(-1);
}

/* 注册卡片 */
.register-card {
  width: 400px;
  padding: 40px;
  border-radius: 16px;
  background-color: rgba(16, 32, 58, 0.8);
  backdrop-filter: blur(10px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.3), 0 0 20px rgba(48, 130, 255, 0.1);
  border: 1px solid rgba(48, 130, 255, 0.2);
  position: relative;
  transition: all 0.5s ease-in-out;
  transform: translateY(0) rotateX(0);
  z-index: 10;
  overflow: hidden;
}

.register-card.form-active {
  transform: translateY(-5px) rotateX(2deg);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4), 0 0 30px rgba(48, 130, 255, 0.2);
}

.card-glow {
  position: absolute;
  width: 150%;
  height: 150%;
  top: -25%;
  left: -25%;
  background: radial-gradient(circle, rgba(48, 130, 255, 0.1) 0%, transparent 70%);
  pointer-events: none;
  opacity: 0;
  animation: cardGlow 8s infinite ease-in-out;
}

/* 注册头部 */
.register-header {
  text-align: center;
  margin-bottom: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.logo-container {
  position: relative;
  width: 80px;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
}

.register-logo {
  width: 64px;
  height: 64px;
  z-index: 2;
  filter: drop-shadow(0 0 10px rgba(48, 130, 255, 0.5));
}

.logo-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid rgba(48, 130, 255, 0.3);
  animation: logoRing 3s infinite ease-in-out;
}

.logo-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: rgba(48, 130, 255, 0.1);
  animation: logoPulse 3s infinite ease-in-out;
}

.register-title {
  font-size: 28px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 10px 0;
  text-shadow: 0 0 15px rgba(48, 130, 255, 0.5);
}

.register-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

/* 表单样式 */
.register-form {
  margin-bottom: 30px;
}

.input-item {
  margin-bottom: 25px;
  position: relative;
}

.el-input :deep(.el-input__wrapper) {
  background-color: rgba(16, 32, 58, 0.5);
  box-shadow: none;
  border: 1px solid rgba(48, 130, 255, 0.2);
  transition: all 0.3s;
  border-radius: 8px;
  padding: 10px 15px;
}

.el-input :deep(.el-input__wrapper):hover {
  border-color: rgba(48, 130, 255, 0.5);
}

.el-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(48, 130, 255, 0.8);
  box-shadow: 0 0 10px rgba(48, 130, 255, 0.2);
}

.el-input :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.9);
  height: 20px;
}

.el-input :deep(.el-input__prefix-inner .el-icon) {
  color: rgba(48, 130, 255, 0.8);
}

.input-focus-line {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(to right, rgba(48, 130, 255, 0.5), rgba(80, 200, 255, 0.8), rgba(48, 130, 255, 0.5));
  transform: translateX(-50%);
  transition: width 0.3s ease-in-out;
}

.input-focus-line.active {
  width: 100%;
}

/* 注册按钮 */
.register-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #1c64ce 0%, #3080ff 50%, #1c64ce 100%);
  border: none;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  box-shadow: 0 5px 15px rgba(28, 100, 206, 0.4);
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(28, 100, 206, 0.5);
  background: linear-gradient(135deg, #2470e0 0%, #4290ff 50%, #2470e0 100%);
}

.register-button:active {
  transform: translateY(1px);
  box-shadow: 0 2px 10px rgba(28, 100, 206, 0.3);
}

.button-text {
  position: relative;
  z-index: 2;
}

.button-flight-trail {
  position: absolute;
  width: 30px;
  height: 100%;
  background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0.8), transparent);
  top: 0;
  left: -100%;
  transform: skewX(-20deg);
  animation: buttonTrail 3s infinite;
}

/* 页脚 */
.register-footer {
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.login-link {
  color: #3080ff;
  text-decoration: none;
  margin-left: 5px;
  position: relative;
  transition: all 0.3s;
}

.login-link:hover {
  color: #60a0ff;
  text-shadow: 0 0 8px rgba(48, 130, 255, 0.5);
}

.login-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background-color: #3080ff;
  transition: width 0.3s;
}

.login-link:hover::after {
  width: 100%;
}

/* 动画定义 */
@keyframes float {
  0% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(20px, -20px);
  }
  100% {
    transform: translate(0, 0);
  }
}

@keyframes flightPath {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

@keyframes flyAcross {
  0% {
    transform: translateX(0) translateY(0) rotate(10deg);
  }
  50% {
    transform: translateX(calc(100vw + 100px)) translateY(-50px) rotate(0deg);
  }
  51% {
    transform: translateX(-50px) translateY(20px) rotate(10deg);
  }
  100% {
    transform: translateX(0) translateY(0) rotate(10deg);
  }
}

@keyframes flyAcrossReverse {
  0% {
    transform: scaleX(-1) translateX(0) translateY(0) rotate(-5deg);
  }
  50% {
    transform: scaleX(-1) translateX(calc(100vw + 100px)) translateY(30px) rotate(5deg);
  }
  51% {
    transform: scaleX(-1) translateX(-50px) translateY(-20px) rotate(-5deg);
  }
  100% {
    transform: scaleX(-1) translateX(0) translateY(0) rotate(-5deg);
  }
}

@keyframes logoRing {
  0% {
    transform: scale(0.8);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.3;
  }
  100% {
    transform: scale(0.8);
    opacity: 0.6;
  }
}

@keyframes logoPulse {
  0% {
    transform: scale(0.5);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.2);
    opacity: 0;
  }
  100% {
    transform: scale(0.5);
    opacity: 0.8;
  }
}

@keyframes cardGlow {
  0% {
    opacity: 0;
    transform: rotate(0deg);
  }
  30% {
    opacity: 1;
  }
  70% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: rotate(360deg);
  }
}

@keyframes buttonTrail {
  0% {
    left: -100%;
  }
  50% {
    left: 100%;
  }
  100% {
    left: 100%;
  }
}
</style> 