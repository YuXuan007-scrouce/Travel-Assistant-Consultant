<template>
  <div class="login-page">
    <div class="h-[25vh] flex flex-col justify-center items-center">
      <van-image round width="25vw" height="25vw" :src="defaultAvatarUrl" />
    </div>

    <van-tabs v-model:active="activeTab" class="mb-6" color="#1989fa">
      <van-tab title="验证码登录" name="sms" />
      <van-tab title="密码登录" name="password" />
    </van-tabs>

    <van-form @submit="onSubmit">
      <van-field
        v-model="loginInfo.phone"
        label="手机号"
        placeholder="请输入手机号"
        type="tel"
        maxlength="11"
        clearable
        :rules="mobileRules"
      />

      <van-field
        v-if="activeTab === 'password'"
        v-model="loginInfo.password"
        label="密码"
        type="password"
        placeholder="请输入密码"
        clearable
        :rules="passwordRules"
      />

      <van-field
        v-if="activeTab === 'sms'"
        v-model="loginInfo.code"
        label="验证码"
        placeholder="请输入验证码"
        clearable
        :rules="codeRules"
      >
        <template #button>
          <van-button
            size="small"
            type="primary"
            :disabled="smsLoading"
            @click="sendSms"
          >
            {{ smsLoading ? `${countdown}s` : "获取验证码" }}
          </van-button>
        </template>
      </van-field>

      <div class="btn-wrapper">
        <van-button
          round
          block
          type="primary"
          native-type="submit"
          :loading="loginLoading"
        >
          登录
        </van-button>
      </div>
    </van-form>
    <div class="login-footer">
    <el-button type="text" @click="goToRegister">还没有账号？立即注册</el-button>
  </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { showToast } from "vant";
import { useRouter } from "vue-router";
import { getSmsCode } from "../../api/login/index";
import { useUserStore } from "../../store/user";
import { getToken } from "../../utils/token";

const defaultAvatarUrl = "/dav.png";
const router = useRouter();
const userStore = useUserStore();

// 当前激活的标签页：'sms' 或 'password'
const activeTab = ref('sms');

/* 表单数据 */
const loginInfo = reactive({
  phone: "",
  code: "",
  password: ""
});

/* 状态控制 */
const smsLoading = ref(false);
const loginLoading = ref(false);
const countdown = ref(60);
let timer: number | null = null;

/* 校验规则 */
const mobileRules = [
  { required: true, message: "账号不能为空" },
  { validator: (val: string) => /^1\d{10}$/.test(val), message: "请输入11位手机号" }
];

const passwordRules = [
  { required: true, message: "密码不能为空" },
  { min: 6, message: "密码长度不能少于6位" }
];

const codeRules = [
  { required: true, message: "验证码不能为空" }
];

/* 发送验证码逻辑 */
const sendSms = async () => {
  if (!/^1\d{10}$/.test(loginInfo.phone)) {
    showToast("请先输入正确的手机号");
    return;
  }

  smsLoading.value = true;
  countdown.value = 60;

  try {
    await getSmsCode(loginInfo.phone);
    showToast("验证码已发送");

    timer = window.setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        smsLoading.value = false;
        if (timer) clearInterval(timer);
      }
    }, 1000);
  } catch (error) {
    showToast("验证码发送失败");
    smsLoading.value = false;
  }
};

/* 提交登录 */
const onSubmit = async () => {
  loginLoading.value = true;

  // 根据当前 Tab 封装请求数据
  const loginPayload = {
    phone: loginInfo.phone,
    // 如果是密码登录，验证码传空；反之亦然
    password: activeTab.value === 'password' ? loginInfo.password : "",
    code: activeTab.value === 'sms' ? loginInfo.code : ""
  };

  try {
    await userStore.LoginAction(loginPayload);
    showToast("登录成功");
    router.replace("/travelassisant");
  } catch (error) {
    // 错误处理由 Axios 拦截器或此处捕获
    console.error("Login failed:", error);
  } finally {
    loginLoading.value = false;
  }
};

const goToRegister = () => {
  router.push('/register'); // 确保你的路由表中已配置该路径
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 20px 16px;
  background: #f7f8fa;
}
.btn-wrapper {
  margin: 40px 16px 0;
}
.mb-6 {
  margin-bottom: 24px;
}
</style>