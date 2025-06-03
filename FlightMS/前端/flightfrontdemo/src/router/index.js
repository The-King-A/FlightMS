import { createRouter, createWebHistory } from 'vue-router'
import HomeScreen from '../views/HomeScreen.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import NotFound from '../views/NotFound.vue'
import MainLayout from '../layouts/MainLayout.vue'
import Cookies from 'js-cookie'

// 登录状态检查
const checkAuth = () => {
  return Cookies.get('token') !== undefined
}

const routes = [
  {
    path: '/',
    redirect: '/login',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'home',
        component: HomeScreen
      },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'flights',
        name: 'flights',
        component: () => import('../views/flight/FlightList.vue')
      },
      {
        path: 'flights/add',
        name: 'add-flight',
        component: () => import('../views/flight/AddFlight.vue')
      },
      {
        path: 'flights/edit/:id',
        name: 'edit-flight',
        component: () => import('../views/flight/EditFlight.vue'),
        props: true
      },
      {
        path: 'statistics',
        name: 'statistics',
        component: () => import('../views/Statistics.vue')
      }
    ]
  },
  {
    path: '/login',
    name: 'login',
    component: Login
  },
  {
    path: '/register',
    name: 'register',
    component: Register
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFound
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局导航守卫
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 需要认证的路由
    if (!checkAuth()) {
      next({ name: 'login' })
    } else {
      next()
    }
  } else {
    // 已登录用户访问登录/注册页
    if ((to.name === 'login' || to.name === 'register') && checkAuth()) {
      next({ name: 'home' })
    } else {
      next()
    }
  }
})

export default router 