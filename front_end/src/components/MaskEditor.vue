<template>
  <div class="mask-editor">
    <canvas
        ref="canvas"
        :width="canvasWidth"
        :height="canvasHeight"
        @mousedown="startDrawing"
        @mousemove="draw"
        @mouseup="stopDrawing"
    ></canvas>

    <button @click="resetCanvas">重置画布</button>
    <button @click="completeMask">完成遮罩</button>
  </div>
</template>

<script>
export default {
  props: ["originalImage"],
  setup(props) {
    const canvas = ref(null);
    const ctx = ref(null);
    const canvasWidth = 512;
    const canvasHeight = 512;
    const isDrawing = ref(false);

    const startDrawing = (event) => {
      ctx.value.beginPath();
      ctx.value.moveTo(event.offsetX, event.offsetY);
      isDrawing.value = true;
    };

    const draw = (event) => {
      if (!isDrawing.value) return;
      ctx.value.lineTo(event.offsetX, event.offsetY);
      ctx.value.strokeStyle = "white";
      ctx.value.lineWidth = 10;
      ctx.value.stroke();
    };

    const stopDrawing = () => {
      isDrawing.value = false;
      ctx.value.closePath();
    };

    const resetCanvas = () => {
      ctx.value.clearRect(0, 0, canvasWidth, canvasHeight);
      ctx.value.drawImage(props.originalImage, 0, 0, canvasWidth, canvasHeight);
    };

    const completeMask = () => {
      const mask = canvas.value.toDataURL("image/png");
      window.opener.updateImage(mask); // 回传遮罩数据
      window.close();
    };

    onMounted(() => {
      ctx.value = canvas.value.getContext("2d");
      const img = new Image();
      img.src = props.originalImage;
      img.onload = () => ctx.value.drawImage(img, 0, 0, canvasWidth, canvasHeight);
    });

    return {
      canvas,
      startDrawing,
      draw,
      stopDrawing,
      resetCanvas,
      completeMask,
    };
  },
};
</script>
