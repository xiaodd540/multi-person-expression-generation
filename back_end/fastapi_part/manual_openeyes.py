import argparse
import json
import os
import requests
import time

# ComfyUI API 服务器地址（如果使用本地 ComfyUI，可以改成 http://127.0.0.1:8188）
COMFYUI_API_URL = "http://localhost:31025/api"


def run_comfyui_workflow(workflow_path, image_path,mask_path):
    """ 发送工作流 JSON 到 ComfyUI API 进行处理 """
    with open(workflow_path, "r", encoding="utf-8") as f:
        workflow_data = json.load(f)

    workflow_data["8"]["inputs"]["image"] = image_path
    workflow_data["33"]["inputs"]["image"] = mask_path  # 修正 'input' 为 'inputs'


    # # 打印工作流数据（调试用）
    # print("工作流数据：")
    # print(json.dumps(workflow_data, indent=4))

    # 构造正确的请求数据
    request_data = {
        "prompt": workflow_data  # 将工作流数据包装在 "prompt" 键中
    }

    data = json.dumps(request_data,indent=4)

    # 发送请求给 ComfyUI API
    try:
        response = requests.post(f"{COMFYUI_API_URL}/prompt", json=request_data)
        response.raise_for_status()  # 检查 HTTP 状态码
    except requests.exceptions.RequestException as e:
        raise RuntimeError(f"请求 ComfyUI 失败: {e}")

    prompt_id = response.json().get("prompt_id")
    if not prompt_id:
        raise RuntimeError("未获取到有效的 Task ID")

    print(f"任务提交成功，Task ID: {prompt_id}")

    # 轮询查询任务状态
    while True:
        try:
            status_response = requests.get(f"{COMFYUI_API_URL}/history/{prompt_id}")
            status_response.raise_for_status()  # 检查 HTTP 状态码
            status_data = status_response.json()

            if prompt_id in status_data:
                outputs = status_data[prompt_id].get("outputs", {})
                images = []

                for node_id, node_output in outputs.items():
                    for image in node_output.get("images", {}):
                        subfolder = image.get("subfolder", "")
                        filename = image["filename"]

                        download_url = f"{COMFYUI_API_URL}/view"
                        params = {
                            "filename": filename,
                            "subfolder": subfolder,
                            "type": "output",
                        }

                        img_response = requests.get(download_url, params=params)
                        print(f"{img_response}\n")
                        if img_response.status_code == 200:
                            images.append({
                                "filename": filename,
                                "content": img_response.content
                            })
                        else:
                            print(f"下载失败：{filename}")
                if images:
                    return images  # 只在成功获取图片时返回
                else:
                    raise RuntimeError("任务完成，但未收到有效的输出图片")

            print("处理中...")
            time.sleep(2)
        except requests.exceptions.RequestException as e:
            raise RuntimeError(f"查询任务状态失败: {e}")

def main():
    parser = argparse.ArgumentParser(description="使用 ComfyUI 运行模型")
    parser.add_argument("--image", required=True, help="输入图片路径")
    parser.add_argument("--mask", required=True, help="输入遮罩路径")
    parser.add_argument("--workflow", required=True, help="ComfyUI 工作流 JSON 文件")
    parser.add_argument("--output", required=True, help="输出图片路径")

    args = parser.parse_args()
    
    # 运行 ComfyUI 工作流
    try:
        result_path = run_comfyui_workflow(args.workflow,args.image,args.mask)
        output="/autodl-tmp/ComfyUI/output"+result_path[0]['filename']
        print(f"Inpainting 处理完成，结果保存至: {output}")
        with open(f"{args.output}", "wb") as file:
             file.write(result_path[0]['content'])
    except Exception as e:
        print(f"处理失败: {e}")

if __name__ == "__main__":
    main()