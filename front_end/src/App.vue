<script setup>
import {RouterLink, useRoute, useRouter} from 'vue-router'
import { ref, watch, computed} from 'vue'
import {useUserStore} from "@/api/app.js";
import {
  Home,
  User,
  Image,
  Video,
  File,
  Palette,  BookCheck
} from 'lucide-vue-next'


const route = useRoute()
const router = useRouter();
const avatar=ref("")
const currentActive = ref(route.path)  // 初始化为当前路由路径
const userStore = useUserStore()
avatar.value =userStore.avatar

// 监听 userId 变化，确保 userId 更新后自动反映到 C.vue
watch(() => userStore.avatar, (newVal) => {
  console.log("Avatar发生了变化:", newVal)
  avatar.value=newVal
})

watch(route, (newRoute) => {
  currentActive.value = newRoute.path
})

const menuItems = [
  { key: '/', label: '首页', icon: Home },
  { key: '/owner', label: '个人资料', icon: User },
  { key: '/about', label: '使用须知', icon: BookCheck },
]

const aiTools = [
  { key: '/auto', label: '图片产生器', icon: Image },
  { key: '/manual', label: '画布绘制', icon: Palette },
  { key: '/HP-generator', label: '画质增强', icon: Video, beta: true },
  { key: '/resource', label: '素材', icon: File },
]

const handleClick = async (item) => {
  // 使用编程式路由跳转
  console.log("开始跳转：", item.key)
  try {
    await router.push(item.key);
    currentActive.value = item.key;
  } catch (error) {
    console.error("跳转失败：", error);
  }
}

// 计算属性，让 isSpecialRoute 根据当前路由动态更新
const isSpecialRoute = computed(() =>
    ['/register', '/login', '/auto', '/manual', '/HP-generator'].includes(route.path)
)
</script>


<template>
  <div class="home-container">
    <aside v-if="!isSpecialRoute" class="sidebar">
      <div class="logo-area">
        <img src="./assets/vue.svg" alt="Dreamina Logo" class="logo">
        <h2>Dreamer</h2>
      </div>

      <nav class="nav-menu">
        <div
            v-for="(item, index) in menuItems"
            :key="index"
            class="nav-item"
            :class="{ 'active': currentActive === item.key }"
        >
          <component :is="item.icon" :size="20" class="nav-icon"/>
          <router-link :to="item.key" @click="handleClick(item)" style="color: black; text-decoration: none">
            <span>{{ item.label }}</span>
          </router-link>
        </div>
      </nav>

      <div class="ai-tools-section">
        <div class="ai-tools-header">
          <span>AI 工具</span>
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 20h9"></path>
            <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
          </svg>
        </div>
        <div
            v-for="(tool, index) in aiTools"
            :key="index"
            class="ai-tool-item"
            :class="{ 'active': currentActive === tool.key }"
        >
          <component :is="tool.icon"  :size="20" class="tool-icon"/>
          <router-link :to="tool.key" @click="handleClick(tool)" style="color: black; text-decoration: none">
            <span>{{ tool.label }}</span>
          </router-link>
          <span v-if="tool.beta" class="beta-tag">Beta</span>
        </div>
      </div>

      <div class="additional-menu">
        <div class="menu-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1-4L17 3z"></path>
          </svg>
          <span>意见反馈</span>
        </div>
        <div class="menu-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="8.5" cy="7" r="4"></circle>
            <polyline points="17 11 19 13 23 9"></polyline>
          </svg>
          <span>邀请</span>
        </div>
      </div>
    </aside>

    <main class="main-content">
      <header  v-if="!isSpecialRoute" class="page-header">
        <div class="user-area">
          <div class="notification-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
            </svg>
            <span class="badge">13</span>
          </div>
          <div class="user-avatar">
            <img :src="avatar" alt="用户头像">
          </div>
        </div>
      </header>
      <router-view :key="route.fullPath" />
    </main>
  </div>
</template>



<style scoped>
.home-container {
  display: flex;
  height: 120vh;
  background-color: #f5f7fa;
}

.sidebar {
  width: 240px;
  background-color: white;
  border-right: 1px solid #e6e9ed;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.logo-area {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.logo {
  width: 40px;
  height: 40px;
  margin-right: 10px;
}

.nav-menu {
  margin-bottom: 20px;
}

.nav-item, .ai-tool-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.nav-icon, .tool-icon {
  margin-right: 10px;
}

.nav-item:hover, .ai-tool-item:hover {
  background-color: #f0f0f0;
}

.ai-tools-section {
  margin-top: 60px;
  padding-top: 20px;
  border-top: 1px solid #e6e9ed;
}

.ai-tools-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: #8c8c8c;
}

.ai-tool-item {
  display: flex;
  align-items: center;
  padding: 8px 15px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.ai-tool-item:hover, .ai-tool-item.active {
  background-color: #e6f7ff;
  color: #1890ff;
}

.tool-icon {
  margin-right: 10px;
}

.main-content {
  flex-grow: 1;
  padding: 20px;
  overflow-y: auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  display: none;
}

.search-input {
  display: none;
}

.search-icon {
  display: none;
}


.user-area {
  display: flex;
  align-items: center;
  margin-left: auto;
}

.notification-icon {
  position: relative;
  margin-right: 20px;
}

.badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: #ff4d4f;
  color: white;
  border-radius: 50%;
  padding: 2px 6px;
  font-size: 10px;
}

.user-avatar {
  width: 40px;  /* 固定宽度 */
  height: 40px; /* 固定高度 */
  border-radius: 60%; /* 圆形头像 */
  overflow: hidden; /* 超出部分隐藏 */
  display: flex;
  justify-content: center;
  align-items: center;
}

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

.additional-menu {
  margin-top: 20px;
  border-top: 1px solid #e6e9ed;
  padding-top: 15px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.menu-item:hover {
  background-color: #f0f0f0;
}

.menu-item svg {
  margin-right: 10px;
}

.tool-icon {
  background-color: #e6f7ff;
  padding: 15px;
  border-radius: 12px;
  margin-right: 15px;
  color: #1890ff;
}

.creation-likes svg {
  margin-right: 5px;
  color: #ff4d4f;
}

.beta-tag {
  background-color: #1890ff;
  color: white;
  font-size: 10px;
  padding: 2px 5px;
  border-radius: 4px;
  margin-left: 8px;
}

.additional-menu {
  margin-top: 20px;
  border-top: 1px solid #e6e9ed;
  padding-top: 15px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.menu-item:hover {
  background-color: #f0f0f0;
}

.menu-item svg {
  margin-right: 10px;
}

.tool-icon {
  background-color: #e6f7ff;
  padding: 15px;
  border-radius: 12px;
  margin-right: 15px;
  color: #1890ff;
}
</style>
