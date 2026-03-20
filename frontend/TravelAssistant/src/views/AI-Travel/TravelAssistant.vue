<template>
<div id="app" class="chat-page bg-gray-50" :class="{ 'bg-gray-800': darkMode }">
    <!-- 顶部导航栏 -->
     <header class="chat-header" :class="{ 'dark': darkMode }">
  <!-- 左侧：返回 + 标题 -->
         <div class="header-left">
              <img class="back-icon" src="@/assets/goBack.svg" @click="goBack" />
              <span class="header-title">湖南旅游顾问</span>
         </div>
  <!-- 右侧：功能按钮 -->
          <div class="header-right">
               <button
                   class="icon-btn add-btn" @click="startNewConversation">
                   <van-icon name="add" size="20" />
              </button>
               <button class="icon-btn" @click="toggleDarkMode">
               <van-icon :name="darkMode ? 'eye' : 'browsing-history'" size="20" />
             </button>
           </div>
     </header>

    <!-- 聊天内容区域 -->
    <main class="chat-main" ref="chatContainer" :class="{ 'bg-gray-800': darkMode }">
      <div v-for="(message, index) in messages" :key="index" class="max-w-3xl mx-auto">
        <div :class="['flex', message.role === 'user' ? 'justify-end' : 'justify-start']">
          <div :class="['flex items-start space-x-3', message.role === 'user' ? 'flex-row-reverse space-x-reverse' : '']">
            <div :class="['w-8 h-8 rounded-full flex-shrink-0  flex items-center overflow-hidden justify-center',
                          message.role === 'user' 
                            ? (darkMode ? 'bg-blue-700 text-blue-200' : 'bg-blue-100 text-blue-600')
                            : (darkMode ? 'bg-green-700 text-green-200' : 'bg-green-100 text-green-600')]">
               <img :src="message.role === 'user' ? userIconUrl : robotIconUrl"
                                class="w-full h-full object-cover"/>
            </div>
            <div :class="['p-3 rounded-lg max-w-lg',
                         message.role === 'user'
                           ? 'bg-blue-500 text-white'
                           : darkMode
                             ? 'bg-gray-700 text-gray-100 border-gray-600'
                             : 'bg-white shadow border border-gray-100']">
              <div v-if="message.role === 'assistant' && message.isLoading" class="flex space-x-2">
                <div :class="['w-2 h-2 rounded-full', darkMode ? 'bg-gray-400' : 'bg-gray-300', 'animate-pulse']"></div>
                <div :class="['w-2 h-2 rounded-full', darkMode ? 'bg-gray-400' : 'bg-gray-300', 'animate-pulse delay-100']"></div>
                <div :class="['w-2 h-2 rounded-full', darkMode ? 'bg-gray-400' : 'bg-gray-300', 'animate-pulse delay-200']"></div>
              </div>
              <div v-else class="whitespace-pre-wrap">
  {{ message.fullContent.slice(0, message.visibleChars) }}
  <span v-if="message.isStreaming" class="typing-cursor"></span>
</div>

            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 输入框区域 -->
    <footer class="chat-footer">
      <div class="max-w-3xl mx-auto relative">
        <div class="flex items-center">
          <!-- 用户按下“单独的 Enter 键”，并阻止默认换行 → 执行 sendMessage -->
          <textarea
            v-model="userInput"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.ctrl.enter.exact.prevent="sendMessage"
            @keydown.esc.exact="stopResponse"
            placeholder="输入您的问题..."
            :class="['flex-1 border rounded-lg py-3 px-4 pr-12 focus:outline-none focus:ring-2 resize-none',
                    darkMode
                      ? 'bg-gray-700 border-gray-600 text-white focus:ring-blue-400 placeholder-gray-400'
                      : 'border-gray-300 focus:ring-blue-500 focus:border-transparent']"
            rows="1"
            ref="textarea"
            @input="adjustTextareaHeight"
          ></textarea>
          
          <button
            @click="isLoading ? stopResponse() : sendMessage()"
            :disabled="!userInput.trim() && !isLoading"
            :class="['ml-2 p-3 rounded-lg',
                    isLoading
                      ? 'bg-red-500 hover:bg-red-600 text-white'
                      : 'bg-blue-500 hover:bg-blue-600 text-white',
                    'disabled:opacity-50 disabled:cursor-not-allowed']"
          >
            <van-icon :name="isLoading ? 'stop-circle' : 'share'"/>
          </button>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import chatHttp from '@/utils/http/aiCustomer'

const router = useRouter();
//跳回之前的页面
const goBack = () => {
  router.back();
}

// 所有响应式变量
const messages = ref([]);
const userInput = ref('');
const isLoading = ref(false);
const chatContainer = ref(null);
const textarea = ref(null);
const darkMode = ref(false);

const memoryId = ref(Date.now().toString());  // 用于会话记忆的 ID

let controller = null;
let typingInterval = null;


//聊天内容框 角色图标
const robotIconUrl = "/AIcustomer.svg"
const userIconUrl = "/userIcon.png" 

// 所有函数定义
const adjustTextareaHeight = () => {
  const textareaEl = textarea.value;
  if (!textareaEl) return;
  textareaEl.style.height = 'auto';         //把高度重置，防止内容删除后 textarea 不能变小
  textareaEl.style.height = `${Math.min(textareaEl.scrollHeight, 200)}px`; //让textarea的高度=内容的高度（scrollHeight）,不超过200px
};


//保证每次新消息出现时，聊天窗口自动滚到底部
const scrollToBottom = () => {   
  nextTick(() => {
    if (chatContainer.value) {   //确认聊天窗口的 DOM 存在。
      //当前滚动位置 = 内容总高度   scrollHeight与scrollHeight是DOM自带的！
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
};

const toggleDarkMode = () => {
  darkMode.value = !darkMode.value;
  localStorage.setItem('darkMode', darkMode.value.toString());
};

const startTypingEffect = (messageIndex) => {
  const msg = messages.value[messageIndex];
  if (!msg) return;

  if (msg.visibleChars < msg.fullContent.length) {
    msg.visibleChars++;
    scrollToBottom();
  } else if (!msg.isStreaming) {
    clearInterval(typingInterval);
    typingInterval = null;
  }
};


//新建对话 按钮
const startNewConversation = () => {
  messages.value = [];
  // 通过时间戳，生成新的 memoryId
  memoryId.value = Date.now().toString();
  
  // 添加初始化，欢迎消息
  messages.value.push({
    role: 'assistant',
    fullContent: '亲，您好！我是湖南旅游顾问，有什么能帮助到您？',
    isLoading: false,
    visibleChars: 0,
    isStreaming: false
  });
  
  messages.value[0].visibleChars = messages.value[0].fullContent.length;
  scrollToBottom();   //滚动到底部
  
   // 聚焦输入框
  nextTick(() => {
    if (textarea.value) {
      //浏览器原生函数 让输入框自动获得光标，用户可以继续打字，而不需要用鼠标点一下
      textarea.value.focus();
    }
  });
};

const sendMessage = async () => {
  // sendMessage 函数实现
  if (!userInput.value.trim() || isLoading.value) return;

  // 终止之前的请求
  if (controller) {
    controller.abort();
  }
  controller = new AbortController();

  const userMessage = {
    role: 'user',
    fullContent: userInput.value.trim(),        //trim() 去掉前后空格
    isLoading: false,
    visibleChars: userInput.value.trim().length,
    isStreaming: false
  };

  messages.value.push(userMessage);  //把这条消息加入到聊天记录数组中messages[]

  const assistantMessage = {
    role: 'assistant',
    fullContent: '',     // 新增：流式只往这里塞
    isLoading: true,
    visibleChars: 0,     //当前显示到哪里
    isStreaming: true
  };

  messages.value.push(assistantMessage);

  userInput.value = '';
  adjustTextareaHeight();
  scrollToBottom();
  isLoading.value = true;

  // 湖南有什么好玩的经典？

  //  介绍一下岳麓山

  //  岳麓山明天上午，剩余名额还有多少？

  //   帮我预约岳麓山明天上午2人，张三，13800138000"
  try {
    let previousLength = 0; // axios 中用于增量解析
    let messageIndex = messages.value.length - 1; // 助手消息的索引

    //  核心新增：定义一个缓冲区，用于拼接不完整的网络包
    let buffer = '';

    // axios 请求（后端仍然采用文本流 SSE 或 chunk）
    const response = await chatHttp.get("/travel/chat", 
      {
        message: userMessage.fullContent,
        conversationId: memoryId.value
      },
      {
      signal: controller.signal,
      responseType: "text",  // 关键！用 text 获取渐进数据
      timeout: 120000,            // 禁用超时
      onDownloadProgress: (progressEvent) => {
        // axios 的 responseText 可以从 event.target.responseText 中获取
        const xhr = progressEvent.event.target;

        if (!xhr || !xhr.responseText) return;

        const text = xhr.responseText;   // 从 event.target.responseText 中获取渐进数据

        // 只处理新增部分
        const chunk = text.substring(previousLength);
        previousLength = text.length;        

        if (!chunk) return;
        // 将 chunk 按行分割，过滤掉 data: 前缀和空行
  const lines = chunk.split('\n');
  let cleanText = '';
  
  lines.forEach(line => {
    if (line.startsWith('data:')) {
      // 提取 data: 之后的内容并去除前后的空白（视后端返回情况而定）
      cleanText += line.replace(/^data:/, '').trim();
    }
  });

  if (!cleanText) return;

  // 更新内容
  messages.value[messageIndex].fullContent += cleanText;

        messages.value[messageIndex].isLoading = false;

        // 启动打字效果
        if (!typingInterval) {
          typingInterval = setInterval(() => {
            startTypingEffect(messageIndex);
          }, 80);
        }

        scrollToBottom();
      }
    }
    );

  } catch (error) {
    if (axios.isCancel(error) || error.name === "CanceledError") {
      console.log("请求中断");
    } else {
      console.error("请求失败：", error);

      const lastMsg = messages.value[messages.value.length - 1];
      lastMsg.fullContent  = "抱歉，请求过程中出现错误：" + error.message;
      lastMsg.visibleChars = lastMsg.fullContent .length;
    }
  } finally {
    const lastMsg = messages.value[messages.value.length - 1];
    lastMsg.isLoading = false;
    lastMsg.isStreaming = false;

    if (lastMsg.visibleChars < lastMsg.fullContent.length) {
      lastMsg.visibleChars = lastMsg.fullContent.length;
    }

    isLoading.value = false;
    controller = null;

    if (typingInterval) {
      clearInterval(typingInterval);
      typingInterval = null;
    }

    scrollToBottom();
  }
};


const stopResponse = () => {
  if (controller) {
    controller.abort();
    const lastMessage = messages.value[messages.value.length - 1];
    lastMessage.isLoading = false;
    lastMessage.isStreaming = false;

    if (lastMessage.visibleChars < lastMessage.fullContent .length) {
      lastMessage.visibleChars = lastMessage.fullContent .length;
    }

    isLoading.value = false;
    controller = null;

    if (typingInterval) {
      clearInterval(typingInterval);
      typingInterval = null;
    }
  }
};

// 生命周期和监听
onMounted(() => {
  darkMode.value = localStorage.getItem('darkMode') === 'true' ||
    (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches);

  messages.value.push({
    role: 'assistant',
    fullContent: '亲，您好！我是湖南旅游顾问，有什么能帮助到您？',
    isLoading: false,
    visibleChars: 0,
    isStreaming: false
  });

  messages.value[0].visibleChars = messages.value[0].fullContent.length;
  scrollToBottom();

  nextTick(() => {
    if (textarea.value) {
      textarea.value.focus();
    }
  });
  //键盘弹出时，调整聊天内容区域高度
   if (window.visualViewport) {
    const footerEl = document.querySelector('.chat-footer');

    window.visualViewport.addEventListener('resize', () => {
      const offset =
        window.innerHeight - window.visualViewport.height;

      footerEl.style.bottom =
        offset > 0 ? `${offset}px` : '0px';
    });
  }
});

watch(messages, scrollToBottom, { deep: true });

// 所有顶层变量和函数自动暴露给模板，无需 return
</script>
<style scoped>
/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
}
::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
/* 输入框自适应高度 */
textarea {
  min-height: 44px;
  max-height: 200px;
  transition: height 0.2s;
}
/* 加载动画 */
@keyframes pulse {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

.animate-pulse {
  animation: pulse 1.5s infinite;
}
.delay-100 {
  animation-delay: 0.1s;
}
.delay-200 {
  animation-delay: 0.2s;
}

/* 打字机效果 */
.typing-cursor::after {
  content: "|";
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  from, to {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

/* 淡入效果 */
.fade-in {
  opacity: 1;
  transition: opacity 0.1s;
}
/*固定头部样式*/
.chat-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  padding: 0 12px;

  display: flex;
  align-items: center;
  justify-content: space-between;

  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  z-index: 1000;
}
/* 暗色模式 */
.chat-header.dark {
  background: #374151;
  border-bottom-color: #4b5563;
}
/* 左侧 */
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.back-icon {
  width: 20px;
  height: 20px;
  cursor: pointer;
}
.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #2563eb;
}
.chat-header.dark .header-title {
  color: #93c5fd;
}
/* 右侧 */
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.chat-header.dark .icon-btn {
  background: #4b5563;
  color: #e5e7eb;
}
.add-btn {
  background: #22c55e;
  color: #ffffff;
}
/*输入框*/
.chat-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: white;

  /* 适配 iPhone 安全区 */
  padding-bottom: env(safe-area-inset-bottom);
}
/*中间滚动*/
.chat-main {
  position: fixed;
  top: 56px;          /* header 高度 */
  bottom: 72px;       /* footer 高度 */
  left: 0;
  right: 0;

  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: 16px;
}
/* 在 style scoped 底部添加 */
.w-8 { width: 32px !important; }
.h-8 { height: 32px !important; }
.rounded-full { border-radius: 50% !important; }
.flex-shrink-0 { flex-shrink: 0 !important; }

/* 强制限制聊天气泡内的图片 */
.chat-main img {
  max-width: 100%;
  object-fit: cover;
}

/* 确保头像容器大小固定 */
.flex-shrink-0.w-8.h-8 {
  width: 32px;
  height: 32px;
  min-width: 32px;
}

</style>