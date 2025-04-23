<script setup>
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { Image, Heart } from "lucide-vue-next";
import api from "../api/app.js";
import { onMounted, onUnmounted, ref } from "vue";
import { useUserStore } from "../api/app.js";

const recentCreations = ref({}) // 避免 undefined

const featuredTools = [
  { key: '/auto', title: '图片产生器', description: '自动生成对应样式', icon: Image },
  { key: '/manual', title: '画布绘制', description: '手动绘制生成', icon: Image },
  { key: '/HP-generator', title: '画质增强', description: '提高照片画质', icon: Image, beta: true }
];

const router = useRouter();
let checkInterval = null;
const data = ref({});
const loading = ref(true);
const user_avatar = useUserStore();
const userId = user_avatar.userId ? Number(user_avatar.userId) : null; // 避免 NaN

// 加载数据
const loadData = async () => {
  try {
    console.log("传递过来的 UserId:", userId);
    if (!userId) throw new Error("无效的 UserId");

    const response = await api.get(`/api/data?userId=${userId}`);
    console.log("数据响应：", response);

    data.value = response.data;
    user_avatar.setAvatar(data.value.avatar);
    user_avatar.setId(userId);
    user_avatar.setUsername(data.value.userName);
    recentCreations.value = data.value.data || {}; // 确保不会是 undefined

    console.log("最近上传:", recentCreations.value);
  } catch (error) {
    console.error("加载数据失败", error);
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
    await api.get('/api/check');
  } catch (error) {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    await router.push('/login');
  }
};

const handleClick = async (item) => {
  console.log("跳转到：", item.key);
  try {
    await router.push(item.key);
  } catch (error) {
    console.error("跳转失败：", error);
  }
};

onMounted(() => {
  checkLoginStatus();
  loadData();
  checkInterval = setInterval(checkLoginStatus, 10 * 60 * 1000);
});

onUnmounted(() => {
  if (checkInterval) clearInterval(checkInterval); // 避免报错
});
</script>

<template>
  <div class="dashboard">
    <div class="ai-tools-preview">
      <h2>快速开始</h2>
      <div class="ai-tools-grid">
        <div v-for="(tool, index) in featuredTools" :key="index" class="ai-tool-card" @click="handleClick(tool)">
          <div class="tool-content">
            <h3>{{ tool.title }}</h3>
            <p>{{ tool.description }}</p>
            <span v-if="tool.beta" class="beta-tag">Beta</span>
          </div>
        </div>
      </div>
    </div>

    <div class="recent-creations-container">
      <h2>最近创作</h2>
      <div class="recent-creations">
        <div class="creation-scroll-container">
          <template v-if="recentCreations && Object.keys(recentCreations).length">
            <div v-for="(images, mainImage) in recentCreations" :key="mainImage" class="creation-section">
              <div class="main-image-container">
                <img :src="mainImage" alt="Main image" class="main-image">
              </div>
              <div class="related-images-grid">
                <div v-for="relatedImage in images" :key="relatedImage" class="related-image-item">
                  <img :src="relatedImage" alt="Related image">
                  <div class="image-overlay">
                    <div class="likes-container">
                      <Heart/>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <div v-else class="no-records">
            <h3>最近 7 天没有上传记录</h3>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保持图像比例且完全覆盖 */
  object-position: center; /* 图像居中 */
}

.dashboard-header h1 {
  margin-bottom: 10px;
}

.card-content h3 {
  margin-bottom: 5px;
}

.beta-tag {
  background-color: #1890ff;
  color: white;
  font-size: 10px;
  padding: 2px 5px;
  border-radius: 4px;
  margin-left: 8px;
}

.menu-item svg {
  margin-right: 10px;
}

.ai-tools-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.ai-tool-card {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
}

.ai-tool-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.tool-content h3 {
  margin-bottom: 5px;
}

.beta-tag {
  background-color: #1890ff;
  color: white;
  font-size: 10px;
  padding: 2px 5px;
  border-radius: 4px;
  margin-left: 8px;
}

.menu-item svg {
  margin-right: 10px;
}

.ai-tools-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.ai-tool-card {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
}

.ai-tool-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.tool-content h3 {
  margin-bottom: 5px;
}

.recent-creations-container {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
}

.recent-creations {
  max-height: 720px;
  overflow-x:hidden;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: #e0e0e0 #f5f5f5;
}

.creation-scroll-container {
  display: flex;
  flex-direction: column; /* 修改为竖直排列 */
  width: 100%;
}

.creation-section {
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
  width: 100%;
}

.main-image-container {
  width: auto;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 15px;
}

.main-image {
  width: 200px;
  height: 200px;
  object-fit: cover;
}

.related-images-grid {
  display: flex; /* 使用 flex 让图片在一行 */
  gap: 15px;
  overflow-x: auto; /* 允许横向滚动 */
  overflow-y: hidden; /* 禁止竖向滚动 */
  white-space: nowrap; /* 确保不会换行 */
  padding-bottom: 10px; /* 避免滚动条挡住内容 */
  scrollbar-width: thin;
  scrollbar-color: #e0e0e0 #f5f5f5;
}

.related-image-item {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.3s ease;
}

.related-image-item:hover {
  transform: scale(1.05);
}

.related-image-item img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background-color: rgba(0,0,0,0.5);
  color: white;
  padding: 5px 10px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.likes-container {
  display: flex;
  align-items: center;
  gap: 5px;
}

.likes-container svg {
  color: red;
}

/* 隐藏默认滚动条，仅在 Webkit 内核浏览器上美化 */
.related-images-grid::-webkit-scrollbar {
  height: 6px; /* 控制滚动条的高度 */
}

.related-images-grid::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2); /* 滚动条颜色 */
  border-radius: 10px;
}

.related-images-grid::-webkit-scrollbar-track {
  background: transparent; /* 轨道透明 */
}

/* 确保 related-image-item 的大小不会影响排列 */
.related-image-item {
  flex: 0 0 auto; /* 保证不会换行 */
  width: 150px; /* 图片大小 */
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.3s ease;
}

.related-image-item img {
  width: 100%;
  height: 150px;
  object-fit: cover;
}
</style>
