<template>
  <div class="login-container">
    <div class="login-box">
      <h2>账户登录</h2>
      <form @submit.prevent="handleSubmit">
        <div class="input-group">
          <div class="input-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="input-icon">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <input
                type="text"
                id="username"
                v-model="username"
                placeholder="请输入用户名"
                required
            />
          </div>
        </div>
        <div class="input-group">
          <div class="input-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="input-icon">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
            <input
                :type="passwordVisible ? 'text' : 'password'"
                id="password"
                v-model="password"
                placeholder="请输入密码"
                required
            />
            <button
                type="button"
                class="password-toggle"
                @click="togglePasswordVisibility"
            >
              <svg
                  v-if="!passwordVisible"
                  xmlns="http://www.w3.org/2000/svg"
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
              >
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                <line x1="1" y1="1" x2="23" y2="23"></line>
              </svg>
              <svg
                  v-else
                  xmlns="http://www.w3.org/2000/svg"
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
              >
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
            </button>
          </div>
        </div>
        <div class="input-group" v-if="loginError">
          <div class="error-message">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="error-icon">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ loginErrorMessage }}
          </div>
        </div>
        <div class="options">
          <label class="remember-me">
            <input
                type="checkbox"
                v-model="remember"
            />
            记住我
          </label>
          <span class="forgot-password" @click="showPasswordModal = true">忘记密码?</span>
        </div>
        <button type="submit" class="login-button" :disabled="isLoading">
          {{ isLoading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p class="register-link">
        还没有账号？<a href="/register">立即注册</a>
      </p>
    </div>
    <!-- 弹窗 -->
    <div v-if="showPasswordModal" class="modal">
      <div class="modal-content">
        <h2>修改密码</h2>
        <form @submit.prevent="handlePasswordChange">
          <!-- 账号输入 -->
          <div class="form-group">
            <label for="account" class="form-label">账号:</label>
            <input
                type="text"
                id="account"
                v-model="account"
                required
                class="input-field"
                placeholder="请输入账号"
            />
          </div>

          <!-- 新密码输入 -->
          <div class="form-group">
            <label for="newPassword" class="form-label">新密码:</label>
            <input
                id="newPassword"
                v-model="newPassword"
                required
                class="input-field"
                placeholder="请输入新密码"
            />
            <small v-if="newPassword && newPassword.length < 8" class="error-text">
              密码长度至少为8个字符
            </small>
          </div>

          <!-- 确认密码输入 -->
          <div class="form-group">
            <label for="confirmPassword" class="form-label">确认密码:</label>
            <input
                id="confirmPassword"
                v-model="confirmPassword"
                required
                class="input-field"
                placeholder="确认新密码"
            />
            <small v-if="confirmPassword && confirmPassword !== newPassword" class="error-text">
              两次密码输入不一致
            </small>
          </div>

          <!-- 提交按钮 -->
          <button type="submit" class="submit-btn" :disabled="isSubmitDisabled">提交</button>
        </form>

        <button @click="closeModal" class="close-btn">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, watch, computed} from 'vue'
import axios from 'axios'
import {useRouter} from "vue-router";
import {useUserStore} from "@/api/app.js"; // 假设使用axios发送请求

const router = useRouter();

// 加密工具函数
const encryptData = (data) => {
  try {
    return btoa(encodeURIComponent(data))
  } catch (error) {
    console.error('加密失败', error)
    return null
  }
}

const decryptData = (encryptedData) => {
  try {
    return decodeURIComponent(atob(encryptedData))
  } catch (error) {
    console.error('解密失败', error)
    return null
  }
}

const username = ref('')
const password = ref('')
const remember = ref(false)
const passwordVisible = ref(false)
const loginError = ref(false)
const loginErrorMessage = ref('')
const isLoading = ref(false)

// 清理本地存储
const clearLocalStorage = () => {
  localStorage.removeItem('rememberedUsername')
  localStorage.removeItem('rememberedPassword')
  localStorage.removeItem('credentialsExpiry')
}

// 保存用户信息到安全存储
const saveUserCredentials = () => {
  if (remember.value) {
    try {
      // 加密并存储用户信息
      const encryptedUsername = encryptData(username.value)
      const encryptedPassword = encryptData(password.value)

      // 存储加密信息和过期时间
      localStorage.setItem('rememberedUsername', encryptedUsername)
      localStorage.setItem('rememberedPassword', encryptedPassword)
      localStorage.setItem('credentialsExpiry',
          new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString()
      )
    } catch (error) {
      console.error('保存凭据失败', error)
    }
  } else {
    clearLocalStorage()
  }
}

// 检查并恢复记住的用户信息
const checkRememberedCredentials = () => {
  try {
    const encryptedUsername = localStorage.getItem('rememberedUsername')
    const encryptedPassword = localStorage.getItem('rememberedPassword')
    const expiryDate = localStorage.getItem('credentialsExpiry')

    if (encryptedUsername && encryptedPassword && expiryDate) {
      const now = new Date()
      const expiry = new Date(expiryDate)

      if (now < expiry) {
        // 解密并恢复用户信息
        const decryptedUsername = decryptData(encryptedUsername)
        const decryptedPassword = decryptData(encryptedPassword)

        if (decryptedUsername && decryptedPassword) {
          username.value = decryptedUsername
          password.value = decryptedPassword
          remember.value = true
        }
      } else {
        clearLocalStorage()
      }
    }
  } catch (error) {
    console.error('恢复凭据失败', error)
  }
}


const user=useUserStore()
// 登录提交处理
const handleSubmit = async () => {
// 重置错误状态
  loginError.value = false
  loginErrorMessage.value = ''
  isLoading.value = true

  try {
    // 模拟登录请求
    const response = await axios.post('http://localhost:8080/users/login', {
      userName: username.value,
      password: password.value
    })

    // 登录成功
    if (response.data.status === "success") {
      // 保存记住我的信息
      saveUserCredentials()
      localStorage.setItem("token",response.data.data.token)
      localStorage.setItem("refreshToken",response.data.data.refreshToken)
      const userId=response.data.data.userId
      user.setId(userId)
      // 跳转或其他成功处理逻辑
      console.log('登录成功')
      await router.push({
        path:'/',
        query:{
          userId: userId
        }})
    } else {
      // 登录失败
      throw new Error( '登录失败！问题是：'+response.data.message)
    }
  } catch (error) {
    // 登录失败处理
    loginError.value = true
    loginErrorMessage.value = error.message || '网络错误，请稍后重试'

    // 清除本地存储
    clearLocalStorage()

    // 重置记住我状态
    remember.value = false

    // 清空密码
    password.value = ''
  } finally {
    isLoading.value = false
  }
}

// 切换密码可见性
const togglePasswordVisibility = () => {
  passwordVisible.value = !passwordVisible.value
}

// 组件挂载时检查记住的用户
onMounted(checkRememberedCredentials)

// 监听记住我状态变化
watch(remember, (newValue) => {
  if (!newValue) {
    clearLocalStorage()
  }
})

// 定义响应式变量
const showPasswordModal = ref(false);  // 控制弹窗显示
const account = ref('');               // 账号
const newPassword = ref('');           // 新密码
const confirmPassword = ref('');       // 确认密码

// 计算属性，用于判断提交按钮是否禁用
const isSubmitDisabled = computed(() => {
  return (
      !account.value ||
      newPassword.value.length < 8 ||
      confirmPassword.value !== newPassword.value
  );
});

// 密码修改的处理逻辑
const handlePasswordChange = async () => {
  try {
    // 这里可以发送请求到后端修改密码
    const res = await axios.post('http://localhost:8080/users/password', {
      userName: account.value,
      password: newPassword.value,
    })
    console.log("/users/password", res.data)
    if (res.data.status === "success") {
      alert(`账号：${account.value}，密码修改成功！`);
      closeModal(); // 关闭弹窗
    }
    else {
      alert('输入的用户不存在！');
    }
  }catch(error) {
    alert('服务器出现问题：',error.message)
  }

};

// 关闭弹窗
const closeModal = () => {
  showPasswordModal.value = false;
  account.value = '';
  newPassword.value = '';
  confirmPassword.value = '';
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.login-box {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 12px;
  color: #888;
  z-index: 10;
}

.input-group input {
  width: 100%;
  padding: 12px 12px 12px 48px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  box-sizing: border-box;
}

.password-toggle {
  position: absolute;
  right: 12px;
  background: none;
  border: none;
  cursor: pointer;
  color: #888;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.remember-me {
  display: flex;
  align-items: center;
  color: #666;
}

.remember-me input {
  margin-right: 8px;
}

.forgot-password {
  color: #007bff;
  text-decoration: none;
  font-size: 14px;
}

.forgot-password:hover {
  text-decoration: underline;
}

.login-button {
  width: 100%;
  padding: 12px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.login-button:hover {
  background-color: #0056b3;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.register-link a {
  color: #007bff;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}

.error-message {
  display: flex;
  align-items: center;
  background-color: #fff0f0;
  color: #ff4d4f;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.error-icon {
  margin-right: 10px;
  stroke: #ff4d4f;
}

.login-button:disabled {
  background-color: #a0a0a0;
  cursor: not-allowed;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  width: 350px;
  text-align: center;
}

/* 输入框和标签并排样式 */
.form-group {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.form-label {
  width: 100px;
  margin-right: 10px;
  text-align: right;
  font-weight: bold;
}

.input-field {
  padding: 10px;
  width: 100%;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

/* 提交按钮样式 */
.submit-btn {
  margin-top: 10px;
  padding: 10px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.submit-btn:disabled {
  background-color: #aaa;
  cursor: not-allowed;
}

/* 关闭按钮样式 */
.close-btn {
  margin-top: 10px;
  padding: 8px 16px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

/* 错误提示样式 */
.error-text {
  color: red;
  font-size: 12px;
  margin-top: 5px;
}

small {
  font-size: 12px;
}
</style>