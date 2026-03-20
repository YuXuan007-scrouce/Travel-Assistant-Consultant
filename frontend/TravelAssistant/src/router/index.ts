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
  // 1. 设置页面标题 (从 meta 中读取)
  const title = to.meta.title ? `${to.meta.title} - 智能旅游助手` : '智能旅游助手';
  document.title = title as string;

  // 2. 鉴权逻辑 (这里仅为示例，需配合 Pinia)
  const isAuthenticated = !!localStorage.getItem('authorization'); // 实际开发建议从 store 获取

  // 如果访问的不是登录页，且没有 token，则强制跳转到登录
  if (to.name !== 'Login' && !isAuthenticated) {
    return { name: 'Login' };
  } else if (to.name === 'Login' && isAuthenticated) {
    // 如果已登录还想去登录页，直接拉回首页/助手页
    return { name: 'TravelAssistant' };
  } else {
    return true; // 正常放行
  }
});

/**
 * 全局后置守卫
 */
router.afterEach((to, from) => {

    NProgress.done();
  console.log(`成功进入页面: ${to.meta.title}`);
});

export default router;