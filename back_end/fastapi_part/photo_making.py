import base64
from PIL import Image
import subprocess
import json
from obs import PutObjectHeader, HeadPermission, ObsClient
import yaml
import os
import traceback
from urllib.parse import urlparse
import requests
from io import BytesIO
import shutil

def getPath(obs_url):
    try:
        # 读取配置文件
        with open("/root/config.yml", "r", encoding="utf-8") as file:
            config = yaml.safe_load(file)

        ak = os.getenv(config["obs"]["AccessKeyID"])
        sk = os.getenv(config["obs"]["SecretAccessKey"])
        server = "https://obs.cn-south-1.myhuaweicloud.com"

        # 创建 obsClient 实例
        obsClient = ObsClient(access_key_id=ak, secret_access_key=sk, server=server)

        # 解析 URL 获取 bucketName 和 objectKey
        parsed_url = urlparse(obs_url)  
        path_parts = parsed_url.path.lstrip("/") 
        
        bucketName = "comfyui-e07f" ##要改成自己华为云obs桶名
        objectKey = path_parts  

        # 确保下载目录存在
        os.makedirs("images/img", exist_ok=True)

        # 解析文件名并定义下载路径
        file_name = os.path.basename(objectKey)  
        downloadPath = os.path.join(objectKey)

        # 下载文件
        resp = obsClient.getObject(bucketName, objectKey, downloadPath)

        # 检查请求结果
        if resp.status < 300:
            print("Get Object Succeeded")
            print("Request ID:", resp.requestId)
            print("Downloaded to:", downloadPath)
            return downloadPath
        else:
            print("Get Object Failed")
            print("Request ID:", resp.requestId)
            print("Error Code:", resp.errorCode)
            print("Error Message:", resp.errorMessage)
            return None

    except Exception as e:
        print("Get Object Failed")
        print(traceback.format_exc())
        return None

    finally:
        # 关闭 obsClient，确保它被正确创建
        obsClient.close()


def upload(filePath):
    #####！！！！！！！！
    with open("/root/config.yml","r",encoding="utf-8") as file:
        config=yaml.safe_load(file)

        ak = os.getenv(config["obs"]["AccessKeyID"])
        sk = os.getenv(config["obs"]["SecretAccessKey"])

    server = "https://obs.cn-south-1.myhuaweicloud.com"
    #####
    # 创建obsClient实例
    # securityToken值
    obsClient = ObsClient(access_key_id=ak, secret_access_key=sk, server=server)

    try:
        # 上传对象的附加头域
        headers = PutObjectHeader(acl=HeadPermission.PUBLIC_READ_WRITE)
        # 桶名
        bucketName = "comfyui-e07f"
        # 对象名，即上传后的文件名
        objectKey = f"{filePath}"  #后面可能要修改！！！！
        # 待上传文件的完整路径，如aa/bb.txt
        file_path = f"{filePath}"
        # 文件上传
        resp = obsClient.putFile(bucketName, objectKey, file_path, headers)
        # 返回码为2xx时，接口调用成功，否则接口调用失败
        if resp.status < 300:
          print('Put File Succeeded')
          print('requestId:', resp.requestId)
          print('etag:', resp.body.etag)
          print('versionId:', resp.body.versionId)
          print('storageClass:', resp.body.storageClass)
        else:
          print('Put File Failed')
          print('requestId:', resp.requestId)
          print('errorCode:', resp.errorCode)
          print('errorMessage:', resp.errorMessage)
    except:
        print('Put File Failed')
        print(traceback.format_exc())
    finally:
        # 关闭obsClient
        obsClient.close()


def crop(filePath, x, y, width, height, task_id):
    try:
        # 获取原图路径和文件名
        image_path = getPath(filePath)

        # 打开原图
        with Image.open(image_path) as img:
            # 确保裁剪区域在图片范围内
            img_width, img_height = img.size
            if x < 0 or y < 0 or width <= 0 or height <= 0 or x + width > img_width or y + height > img_height:
                raise ValueError("裁剪范围超出图片边界或无效")

            # 裁剪原图
            cropped_img = img.crop((x, y, x + width, y + height))
            
            # 生成裁剪图路径
            cropped_img_path = os.path.join("images/crops", f"crop_task{task_id}.png")
            comfyUI_crop=os.path.join("autodl-tmp/ComfyUI/input",cropped_img_path)
            
            # 保存裁剪图片
            cropped_img.save(cropped_img_path)
            cropped_img.save(comfyUI_crop)

        return cropped_img_path
    
    except Exception as e:
        print(f"裁剪图片出错: {e}")
        return None, None


def process_auto_inpainting(filePath,x,y,width,height,task_id,task_type):   #还要修改一下考虑是否区分自动/手动！！！！！！
    """ 处理 Inpainting 任务 """
    try:
        
        cropped_img_path=crop(filePath=filePath,x=x,y=y,width=width,height=height,task_id=task_id)
        generate_path = os.path.join("images/generates",f"generate_task{task_id}.png")
        
        if task_type == 'SmileAdjustment':
            # 调用 ComfortUI 进行 Inpainting
            command = [
                "python", "auto_smile.py",
                "--image", cropped_img_path,
                "--workflow", "自动微笑.json",
                "--output", generate_path
            ]
            subprocess.run(command, check=True)
        elif task_type == 'EyeOpening':
            # 调用 ComfortUI 进行 Inpainting
            command = [
                "python", "auto_openeyes.py",
                "--image", cropped_img_path,
                "--workflow", "自动睁眼.json",
                "--output", generate_path
            ]
            subprocess.run(command, check=True)
        else:
            # 调用 ComfortUI 进行 Inpainting
            command = [
                "python", "run_comfort_ui.py",
                "--image", cropped_img_path,
                "--workflow", "自动微笑.json",
                "--output", generate_path
            ]
            subprocess.run(command, check=True)
       
        if os.path.exists(generate_path):
            print("文件存在")
            upload(generate_path)
        else:
            print("文件不存在")
       
        generatedUrl=f"https://comfyui-e07f.obs.cn-south-1.myhuaweicloud.com/{generate_path}"
        # # 将 Inpainting 结果合成回原图
        # with Image.open(image_path) as img, Image.open(cropped_img_path) as result_img:
        #     img.paste(result_img, (x, y))
        
        #     final_path = f"/picture/composite/{task_id}_{filename}" 
        #     img.save(final_path)
        #     upload(final_path)    

        # print(f"Inpainting 完成，合成图片路径: {final_path}")
        return generate_path,generatedUrl

    except Exception as e:
        print("Inpaint Failed")
        print(traceback.format_exc())



def uploadmask(mask_base64, task_id,filename):
    try:
        # 确保 mask 目录存在
        mask_dir = "images/masks"
        os.makedirs(mask_dir, exist_ok=True)

        #从obs上下载base64数据
        mask_base64_path,namr=getPath(mask_base64)

        # 解析 Base64 数据
        mask_data = base64.b64decode(mask_base64)

        # 生成 mask 图片路径
        mask_path = os.path.join(mask_dir, f"mask_{task_id}_{filename}")
        
        # 保存图片
        with Image.open(BytesIO(mask_data)) as img:
            img.save(mask_path)

        # 上传到 OBS（如果有上传逻辑）
        # upload(mask_path)  # 你可以实现这个 upload() 方法

        return mask_path  # 或者返回上传后的 URL
    
    except Exception as e:
        print(f"上传 Mask 出错: {e}")
        return None



def process_manual_inpainting(filePath,x,y,width,height,task_id,task_type,mask_path):   #还要修改一下考虑是否区分自动/手动！！！！！！
    """ 处理 Inpainting 任务 """
    try:
        
        cropped_img_path=crop(filePath=filePath,x=x,y=y,width=width,height=height,task_id=task_id)
        mask_path=getPath(mask_path)
        comfyUI_mask=os.path.join("autodl-tmp/ComfyUI/input",mask_path)
        shutil.copy(mask_path,comfyUI_mask)
        
        generate_path = os.path.join("images/generates",f"generate_task{task_id}.png")
        
        if task_type == 'SmileAdjustment':
            # 调用 ComfortUI 进行 Inpainting
            command = [
                "python", "manual_smile.py",
                "--image", cropped_img_path,
                "--mask",mask_path,
                "--workflow","手动微笑.json",
                "--output", generate_path
            ]
            subprocess.run(command, check=True)
        elif task_type == 'EyeOpening':
            # 调用 ComfortUI 进行 Inpainting
            command = [
                "python", "manual_openeyes.py",
                "--image", cropped_img_path,
                "--mask",mask_path,
                "--workflow","手动睁眼.json",
                "--output", generate_path
            ]
            subprocess.run(command, check=True)
        else:
            # 调用 ComfortUI 进行 Inpainting
            command = [
                "python", "manual_smile.py",
                "--image", cropped_img_path,
                "--mask",mask_path,
                "--workflow","手动微笑.json",
                "--output", generate_path
            ]
            subprocess.run(command, check=True)
       
        if os.path.exists(generate_path):
            print("文件存在")
            upload(generate_path)
        else:
            print("文件不存在")
        
        generatedUrl=f"https://comfyui-e07f.obs.cn-south-1.myhuaweicloud.com/{generate_path}"

        # # 将 Inpainting 结果合成回原图
        # with Image.open(image_path) as img, Image.open(cropped_img_path) as result_img:
        #     img.paste(result_img, (x, y))
      
        #     final_path = f"/picture/composite/{task_id}_{filename}" 
        #     img.save(final_path)
        #     upload(final_path)        

        # print(f"Inpainting 完成，合成图片路径: {final_path}")
        return generate_path,generatedUrl

    except Exception as e:
        print("Inpaint Failed")
        print(traceback.format_exc())


def process_super(filePath,task_id,photo_id):
    try:
        # 获取原图路径和文件名
        image_path= getPath(filePath)
        # 定义目标路径
        comfyUI_iamge=os.path.join("autodl-tmp/ComfyUI/input",image_path)
        # 确保目标目录存在
        os.makedirs(os.path.dirname(comfyUI_iamge), exist_ok=True)
        # 拷贝图片到目标路径
        shutil.copy(image_path, comfyUI_iamge)
        # 生成超分辨率图片路径
        generate_path = os.path.join("images/generates",f"generate_task{task_id}.png")
        
        # 调用 ComfortUI 进行 Inpainting
        command = [
            "python", "super_revolution.py",
            "--image", image_path,
            "--workflow", "超分.json",
            "--output", generate_path
        ]
        subprocess.run(command, check=True)

        if os.path.exists(generate_path):
            print("文件存在")
            upload(generate_path)
        else:
            print("文件不存在")

        generatedUrl=f"https://comfyui-e07f.obs.cn-south-1.myhuaweicloud.com/{generate_path}"
        return generatedUrl
    
    except Exception as e:
        print("SR Failed")
        print(traceback.format_exc())       