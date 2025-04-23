<template>
  <div class="register-container">
    <div class="register-wrapper">
      <div class="register-box">
        <div class="register-header">
          <h2>用户注册</h2>
          <p>创建您的个人账号</p>
        </div>

        <form @submit.prevent="handleRegister" class="register-form">
          <!-- 头像上传 -->
          <div class="form-group avatar-section">
            <div
                class="avatar-upload"
                @click="triggerAvatarUpload"
            >
              <input
                  type="file"
                  ref="avatarInput"
                  @change="handleAvatarUpload"
                  accept="image/*"
                  style="display:none"
              />
              <div class="avatar-preview-container">
                <img
                    v-if="avatarPreview"
                    :src="avatarPreview"
                    alt="头像预览"
                    class="avatar-preview"
                />
                <div v-else class="avatar-placeholder">
                  <svg xmlns="http://www.w3.org/2000/svg" width="50" height="50" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                  <span>点击上传头像</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 表单输入区 -->
          <div class="form-inputs">
            <!-- 用户名输入 -->
            <div class="form-group">
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
                    :class="{'input-error': usernameError}"
                />
              </div>
              <span v-if="usernameError" class="error-text">{{ usernameError }}</span>
            </div>

            <!-- 密码输入 -->
            <div class="form-group">
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
                    :class="{'input-error': passwordError}"
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
              <div class="password-strength">
                <div
                    v-for="n in 5"
                    :key="n"
                    class="strength-bar"
                    :class="[
                  n <= passwordStrength.strength ? passwordStrength.color : '',
                  passwordStrengthColor
                ]"
                ></div>
                <span class="strength-text">
                {{ passwordStrength.text }}
              </span>
              </div>
              <span v-if="passwordError" class="error-text">{{ passwordError }}</span>
            </div>

            <!-- 确认密码 -->
            <div class="form-group">
              <div class="input-wrapper">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="input-icon">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
                <input
                    :type="confirmPasswordVisible ? 'text' : 'password'"
                    id="confirm-password"
                    v-model="confirmPassword"
                    placeholder="请再次输入密码"
                    required
                    :class="{'input-error': confirmPasswordError}"
                />
                <button
                    type="button"
                    class="password-toggle"
                    @click="toggleConfirmPasswordVisibility"
                >
                  <svg
                      v-if="!confirmPasswordVisible"
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
              <span v-if="confirmPasswordError" class="error-text">{{ confirmPasswordError }}</span>
            </div>
          </div>

          <!-- 提交按钮 -->
          <div class="form-group submit-section">
            <button
                type="submit"
                class="register-button"
                :disabled="!isFormValid"
            >
              立即注册
            </button>
          </div>
        </form>

        <div class="login-link">
          <p>已有账号？<a href="/login">立即登录</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import axios from 'axios'
import router from "../router/index.js";

// 状态管理
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const avatarFile = ref(null)
const avatarPreview = ref(null)
const avatarURL = ref('')

// 密码可见性
const passwordVisible = ref(false)
const confirmPasswordVisible = ref(false)

// 错误信息
const usernameError = ref('')

// 密码强度相关
const passwordStrength = ref({ strength: 0, text: '密码强度', color: '' })

// 头像上传相关
const avatarInput = ref(null)

// 触发文件选择
const triggerAvatarUpload = () => {
  avatarInput.value.click()
}

// 处理头像上传
const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (file) {
    // 验证文件类型和大小
    const validTypes = ['image/jpeg', 'image/png', 'image/gif','image/jpg']
    const maxSize = 5 * 1024 * 1024 // 5MB

    if (!validTypes.includes(file.type)) {
      alert('请上传有效的图片格式(jpeg/png/gif/jpg)')
      return
    }

    if (file.size > maxSize) {
      alert('图片大小不能超过5MB')
      return
    }

    // 预览头像
    const reader = new FileReader()
    reader.onload = (e) => {
      avatarPreview.value = e.target.result
      avatarFile.value = file
    }
    reader.readAsDataURL(file)
    try {
      const formData = new FormData();
      formData.append("file", file);
      const response = await axios.post('http://localhost:8080/users/avatar', formData,{headers: {'Content-Type': 'multipart/form-data'}})
      if (response.status === 200) {
        avatarURL.value = response.data
        console.log("上传成功:", response.data);
      }
    }catch(err) {
      console.log(err)
    }
  }
}

// 密码可见性切换
const togglePasswordVisibility = () => {
  passwordVisible.value = !passwordVisible.value
}

const toggleConfirmPasswordVisibility = () => {
  confirmPasswordVisible.value = !confirmPasswordVisible.value
}

// 监听密码强度变化
// 密码强度检测函数
const checkPasswordStrength = (password) => {
  if (!password) return { strength: 0, text: '请输入密码' }

// 密码强度评估规则
  const lengthCheck = password.length >= 8
  const uppercaseCheck = /[A-Z]/.test(password)
  const lowercaseCheck = /[a-z]/.test(password)
  const numberCheck = /[0-9]/.test(password)
  const specialCharCheck = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)

// 计算强度
  let strength = 0
  if (lengthCheck) strength++
  if (uppercaseCheck) strength++
  if (lowercaseCheck) strength++
  if (numberCheck) strength++
  if (specialCharCheck) strength++

// 根据强度返回不同的文案和样式
  let text = ''
  let color = ''

  switch (true) {
    case strength <= 1:
      text = '密码强度：非常弱'
      color = 'weak'
      break
    case strength <= 2:
      text = '密码强度：较弱'
      color = 'weak'
      break
    case strength <= 3:
      text = '密码强度：中等'
      color = 'medium'
      break
    case strength <= 4:
      text = '密码强度：强'
      color = 'strong'
      break
    default:
      text = '密码强度：非常强'
      color = 'strong'
  }

  return { strength, text, color }
}
// 监听密码变化
watch(password, (newPassword) => {
  passwordStrength.value = checkPasswordStrength(newPassword)
})

// 密码确认验证
const passwordError = computed(() => {
  if (password.value && password.value.length < 8) {
    return '密码长度至少8位'
  }
  return ''
})

// 确认密码验证
const confirmPasswordError = computed(() => {
  if (confirmPassword.value && password.value !== confirmPassword.value) {
    return '两次输入的密码不一致'
  }
  return ''
})

// 用户名唯一性检查
const checkUsernameUnique = async () => {
  try {
    const response = await axios.get("http://localhost:8080/users/check-username",{
      params: { username: username.value }
    })
    return response.data
  } catch (error) {
    console.error('用户名检查失败', error)
    return false
  }
}

// 用户名验证
watch(username, (newUsername) => {
  if (newUsername.length < 4 || newUsername.length > 16) {
    usernameError.value = '用户名长度应在4-16个字符之间'
  } else {
    usernameError.value = ''
  }
})

// 表单是否有效
const isFormValid = computed(() => {
  return (
      username.value &&
      password.value &&
      confirmPassword.value &&
      !usernameError.value &&
      !passwordError.value &&
      !confirmPasswordError.value&&
      passwordStrength.value.strength > 2
  )
})

// 注册处理
const handleRegister = async (event) => {
// 阻止默认提交行为
  event.preventDefault()

// 表单验证
  if (!isFormValid.value) {
    alert('请检查表单信息是否填写正确')
    return
  }

// 检查用户名唯一性
  let isUnique = await checkUsernameUnique()
  if (!isUnique) {
    usernameError.value = '该用户名已被注册'
    isUnique = false
    return
  }

// 创建userData用于信息上传
  const userData = {
    userName: username.value,
    password: password.value,
    avatar: avatarURL.value // 这里可以放 Base64 编码的图片
  };

  try {
    // 显示加载状态
    const loadingToast = showLoading('正在注册...')

    // 发送注册请求
    const response = await axios.post('http://localhost:8080/users/register', userData, {
      headers: {
        'Content-Type': 'application/json'
      },
      // 添加超时和重试机制
      timeout: 10000
    })

    // 关闭加载状态
    closeLoading(loadingToast)

    // 处理注册响应
    if (response.data) {
      // 注册成功
      showSuccessMessage('注册成功，即将跳转到登录页')

      // 延迟跳转
      setTimeout(() => {
        // 使用路由跳转（假设使用vue-router）
        router.push('/login')

        // 如果没有路由，可以直接跳转
        // window.location.href = '/login'
      }, 2000)
    } else {
      // 注册失败
      showErrorMessage('注册失败')
    }
  } catch (error) {
    // 网络错误处理
    console.error('注册错误', error)

    if (error.response) {
      // 服务器返回错误
      showErrorMessage('服务器错误')
    } else if (error.request) {
      // 请求已发送但无响应
      showErrorMessage('网络连接失败，请检查网络')
    } else {
      // 其他错误
      showErrorMessage('注册过程中发生未知错误')
    }
  }
}

// 辅助通知函数（模拟实现，实际项目中可以使用更专业的通知组件）
const showLoading = (message) => {
// 创建并返回加载提示
  const loadingEl = document.createElement('div')
  loadingEl.className = 'loading-toast'
  loadingEl.textContent = message
  document.body.appendChild(loadingEl)
  return loadingEl
}

const closeLoading = (loadingEl) => {
  if (loadingEl && loadingEl.parentNode) {
    loadingEl.parentNode.removeChild(loadingEl)
  }
}

const showSuccessMessage = (message) => {
  const successEl = document.createElement('div')
  successEl.className = 'success-toast'
  successEl.textContent = message
  document.body.appendChild(successEl)

// 2秒后自动移除
  setTimeout(() => {
    document.body.removeChild(successEl)
  }, 2000)
}

const showErrorMessage = (message) => {
  const errorEl = document.createElement('div')
  errorEl.className = 'error-toast'
  errorEl.textContent = message
  document.body.appendChild(errorEl)

// 3秒后自动移除
  setTimeout(() => {
    document.body.removeChild(errorEl)
  }, 3000)
}

// 添加样式
const styleEl = document.createElement('style')
styleEl.textContent = `
.loading-toast, .success-toast, .error-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 15px 30px;
  border-radius: 8px;
  color: white;
  z-index: 9999;
  text-align: center;
}

.loading-toast {
  background-color: rgba(0, 0, 0, 0.7);
}

.success-toast {
  background-color: #52c41a;
}

.error-toast {
  background-color: #ff4d4f;
}
`
document.head.appendChild(styleEl)
</script>

<style scoped>
/* 响应式全屏背景 */
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f6f8f9 0%, #e5ebee 100%);
  padding: 20px;
  box-sizing: border-box;
}

.register-wrapper {
  width: 100%;
  max-width: 500px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  padding: 30px;
  box-sizing: border-box;
}

.register-header {
  text-align: center;
  margin-bottom: 25px;
}

.register-header h2 {
  color: #333;
  margin-bottom: 10px;
}

.register-header p {
  color: #666;
  font-size: 14px;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group{
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avatar-section {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center;     /* 垂直居中 */
  width: 100%;             /* 占满宽度 */
  margin-bottom: 20px;     /* 与下方内容间距 */
}

.avatar-upload {
  width: 120px;            /* 固定宽度 */
  height: 120px;           /* 固定高度 */
  border-radius: 50%;      /* 圆形 */
  border: 2px dashed #e0e0e0;
  display: flex;           /* 使用flex布局 */
  justify-content: center; /* 水平居中 */
  align-items: center;     /* 垂直居中 */
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;        /* 防止内容溢出 */
}

.avatar-upload:hover {
  border-color: #1890ff;
  box-shadow: 0 0 10px rgba(24,144,255,0.2);
}

.avatar-preview-container {
  width: 100%;             /* 占满父容器 */
  height: 100%;            /* 占满父容器 */
  display: flex;           /* 使用flex布局 */
  justify-content: center; /* 水平居中 */
  align-items: center;     /* 垂直居中 */
}

.avatar-preview {
  max-width: 100%;         /* 最大宽度不超过容器 */
  max-height: 100%;        /* 最大高度不超过容器 */
  object-fit: cover;       /* 保持宽高比 */
  border-radius: 50%;      /* 保持圆形 */
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;     /* 水平居中 */
  justify-content: center; /* 垂直居中 */
  text-align: center;      /* 文字居中 */
  color: #999;
  padding: 10px;
}

.avatar-placeholder svg {
  margin-bottom: 8px;      /* 图标与文字间距 */
}

/* 深色模式头像上传样式 */
@media (prefers-color-scheme: dark) {
  .avatar-upload {
    border-color: #34495e;
    background-color: rgba(52, 73, 94, 0.1);
  }

  .avatar-upload:hover {
    border-color: #3498db;
    box-shadow: 0 0 10px rgba(52, 132, 255, 0.3);
  }

  .avatar-placeholder {
    color: #bdc3c7;
  }
}

/* 响应式调整 */
@media screen and (max-width: 600px) {
  .avatar-upload {
    width: 100px;
    height: 100px;
  }

  .avatar-placeholder svg {
    width: 40px;
    height: 40px;
  }

  .avatar-placeholder span {
    font-size: 12px;
  }
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.input-wrapper input {
  flex-grow: 1;
  padding: 12px 12px 12px 40px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.input-wrapper input:focus {
  border-color: #1890ff;
  outline: none;
  box-shadow: 0 0 5px rgba(24, 144, 255, 0.2);
}

.input-icon {
  position: absolute;
  left: 10px;
  color: #999;
}

.password-toggle {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
}

.register-button {
  width: 100%;
  padding: 12px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.register-button:hover:not(:disabled) {
  background-color: #40a9ff;
}

.register-button:disabled {
  background-color: #a0a0a0;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.login-link a {
  color: #1890ff;
  text-decoration: none;
}

.error-text {
  color: #ff4d4f;
  font-size: 12px;
}

.password-strength {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 5px;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background-color: #e0e0e0;
  border-radius: 2px;
}
.strength-bar.weak.weak {
  background-color: #ff4d4f;
}

.strength-bar.medium.medium {
  background-color: #faad14;
}

.strength-bar.strong.strong {
  background-color: #52c41a;
}

.strength-text {
  font-size: 12px;
  color: #666;
  margin-left: 10px;
}

/* 响应式设计 */
@media screen and (max-width: 600px) {
  .register-wrapper {
    width: 95%;
    padding: 20px;
    margin: 0 auto;
  }

  .register-header h2 {
    font-size: 20px;
  }

  .input-wrapper input {
    padding: 10px 10px 10px 35px;
    font-size: 12px;
  }

  .input-icon {
    width: 20px;
    height: 20px;
  }

  .avatar-upload {
    width: 100px;
    height: 100px;
  }

  .avatar-placeholder svg {
    width: 40px;
    height: 40px;
  }

  .register-button {
    font-size: 14px;
    padding: 10px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .register-container {
    background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%);
  }

  .register-wrapper {
    background-color: #2c3e50;
    box-shadow: 0 10px 25px rgba(255, 255, 255, 0.1);
  }

  .register-header h2 {
    color: #ecf0f1;
  }

  .register-header p {
    color: #bdc3c7;
  }

  .input-wrapper input {
    background-color: #34495e;
    border-color: #2c3e50;
    color: #ecf0f1;
  }

  .input-wrapper input:focus {
    border-color: #3498db;
  }

  .input-icon {
    color: #bdc3c7;
  }

  .password-toggle {
    color: #bdc3c7;
  }

  .register-button {
    background-color: #3498db;
  }

  .register-button:hover:not(:disabled) {
    background-color: #2980b9;
  }

  .login-link a {
    color: #3498db;
  }

  .strength-text {
    color: #bdc3c7;
  }

  .avatar-upload {
    border-color: #34495e;
  }

  .avatar-upload:hover {
    border-color: #3498db;
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.register-wrapper {
  animation: fadeIn 0.5s ease-out;
}

/* 输入验证动画 */
.input-error {
  animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
  transform: translate3d(0, 0, 0);
  backface-visibility: hidden;
  perspective: 1000px;
}

@keyframes shake {
  10%, 90% {
    transform: translate3d(-1px, 0, 0);
  }

  20%, 80% {
    transform: translate3d(2px, 0, 0);
  }

  30%, 50%, 70% {
    transform: translate3d(-4px, 0, 0);
  }

  40%, 60% {
    transform: translate3d(4px, 0, 0);
  }
}

/* 密码强度条动画 */
.strength-bar {
  transition: all 0.3s ease;
}

/* 焦点状态 */
input:focus + .input-icon {
  color: #1890ff;
}

/* 辅助功能增强 */
input:focus {
  outline: 2px solid rgba(24, 144, 255, 0.2);
  outline-offset: 2px;
}

/* 移动端触控优化 */
@media (pointer: coarse) {
  .register-button,
  .avatar-upload,
  .password-toggle {
    min-height: 44px;
    min-width: 44px;
  }
}
</style>