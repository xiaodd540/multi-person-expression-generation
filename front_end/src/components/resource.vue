<template>
  <h1>素材展示</h1>
  <div class="recent-creations-container">
    <div
        v-for="(generatedImages, originalImage) in recentCreations"
        :key="originalImage"
        class="creation-item"
    >
      <!-- 显示原图 -->
      <div class="original-image">
        <img :src="originalImage" alt="Original Image" />
        <p class="caption">原图</p>
      </div>

      <!-- 显示生成图，在同一行内展示 -->
      <div class="generated-images">
        <div
            v-for="(image, index) in generatedImages"
            :key="index"
            class="generated-image"
            :class="{
            'hovered': hoverIndex[originalImage] === index,
          }"
            @mouseover="onHover(originalImage, index)"
            @mouseleave="onLeave(originalImage)"
            @click="openPreview(image)"
        >
        <img :src="image" alt="Generated Image" />
          <p class="caption">生成图 {{ index + 1 }}</p>
      </div>
    </div>
  </div>

  <!-- 预览模态框 -->
  <div v-if="isPreviewOpen" class="preview-modal" @click="closePreview">
    <div class="preview-modal-content">
      <img :src="previewImage" alt="Preview Image" />
    </div>
  </div>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, ref} from 'vue';
import api from "../api/app.js";
import {useUserStore} from "../api/app.js";
import {useRouter} from "vue-router";
import axios from "axios";

// 模拟的图像数据
// const recentCreations = {
//   "src/assets/test_small.jpg": [
//     "src/assets/test.jpg",
//     "src/assets/洛克王国.jpg",
//     "src/assets/洛克王国.jpg"
//   ],
//   "src/assets/test.jpg": [
//     "src/assets/洛克王国.jpg",
//     "src/assets/洛克王国.jpg"
//   ],
//   "src/assets/vue.svg": [
//     "src/assets/洛克王国.jpg",
//     "src/assets/洛克王国.jpg",
//     "src/assets/洛克王国.jpg",
//     "src/assets/洛克王国.jpg",
//     "src/assets/test.jpg",
//     "src/assets/洛克王国.jpg",
//     "src/assets/洛克王国.jpg",
//   ]
// };

const recentCreations = ref({})

const hoverIndex = ref({}); // 用来记录每个原图下当前悬停的生成图索引
const isPreviewOpen = ref(false); // 控制预览框的显示
const previewImage = ref(''); // 用来存储要预览的图片路径
const userId=useUserStore().userId;
let checkInterval = null;
const router = useRouter();

onMounted(() => {
  checkLoginStatus(); // 页面加载时检查
  fetchRecentCreations();
  checkInterval = setInterval(checkLoginStatus, 10 * 60 * 1000); // 10分钟检查一次
});

onUnmounted(() => {
  clearInterval(checkInterval);
});

// 鼠标悬停时记录当前原图和生成图的索引
const onHover = (originalImage, index) => {
  hoverIndex.value[originalImage] = index;
};

// 鼠标离开时清除悬停状态
const onLeave = (originalImage) => {
  delete hoverIndex.value[originalImage];
};

// 打开预览框
const openPreview = (image) => {
  previewImage.value = image;
  isPreviewOpen.value = true;
};

// 关闭预览框
const closePreview = () => {
  isPreviewOpen.value = false;
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

const fetchRecentCreations = async () => {
  try {
    const response = await api.get(`/api/resource/${userId}`);
    console.log("/resource的响应体的值：\n",response.data);
    recentCreations.value = response.data; // 赋值给 recentCreations
  } catch (error) {
    console.error('获取数据失败:', error);
  }
};
</script>

<style scoped>
.recent-creations-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  max-height: 850px;
  padding: 20px;
  background-color: #f5f5f5;
}

.creation-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: background-color 0.3s ease, transform 0.3s ease;
}

.creation-item:hover {
  transform: translateY(-5px); /* 提升效果 */
}

.creation-item:not(:last-child) {
  border-bottom: 2px solid #e0e0e0; /* 每行之间添加分隔线 */
}

.original-image img {
  width: 100%;
  max-width: 300px;
  border-radius: 8px;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  object-fit: cover;
}

.generated-images {
  display: flex;
  gap: 15px;
  margin-top: 10px;
  flex-wrap: nowrap; /* 保证所有生成图在同一行 */
  justify-content: flex-start;
  align-items: center;
  width: 100%;
  overflow-x: auto; /* 启用横向滚动 */
  overflow-y: hidden;
}

.generated-image {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  min-width: 200px; /* 每张生成图的宽度固定 */
  max-width: 300px;
  flex-shrink: 0; /* 不允许生成图缩小 */
}

.generated-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.generated-image:hover {
  cursor: pointer;
}

/* 样式：当鼠标悬停在某个生成图时，应用效果 */
.hovered {
  transform: scale(1.1);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
}

.caption {
  margin-top: 0.5rem;
  color: #666;
  font-size: 1.5rem;
  text-align: center;
  font-weight: bold;
}

/* 预览模态框样式 */
.preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.preview-modal-content img {
  max-width: 80%;
  max-height: 80%;
  object-fit: contain;
  border-radius: 8px;
}
</style>
