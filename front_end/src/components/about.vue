<template>
  <div class="container">
    <h2 class="title">测试用例图片</h2>
    <div class="image-grid">
      <img v-for="image in images" :key="image.id" :src="image.url"
           class="image-item" @click="openModal(image)" />
    </div>

    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <img :src="selectedImage.url" class="modal-image" />
        <button class="close-button" @click="closeModal">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const images = ref([
  { id: 1, url: 'https://comfyui-e07f.obs.cn-south-1.myhuaweicloud.com/picture/27-1.png' },
  { id: 2, url: 'https://comfyui-e07f.obs.cn-south-1.myhuaweicloud.com/picture/20-1.png' },
  { id: 3, url: 'https://comfyui-e07f.obs.cn-south-1.myhuaweicloud.com/picture/14-1.png' }
]);

const selectedImage = ref(null);
const showModal = ref(false);

const openModal = (image) => {
  selectedImage.value = image;
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  selectedImage.value = null;
};
</script>

<style scoped>
.container {
  max-height: 950px;
  overflow-y: auto; /* Enable scrolling if content exceeds max height */
  margin: 0 auto;
  padding: 20px;
}

.title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 16px;
}

.image-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.image-item {
  width: 100%;
  max-width: 600px; /* Limit the maximum width to avoid images being too wide */
  height: auto;
  max-height: 300px; /* Adjust the max-height to make images smaller */
  object-fit: cover;
  cursor: pointer;
  transition: opacity 0.3s ease-in-out;
  margin: 0 auto; /* Center images horizontally */
}

.image-item:hover {
  opacity: 0.8;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  text-align: center;
  max-width: 80%;
}

.modal-image {
  width: 100%;
  max-height: 500px; /* Set a smaller max-height for modal images */
  object-fit: contain;
}

.close-button {
  margin-top: 10px;
  padding: 10px 20px;
  background: red;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.3s;
}

.close-button:hover {
  background: darkred;
}
</style>