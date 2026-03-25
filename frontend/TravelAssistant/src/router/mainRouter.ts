import type { RouteRecordRaw } from "vue-router";

const otherRoutes: Array<RouteRecordRaw> = [
  // 1. 添加根路由重定向，解决 "No match for /" 报错
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("../views/login/login.vue"),
    meta: {
      title: "登录",
      noCache: true
    }
  },
    {
    path: "/travelassisant",
    name: "TravelAssisant.vue",
    component: () => import("../views/AI-Travel/TravelAssistant.vue"),
    meta: {
      title: "旅游顾问",
      noCache: true
    }
  },
  {
     path: '/register',
     name: 'Register',
     component: () => import('@/views/login/Register.vue'),
      meta: {
      title: "注册",
      noCache: true
    }
}
]

export default otherRoutes;