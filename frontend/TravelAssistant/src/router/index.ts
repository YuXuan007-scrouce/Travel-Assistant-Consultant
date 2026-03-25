import { createRouter, createWebHistory } from 'vue-router';
import otherRoutes from './mainRouter'; 
import NProgress from "../utils/progress";

// import { useUserStore } from '@/store/user'; 

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' }, // 默认重定向到登录
    ...otherRoutes
  ]
});

/**
 * 全局前置守卫：登录拦截
 */
router.beforeEach((to, from) => {
  NProgress.start();

  // 1. 设置页面标题
  const title = to.meta.title ? `${to.meta.title} - 智能旅游助手` : '智能旅游助手';
  document.title = title as string;

  // 2. 鉴权逻辑
  const isAuthenticated = !!localStorage.getItem('authorization');

  // --- 定义白名单 (不需要登录也能访问的路由名称) ---
  const whiteList = ['Login', 'Register']; 

  // 情况 A: 访问的是白名单页面
  if (whiteList.includes(to.name as string)) {
    // 如果已经登录了，还想去登录或注册页，建议直接重定向到首页 (Dashboard/Home)
    if (isAuthenticated) {
      return { name: 'TravelAssisant' }; 
    }
    return true; // 未登录访问白名单，直接放行
  }

  // 情况 B: 访问的是需要权限的页面，且未登录
  if (!isAuthenticated) {
    return { name: 'Login' };
  }

  // 情况 C: 已登录且访问的是权限页面
  return true;
});

/**
 * 全局后置守卫
 */
router.afterEach((to, from) => {

    NProgress.done();
  console.log(`成功进入页面: ${to.meta.title}`);
});

export default router;