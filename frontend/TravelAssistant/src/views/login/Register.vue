<template>
  <div class="register-container">
    <van-nav-bar
      title="用户注册"
      left-text="返回"
      left-arrow
      @click-left="$router.back()"
    />

    <div class="register-content">
      <van-form @submit="handleRegister">
        <van-cell-group inset>
          <van-field
            v-model="form.nickName"
            label="昵称"
            placeholder="请输入您的昵称"
            left-icon="user-o"
            :rules="[{ required: true, message: '请填写昵称' }]"
          />
          
          <van-field
            v-model="form.phone"
            label="手机号"
            placeholder="请输入您的手机号"
            left-icon="phone-o"
            type="tel"
            :rules="phoneRules"
          />

          <van-field
            v-model="form.password"
            :type="passwordVisible ? 'text' : 'password'"
            label="密码"
            placeholder="请输入您的密码"
            left-icon="lock"
            :right-icon="passwordVisible ? 'eye-o' : 'closed-eye'"
            @click-right-icon="passwordVisible = !passwordVisible"
            :rules="passwordRules"
          />
        </van-cell-group>

        <div style="margin: 32px 16px;">
          <van-button 
            round 
            block 
            type="primary" 
            native-type="submit" 
            :loading="submitting"
            loading-text="注册中..."
          >
            立即注册
          </van-button>
        </div>
      </van-form>

      <div class="login-link">
        已有账号？ <van-button plain type="primary" size="small" to="/login">去登录</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showFailToast, type FieldRule } from 'vant';
import { register } from '@/api/login/index'; 
import { registerInterface } from '@/api/login/type';

const router = useRouter();
const submitting = ref(false);
const passwordVisible = ref(false);

// 初始化表单数据
const form = reactive<registerInterface>({
  nickName: '',
  phone: '',
  password: ''
});

// --- 表单校验规则 ---
const phoneRules: FieldRule[] = [
  { required: true, message: '请填写手机号' },
  { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误' }
];

const passwordRules: FieldRule[] = [
  { required: true, message: '请填写密码' },
  { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/, message: '密码至少8位，需包含字母和数字' }
];

// --- 提交注册逻辑 ---
const handleRegister = async () => {
  submitting.value = true;
  try {
   
    const res = await register(form);
    
    // 根据响应判断
    if (res.code === 200) {
      showSuccessToast('注册成功！');
      // 注册成功后跳转登录
      router.push('/login');
    } else {
      showFailToast(res.errorMsg || '注册失败');
    }
  } catch (error) {
    console.error('注册请求异常:', error);
    showFailToast('网络异常，请稍后再试');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background-color: #f7f8fa; /* Vant 的背景色 */
}

.register-content {
  padding-top: 20px;
}

.login-link {
  text-align: center;
  font-size: 14px;
  color: #646566;
  margin-top: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
</style>