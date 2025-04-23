<template>
  <div class="owner-container">
    <div class="owner-profile">
      <!-- 左侧 头像 + 用户信息 -->
      <div class="profile-left">
        <div class="avatar-container">
          <img :src="userProfile.avatar" class="avatar" alt="用户头像">
          <div class="avatar-overlay" @click="showAvatarModal = true">
            <Camera name="Camera" :size="24" />
            <el-text style="color: black">更换头像</el-text>
          </div>
        </div>
        <div class="user-info">
          <h2>{{ userProfile.username }}</h2>
        </div>

        <button class="forgot-password-button" @click="showPasswordModal = true">修改密码</button>
        <!-- 退出登录按钮 -->
        <button class="logout-button" @click="logout">退出登录</button>

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
                    disabled
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
                <small v-if="newPassword && newPassword.length < 6" class="error-text">
                  密码长度至少为6个字符
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

        <!-- 更换头像弹窗 -->
        <div v-if="showAvatarModal" class="modal">
          <div class="modal-content">
            <h2>更换头像</h2>

            <!-- 头像选择区域 -->
            <div class="avatar-preview">
              <img :src="avatarPreview" alt="头像预览" class="avatar-img" v-if="avatarPreview" />
              <div v-else class="avatar-placeholder">请选择头像</div>
            </div>

            <!-- 文件上传 -->
            <input type="file" @change="handleAvatarChange" accept="image/*" class="file-input" />

            <!-- 提交按钮 -->
            <button @click="handleSubmit" :disabled="!avatarPreview" class="submit-btn">上传</button>

            <!-- 关闭按钮 -->
            <button @click="closeModal" class="close-btn">关闭</button>
          </div>
        </div>
      </div>

      <!-- 右侧 上传记录 -->
      <div class="profile-right">
        <h3>上传记录</h3>

        <div v-if="loading" style="text-align: center">加载中...</div>
        <div v-else-if="uploadRecords.length === 0" style="text-align: center">暂无上传记录</div>

        <div class="upload-records">
          <div v-for="(record, index) in uploadRecords" :key="index" class="record-item">
            <div class="record-image">
              <img :src="record.image" :alt="record.date" @error="handleImageError">
            </div>
            <div class="record-details">
              <div class="record-meta">
                <span>{{ record.date }}</span>
                <span>{{ record.status }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页导航 -->
        <div class="pagination">
          <button @click="prevPage" :disabled="page === 1">上一页</button>
          <span>第 {{ page }} 页 / 共 {{ totalPages }} 页 (总计 {{ total }} 条)</span>
          <button @click="nextPage" :disabled="page >= totalPages">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, computed, watch, onUnmounted} from 'vue';
import { Camera } from 'lucide-vue-next';
import axios from 'axios';
import { useRoute ,useRouter} from 'vue-router';
import api, {useUserStore} from "@/api/app.js";
import { storeToRefs } from 'pinia'

// 获取用户 ID，监听路由变化
const user=useUserStore()
const userId = user.userId
const { avatar, username } = storeToRefs(user)

// 记录 & 分页
const loading = ref(true);
const page = ref(1);
const size = 5; // 每页 5 条
const total = ref(0);
const totalPages = ref(1);
const uploadRecords = ref({});

const userProfile = ref({
  avatar,
  username
})
// 处理错误图片
const handleImageError = (event) => {
  event.target.src = '/fallback.jpg';
};

// 获取历史记录
const fetchHistory = async () => {
  loading.value = true;
  try {
    const response = await api.post("/api/history", {
      userId: userId,
      page: page.value,
      size: size
    });

    console.log("/history的响应体:",response.data);
    // 解析响应数据
    if (response.data && response.data.photos) {
      uploadRecords.value = response.data.photos.map(photo => ({
        image: photo.PhotoPath,  // 这里使用正确的字段名
        date: new Date(photo.UploadTime).toLocaleString(), // 使用正确的字段名
        status: photo.Status // 使用正确的字段名
      }));
    } else {
      console.log("No photos found in the response.");
    }
    total.value = response.data.total;
    totalPages.value = Math.max(1, Math.ceil(total.value / size)); // 至少 1 页
  } catch (error) {
    console.error("获取历史记录失败", error);
  } finally {
    loading.value = false;
  }
};

// 上一页
const prevPage = () => {
  if (page.value > 1) {
    page.value--;
    fetchHistory();
  }
};

// 下一页
const nextPage = () => {
  if (page.value < totalPages.value) {
    page.value++;
    fetchHistory();
  }
};

const router = useRouter();
let checkInterval =null;
const checkLoginStatus = async () => {
  const token = localStorage.getItem('token');
  if (!token) {
    await router.push('/login');
    return;
  }
  try {
    await api.get('/api/check'); // 检查 token 是否有效
  } catch (error) {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    await router.push('/login');
  }
};

// 退出登录
const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  router.push('/login'); // 跳转到登录页面
};

// 定义响应式变量
const showPasswordModal = ref(false);  // 控制弹窗显示
const account = ref(user.username);               // 账号
const newPassword = ref('');           // 新密码
const confirmPassword = ref('');       // 确认密码

// 计算属性，用于判断提交按钮是否禁用
const isSubmitDisabled = computed(() => {
  return (
      !account.value ||
      newPassword.value.length < 6 ||
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
    },{headers: {'Auth': 'multipart/form-data'}})
    console.log("/users/password", res.data)
    if (res.data.status === "success") {
      alert(`账号：${account.value}，密码修改成功！`);
      closeModal(); // 关闭弹窗
    }
    else {
      alert('输入出现问题');
    }
  }catch(error) {
    alert('服务器出现问题：',error.message)
  }

};

// 关闭弹窗
const closeModal = () => {
  showPasswordModal.value = false;
  newPassword.value = '';
  confirmPassword.value = '';
  showAvatarModal.value = false;
  avatarPreview.value = null; // 清空头像预览
};

const showAvatarModal = ref(false); // 控制弹窗显示
const avatarPreview = ref(null);   // 头像预览图像
const avatarFile = ref(null);      // 保存选中的头像文件

// 处理头像文件更换
const handleAvatarChange = async (event) => {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = () => {
      avatarPreview.value = reader.result; // 设置头像预览
    };
    reader.readAsDataURL(file); // 读取文件为 Data URL
    try {
      const formData = new FormData();
      formData.append("file", file);
      const response = await axios.post('http://localhost:8080/users/avatar', formData,{headers: {'Content-Type': 'multipart/form-data'}})
      console.log("上传成功:", response.data);
      if (response.status === 200) {
        avatarFile.value = response.data
        console.log("上传成功:", response.data);
      }
    }catch(err) {
      console.log(err)
    }
  }
};

// 提交更换头像
const handleSubmit = async () => {
  if (avatarPreview.value) {
    try {
        const res = await axios.post('http://localhost:8080/users/changeAvatar', {
          userId: userId,
          avatar: avatarFile.value
        },{headers: {'Authorization': "Bearer " + localStorage.getItem("token")}})
       if (res.status === 200 && res.data==="上传成功") {
         user.setAvatar(avatarFile.value);
         // userProfile.value.avatar = avatarFile.value;
         alert('头像上传成功！'); // 这里可以放置实际的上传逻辑
         closeModal(); // 上传后关闭弹窗
       }
       else {
         throw new Error("服务器有问题:"+res.data)
       }
    }catch(err) {
      console.log(err.message);
      alert("出现问题："+err.message)
    }

  }
};

// 页面加载 & 监听 userId 变化
onMounted(() => {
  checkLoginStatus(); // 页面加载时检查
  fetchHistory();
  checkInterval = setInterval(checkLoginStatus, 10 * 60 * 1000); // 10分钟检查一次
});

onUnmounted(() => {
  clearInterval(checkInterval);
});

</script>


<style scoped>
body {
  overflow: hidden; /* 隐藏 body 的滚动条 */
}

.owner-container {
  background-color: #f5f5f5;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  padding: 40px 20px;
}

.owner-profile {
  display: flex;
  width: 100%;
  max-width: 1200px;
  background-color: white;
  border-radius: 16px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.profile-left {
  width: 35%;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-right: 1px solid #e0e0e0;
}

.avatar-container {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #e0e0e0;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background-color: #ccc;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.profile-right {
  width: 65%;
  padding: 40px;
}

.upload-records {
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  overflow-x: hidden;
  max-height: 950px;
  -webkit-overflow-scrolling: touch;
}

.record-item {
  display: flex;
  align-items: center;
  background-color: #f9f9f9;
  border-radius: 12px;
  padding: 15px;
  transition: transform 0.3s;
}

.record-item:hover {
  transform: scale(1.02);
}

.record-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 20px;
}

.record-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.record-details {
  flex-grow: 1;
}

.record-meta {
  display: flex;
  justify-content: space-evenly;
  align-items: center;
  margin-top: 10px;
  color: #666;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
  align-items: center;
}

.pagination button {
  padding: 6px 12px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  background-color: #007bff;
  color: white;
  transition: background 0.3s;
}

.pagination button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.forgot-password-button {
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.3s;
}

.forgot-password-button:hover {
  background-color: #c0392b;
}

.logout-button {
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.3s;
}

.logout-button:hover {
  background-color: #c0392b;
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

/* 头像预览区域 */
.avatar-preview {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 50%;
  width: 150px;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
}

.avatar-img {
  border-radius: 50%;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 14px;
  color: #aaa;
}

/* 文件输入框样式 */
.file-input {
  display: inline;
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
