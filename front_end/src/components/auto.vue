<template>
  <button class="back-button" @click="$router.push('/')">返回</button>
  <div class="container">
    <h1>图片裁剪</h1>
    <!-- 上传文件 -->
    <input type="file" @change="onFileChange" accept="image/*" />

    <!-- 原始图片裁剪 -->
    <div v-if="imageUrl" class="crop-container">
      <h2>裁剪区域</h2>
      <img ref="image" :src="imageUrl" alt="裁剪预览" />
      <button @click="cropImage">裁剪</button>
    </div>

    <!-- 显示裁剪的所有图片 -->
    <div v-if="croppedImages.length" class="cropped-images">
      <h2>选择裁剪图片</h2>
      <div class="image-list">
        <img
            v-for="(image, index) in croppedImages"
            :key="index"
            :src="image.croppedImage"
            :class="{ selected: index === selectedImageIndex }"
            @click="selectCroppedImage(index)"
            alt="裁剪图片"
        />
      </div>
    </div>

    <div v-if="selectedImageIndex !== null" class="mask-container">
      <h2>预览图片</h2>
      <canvas ref="maskCanvas" class="mask-canvas"></canvas>
      <el-select
          v-model="currentValue"
          clearable
          placeholder="请选择"
          style="width: 240px; height: 30px; margin-top: 30px"
      >
        <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>
      <div class="button-group" v-if="!inpaintedImage">
        <button @click="submitToBackend(selectedImageIndex)">提交到后端</button>
      </div>
      <div v-else class="button-group">
        <button @click="downloadImage">下载</button>
        <button @click="reset">重新制作</button>
      </div>
    </div>

    <!-- 显示 Inpainting 结果 -->
    <div v-if="inpaintedImage" class="result-container">
      <h2>修复结果</h2>
      <img :src="inpaintedImage" alt="修复结果" />
    </div>
  </div>
</template>

<script>
import Cropper from "cropperjs";
import "cropperjs/dist/cropper.css";
import { fabric } from "fabric";
import {RouterLink, RouterView, useRoute, useRouter} from 'vue-router'
import {ref} from "vue";
import api, {useUserStore} from "@/api/app.js";
import {ElLoading} from "element-plus";


export default {
  setup() {
    const TaskType = {
      SMILE_ADJUSTMENT: "SmileAdjustment",
      EYE_OPENING: "EyeOpening",
    };
    const options = [
      {
        value: TaskType.SMILE_ADJUSTMENT,
        label: '微笑',
      },
      {
        value: TaskType.EYE_OPENING,
        label: '睁眼',
      },
    ]
    return {
      options,
    }
  },
  data() {
    return {
      currentValue:'',
      selectedValues: {}, // 用于存储不同图片的选中值
      islarge: false, //是否放大裁剪图片   ！！！！！
      userid:useUserStore().userId,
      imageUrl: null, // 原始上传的图片
      croppedImages: [], // 存储多个裁剪后的图片
      cropper: null,
      canvas: null,
      selectedImageIndex: null, // 初始时没有选择图片，后续通过点击选择
      inpaintedImage: null, // 后端返回的修复图片
      imageHeight: null,
      imageWidth: null,
      photo_id:null,
      router: useRouter(),
      checkInterval: null,
      stopPolling: null,
      loadingInstance: null, // Element Plus 的 Loading 实例
    };
  },

  mounted() {
    // 组件挂载后，检查登录状态并启动定时器
    this.checkLoginStatus();
    this.checkInterval = setInterval(this.checkLoginStatus, 10 * 60 * 1000); // 每10分钟检查一次
  },

  beforeDestroy() {
    // 组件销毁之前，清除定时器
    clearInterval(this.checkInterval);
  },

  watch: {
    selectedImageIndex(newIndex) {
      if (newIndex !== null) {
        this.currentValue = this.selectedValues[newIndex] || ''; // 切换图片时，更新选中值
        this.croppedImages[newIndex].index = newIndex;
        console.log("当前选择的图片：\n",this.croppedImages[newIndex]);
      }
    },
    currentValue(newValue) {
      if (this.selectedImageIndex !== null) {
        this.selectedValues[this.selectedImageIndex] = newValue; // 选中值更新时，存入对应索引
      }
    }
  },

  methods: {
    /**
     * 处理文件上传
     */

    onFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        // 清空旧数据
        this.islarge = false;
        this.croppedImages = [];  // 清空裁剪图片数组
        this.selectedImageIndex = null; // 初始没有选中图片

        // 释放旧 URL，防止内存泄漏
        if (this.imageUrl) {
          URL.revokeObjectURL(this.imageUrl);
        }

        // 生成新图片 URL
        this.imageUrl = URL.createObjectURL(file);

        // 发送文件到后端
        const formData = new FormData();
        formData.append("file", file);
        fetch(`http://localhost:8080/file/upload/${this.userid}`, {
          method: "POST",
          body: formData,
          headers: {
            "Authorization": "Bearer " + localStorage.getItem("token")
          }
        })
            .then((response) => response.json())
            .then((data) => {
              console.log("上传成功:", data);
              this.photo_id = data.photo_id;
            })
            .catch((error) => {
              console.error("上传失败:", error);
            });

        this.$nextTick(() => {
          const imageElement = this.$refs.image;
          imageElement.onload = () => {
            // 获取原图的高度和宽度
            this.imageHeight = imageElement.naturalHeight;
            this.imageWidth = imageElement.naturalWidth;
            console.log("原图片宽高：", this.imageWidth, this.imageHeight);
          }
          if (this.cropper) this.cropper.destroy(); // 销毁旧 Cropper
          // this.cropper = new Cropper(imageElement, { aspectRatio: 1, zoomOnWheel: false });
          this.cropper = new Cropper(imageElement, {
            aspectRatio: 1, // 设置裁剪框比例（例如 1:1 正方形）
            zoomOnWheel: false,
            background:false,
            viewMode:2,
          });
        });
      }
    },

    /**
     * 裁剪图片
     */
    cropImage() {
      if (this.cropper) {
        var canvas = this.cropper.getCroppedCanvas();
        const scale = 2; // 放大裁剪图片

        const data = this.cropper.getCropBoxData();
        const imageData = this.cropper.getImageData();
        const canvasData = this.cropper.getCanvasData();

        // 计算缩放比例
        const scaleX = imageData.naturalWidth / imageData.width;
        const scaleY = imageData.naturalHeight / imageData.height;

        // 修正 x, y 坐标到原始图片尺寸
        const correctedX = (data.left - canvasData.left) * scaleX;
        const correctedY = (data.top - canvasData.top) * scaleY;
        const correctedWidth = data.width * scaleX;
        const correctedHeight = data.height * scaleY;

        console.log(`修正后 x: ${correctedX}, y: ${correctedY}, 宽: ${correctedWidth}, 高: ${correctedHeight}`);

        if (correctedWidth<100&&correctedHeight<100) {
          this.islarge = true;
          canvas = this.cropper.getCroppedCanvas({
            width: correctedWidth * scale,
            height: correctedHeight * scale
          });
        }
        const croppedImage = canvas.toDataURL("image/png"); // 获取裁剪结果

        // 记录裁剪数据
        this.croppedImages.push({
          croppedImage,  // 裁剪后的图片
          x: correctedX, // 原图中的 x 坐标
          y: correctedY, // 原图中的 y 坐标
          width: correctedWidth,  // 原图中的宽
          height: correctedHeight // 原图中的高
        });


        // 设置为选中当前裁剪图片，且默认选中第一张图片
        if (this.selectedImageIndex === null) {
          this.selectedImageIndex = 0; // 默认选中第一张图片
        }

        console.log(this.croppedImages);
        this.$nextTick(() => {
          if (this.islarge) {
            this.initMaskCanvas(this.croppedImages[0].width*scale, this.croppedImages[0].height*scale,this.croppedImages[0].croppedImage);
          }
          else {
            this.initMaskCanvas(this.croppedImages[0].width, this.croppedImages[0].height
                , this.croppedImages[0].croppedImage);
          }
        });
      }
    },

    /**
     * 初始化 Fabric.js 画布
     */
    initMaskCanvas(width, height, imageUrl) {
      if (this.canvas) {
        this.canvas.clear(); // 清空画布
        this.canvas.dispose(); // 销毁旧的 fabric 画布
      }

      // 创建新的 fabric 画布
      this.canvas = new fabric.Canvas(this.$refs.maskCanvas, {
        width,
        height,
        backgroundColor: "transparent",
      });

      // 确保 canvas 尺寸匹配
      this.$refs.maskCanvas.width = width;
      this.$refs.maskCanvas.height = height;
      this.canvas.setWidth(width);
      this.canvas.setHeight(height);


      // 加载裁剪图片到 fabric.js 画布
      fabric.Image.fromURL(imageUrl, (img) => {
        img.set({ selectable: false }); // 禁止拖拽
        this.canvas.setBackgroundImage(img, this.canvas.renderAll.bind(this.canvas));
      });
    },

    /**
     * 切换不同裁剪图片进行 Mask
     */
    async selectCroppedImage(index) {
      this.selectedImageIndex = index;
      const imageUrl = this.croppedImages[index].croppedImage;

      // 获取裁剪图片的宽高
      const { width, height } = await this.getImageSize(imageUrl);
      console.log("图片的宽高：",width, height);

      await this.$nextTick(() => {
        this.initMaskCanvas(width, height, imageUrl);
      });
    },

    getImageSize(dataURL) {
      return new Promise((resolve) => {
        const img = new Image();
        img.src = dataURL;
        img.onload = () => {
          resolve({ width: img.width, height: img.height });
        };
      });
    },

    /**
     * 提交所有裁剪图片和对应的 Mask
     */
    submitToBackend() {
      const TaskModel = {
        Manual: "Manual",
        Automatic: "Automatic",
      }
      const processedImages = this.croppedImages.map((image) => {
        // 获取裁剪图片的缩放比例（假设放大了 2 倍）
        const scale = 2;


        console.log("功能是：",this.selectedValues);
        console.log("如今的croppedImages",image.x);

        return {
          x: image.x,
          y: image.y,
          width: image.width,
          height: image.height,
          taskType: this.selectedValues[image.index],
          taskModel:TaskModel.Automatic,
        };
      });
      console.log("后端要的数据：",processedImages)

      // **显示加载动画**
      this.loadingInstance = ElLoading.service({
        lock: true,
        text: "正在处理图片...",
        background: "rgba(0, 0, 0, 0.7)",
      });

      // **初始化轮询状态**
      this.stopPolling = false;

      this.startPolling(this.photo_id); // **开始轮询**
      fetch(`http://localhost:8080/file/auto/${this.photo_id}`, {
        method: "POST",
        body: JSON.stringify(processedImages),
        headers: { "Content-Type": "application/json" ,"Authorization": "Bearer " + localStorage.getItem("token")},
      })
          .then((response) => {
            if (!response.ok) {
              throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.json(); // 解析 JSON 数据
          })
          .then((data) => {
            console.log(data);
            // 确保数据中有 path_url 和 message
            this.inpaintedImage = data.path_url; // 显示修复后的图片
            this.stopPolling = true; // **收到图片后停止轮询**
            this.closeLoading(); // **隐藏加载动画**
          })
          .catch((error) => {
            console.error("Error:", error);
            this.closeLoading(); // **错误时也隐藏加载动画**
          });
    },

    downloadImage(){
      const link = document.createElement('a');
      link.href = this.inpaintedImage;
      link.download = '修复结果.png';
      link.click();
    },

    reset(){
      this.router.replace({ path: "/auto", query: { t: Date.now() } }); //页面替换且在url加入当前的时间
    },

    // 轮询任务状态
    startPolling(photoId) {
      const pollingInterval = 3000;
      this.statusMessage = "处理中...";

      const intervalId = setInterval(() => {
        if (this.stopPolling) {
          clearInterval(intervalId); // **收到图片后停止轮询**
          return;
        }

        fetch(`http://localhost:8080/file/status/${photoId}`, {
          method: "GET",
          headers: {
            "Authorization": "Bearer " + localStorage.getItem("token"),
          },
        })
            .then((response) => {
              if (response.status === 200) {
                this.progress = 100;
                this.stopPolling = true; // **轮询成功，标记停止**
                clearInterval(intervalId);
                this.closeLoading(); // **隐藏加载动画**
              } else if (response.status === 100) {
                this.progress = Math.min(this.progress + 10, 90);
              } else {
                clearInterval(intervalId);
                this.closeLoading(); // **隐藏加载动画**
              }
            })
            .catch((error) => {
              console.error("轮询请求错误:", error);
              clearInterval(intervalId);
            });
      }, pollingInterval);
    },

    async  checkLoginStatus(){
      const token = localStorage.getItem('token');
      if (!token) {
        await this.router.push('/login');
        return;
      }
      try {
        await api.get('/api/check'); // 检查 token 是否有效
      } catch (error) {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        await this.router.push('/login');
      }
    },

    // 关闭加载动画
    closeLoading() {
      if (this.loadingInstance) {
        this.loadingInstance.close();
        this.loadingInstance = null;
      }
    },
  },
};
</script>


<style scoped>
.back-button {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 8px 12px;
  background-color: #007bff;
  color: white;
  border: none;
  cursor: pointer;
  transition: 0.3s;
}

.back-button:hover {
  background-color: #0056b3;
}
.container {
  max-width: 700px;
  margin: 20px auto;
  text-align: center;
}
.crop-container {
  margin-top: 20px;
}
.cropped-images {
  margin-top: 20px;
}
.image-list {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}
.image-list img {
  width: 100px;
  height: 100px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: 0.3s;
}
.image-list img.selected {
  border-color: #007bff;
}
.mask-container {
  display: flex;            /* 启用 Flexbox 布局 */
  justify-content: center;  /* 水平居中 */
  align-items: center;      /* 垂直居中 */
  flex-direction: column;   /* 垂直方向排列元素 ！！！！！！！*/
  margin-top: 20px;
}

.mask-canvas {
  border: 1px solid #ddd;
  margin-top: 10px;

}
.button-group {
  margin-top: 10px;
}
button {
  margin: 5px;
  padding: 8px 12px;
  border: none;
  background-color: #007bff;
  color: white;
  cursor: pointer;
  transition: 0.3s;
}
button:hover {
  background-color: #0056b3;
}
.result-container {
  margin-top: 20px;
}
</style>
