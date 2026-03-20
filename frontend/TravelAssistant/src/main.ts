import { createApp } from 'vue'
//import './style.css'
import App from './App.vue'
import {createPinia} from "pinia";
import router from './router';
import Vant from "vant";

// 1. 基础重置
import "normalize.css/normalize.css";
// 2. 组件库样式
import "vant/lib/index.css"; 
// 3. 项目全局样式 (包含 tailwind 和自定义样式)
import "./styles/index.ts";

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(Vant);

app.mount('#app')
