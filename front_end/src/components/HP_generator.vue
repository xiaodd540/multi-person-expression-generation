<template>
  <div class="container">
    <div class="header">
      <button @click="goHome" class="back-button">返回首页</button>
    </div>
    <div class="upload-section">
      <input type="file" @change="handleFileUpload" accept="image/*" class="upload-input" />
      <button @click="enhanceImage" :disabled="!imageSrc" class="enhance-button">HP增强</button>
    </div>
    <div v-if="imageSrc" class="image-info">
      <p>图片大小: {{ imageSize }}</p>
    </div>
    <div v-if="imageSrc" class="image-preview">
      <div class="image-wrapper">
        <img :src="imageSrc" alt="Uploaded Image" />
        <div v-if="loading" class="processing-overlay">正在加工...</div>
      </div>
    </div>
    <div v-if="enhancedImageSrc" class="enhanced-image-preview">
      <span class="enhanced-label">增强后图片</span>
      <img :src="enhancedImageSrc" alt="Enhanced Image" />
    </div>
    <div v-if="enhancedImageSrc" class="download-section">
      <a :href="enhancedImageSrc" download="enhanced_image.jpg" class="download-button">下载图片</a>
    </div>
  </div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, onUnmounted, ref} from 'vue';
import { useRouter } from 'vue-router';
import api from "@/api/app.js";
import {useUserStore} from "../api/app.js";
import axios from "axios";

const router = useRouter();
const imageSrc = ref(null);
const enhancedImageSrc = ref(null);
const imageSize = ref('');
const loading = ref(false);
const checkInterval = ref(null);
const userId=useUserStore().userId
const photo_id=ref(null);

onMounted(() => {
  checkLoginStatus();
  checkInterval.value = setInterval(checkLoginStatus, 10 * 60 * 1000); // 每10分钟检查一次
});

onUnmounted(() => {
  clearInterval(checkInterval);
});

const goHome = () => {
  router.push('/');
};

const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (file) {
    imageSrc.value = URL.createObjectURL(file);
    imageSize.value = (file.size / 1024).toFixed(2) + ' KB';
  }

  try {
    const formData = new FormData();
    formData.append('file', file);
    const response = await fetch(`http://localhost:8080/file/upload/${userId}`, {
      method: 'POST',
      body: formData,
      headers: {
        "Authorization": "Bearer " + localStorage.getItem("token")
      }
    })

    if (!response.ok) {
      throw new Error('上传失败');
    }

    const data = await response.json();
    photo_id.value = data.photo_id;
    console.log("超分上传图片的ID:", photo_id.value);

  } catch (err) {
    console.error("上传失败", err);
  }

};

const enhanceImage = async () => {
  if (!imageSrc.value) return;

  loading.value = true;
  // setTimeout(() => {
  //   enhancedImageSrc.value = imageSrc.value; // 模拟增强后的图片
  //   loading.value = false;
  // }, 2000);

  try {
    const res = await fetch(`http://localhost:8080/file/SR/${photo_id.value}`, {
      method: 'POST',
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + localStorage.getItem("token")
      },
    });
    if (!res.ok) {
      throw new Error('图片增强请求失败：' + res.statusText);
    }

    const data = await res.json();
    console.log("超分响应体：", data);

    enhancedImageSrc.value = data.path_url;

  } catch (error) {
    console.error('增强失败:', error);
  } finally {
    loading.value = false;
  }
};

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
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 30px;
  background: linear-gradient(135deg, #f0f4f8, #d9e2ec);
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
.header {
  width: 100%;
  display: flex;
  justify-content: flex-start;
}
.back-button {
  padding: 10px 15px;
  background-color: #ff4d4d;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.back-button:hover {
  background-color: #cc0000;
}
.upload-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.upload-input {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 8px;
}
.enhance-button {
  padding: 10px 20px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.enhance-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
.image-info {
  font-size: 16px;
  color: #333;
}
.image-preview, .enhanced-image-preview {
  width: 320px;
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  background: white;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
.image-preview img, .enhanced-image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}
.processing-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  border-radius: 12px;
}
.enhanced-label {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 14px;
}
.download-section {
  margin-top: 15px;
}
.download-button {
  padding: 12px 18px;
  background-color: #007bff;
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.download-button:hover {
  background-color: #0056b3;
}
</style>