import os
from fastapi import FastAPI, HTTPException
import photo_making
import json
from pydantic import BaseModel
from typing import List, Dict
from PIL import Image
from fastapi.middleware.cors import CORSMiddleware


# 创建一个 FastAPI 实例
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 或指定允许的源
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 本地存储路径
LOCAL_STORAGE = "images/img"
PROCESSED_STORAGE = "images/final"
os.makedirs(LOCAL_STORAGE, exist_ok=True)
os.makedirs(PROCESSED_STORAGE, exist_ok=True)


# 存储所有裁剪图片的处理结果
processed_images: Dict[str, List[Dict]] = {}

# 裁剪信息模型
class CropInfo(BaseModel):
    y: float
    x: float
    height: float
    width: float

# 处理任务模型
class auto_ImageProcessingRequest(BaseModel):
    photoUrl: str  # 原图 URL
    taskType: str  # 任务类型
    taskId: int  # 任务 ID
    crop: CropInfo  # 裁剪信息

class manual_ImageProcessingRequest(BaseModel):
    maskUrl: str  #遮罩图base64
    photoUrl: str  # 原图 URL
    taskType: str  # 任务类型
    taskId: int  # 任务 ID
    crop: CropInfo  # 裁剪信息
    
# 超分辨率请求模型
class super_ImageProcessingRequest(BaseModel):
    photoUrl: str  # 原图 URL
    taskId: int  # 任务 ID
    photoId: int  #  原图ID

# 合成请求模型
class MergeRequest(BaseModel):
    photoUrl: str  # 原图 URL
    photoId: int  # 原图 ID



# 定义一个文件下载的路由
@app.get("/")
def index():
    return "已经启动fastapi"  # 直接返回字符串

#自动化接口
@app.post("/comfyui/auto")
async def auto(request: auto_ImageProcessingRequest):
    try:
        # 确保存储 photoUrl 作为 key，存放所有处理好的图片
        if request.photoUrl not in processed_images:
            processed_images[request.photoUrl] = []

        generate_path, generatedUrl = photo_making.process_auto_inpainting(
              request.photoUrl, 
              request.crop.x, 
              request.crop.y, 
              request.crop.width, 
              request.crop.height, 
              request.taskId, 
              request.taskType
        )

        # 如果 generatedUrl 为空，返回 404
        if not generatedUrl:
            raise HTTPException(status_code=404, detail="Generated image URL is empty")
        
        processed_images[request.photoUrl].append({
            "taskId": request.taskId,
            "processedImagePath": generate_path,
            "crop": request.crop.model_dump()
        })

        return {"taskId": request.taskId, "status": "processed", "processedImagePath": generatedUrl}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


#手动化接口
@app.post("/comfyui/manual")
async def manual(request: manual_ImageProcessingRequest):
    print("开始收到请求")
    try:
        # 确保存储 photoUrl 作为 key，存放所有处理好的图片
        if request.photoUrl not in processed_images:
            processed_images[request.photoUrl] = []

        generate_path, generatedUrl = photo_making.process_manual_inpainting(
            request.photoUrl,
            request.crop.x,
            request.crop.y,
            request.crop.width,
            request.crop.height,
            request.taskId,
            request.taskType,
            request.maskUrl
        )
        
        # 如果 generatedUrl 为空，返回 404
        if not generatedUrl:
            raise HTTPException(status_code=404, detail="Generated image URL is empty")
        
        processed_images[request.photoUrl].append({
            "taskId": request.taskId,
            "processedImagePath": generate_path,
            "crop": request.crop.model_dump()
        })

        return {"taskId": request.taskId, "status": "processed", "processedImagePath": generatedUrl}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))



# 处理合成请求
@app.post("/comfyui/merge-images")
async def merge_images(request: MergeRequest):
    try:
        # 获取原图路径
        original_image_path = os.path.join(LOCAL_STORAGE, os.path.basename(request.photoUrl))
        if not os.path.exists(original_image_path):
            raise HTTPException(status_code=400, detail="Original image not found")

        # 确保所有处理的裁剪图片都已存储
        if request.photoUrl not in processed_images or not processed_images[request.photoUrl]:
            raise HTTPException(status_code=400, detail="No processed images available")

        # 载入原图
        original_image = Image.open(original_image_path)
        original_image = convert_to_png(original_image)

        # 逐个叠加处理后的裁剪图片
        for item in processed_images[request.photoUrl]:
            processed_img_path = item["processedImagePath"]
            crop = item["crop"]
            print(f"准备合成的图片: {processed_img_path}")
            if os.path.exists(processed_img_path):
                processed_crop_img = Image.open(processed_img_path)
                original_image.paste(processed_crop_img, (round(crop["x"]), round(crop["y"])))
                print(f"已合成裁剪图: {processed_img_path}")
            else:
                print(f"警告：找不到处理图像路径: {processed_img_path}")

        # 生成最终合成图
        merged_image_path = os.path.join(PROCESSED_STORAGE, f"final_photo{request.photoId}.png")
        original_image.save(merged_image_path)
        print(f"合成图已保存: {merged_image_path}")

        # 上传图片（OBS）
        photo_making.upload(merged_image_path)

        final_url = f"https://comfyui-e07f.obs.cn-south-1.myhuaweicloud.com/images/final/{os.path.basename(merged_image_path)}"
        print(f"最终图片地址: {final_url}")

        return {
            "taskId": request.photoId,
            "status": "merged",
            "mergedImagePath": final_url
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

def convert_to_png(image):
    """ 确保图片为 PNG 格式，并返回转换后的 Image 对象 """
    if image.format != "PNG":
        image = image.convert("RGBA")  # 确保支持透明背景
    return image

@app.post("/comfyui/SR")
async def super(request: super_ImageProcessingRequest):
    try:
        generatedUrl = photo_making.process_super(request.photoUrl,request.taskId,request.photoId)
        return {"taskId": request.photoId, "status": "SR", "processedImagePath": generatedUrl}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))



# 程序的入口
if __name__ == "__main__":
    import uvicorn

    # 使用指定的端口运行 FastAPI 服务器
    uvicorn.run(app, host="0.0.0.0", port=6006)
