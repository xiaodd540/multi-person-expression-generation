package com.example.springboot_part.utils;

import com.obs.services.ObsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import static com.obs.services.model.AccessControlList.REST_CANNED_PUBLIC_READ_WRITE;

@Component
public class ObsUtils {
    @Autowired
    private ObsClient obsClient;
    @Value("${huawei.obs.bucket-name}")
    private String bucketName;
    @Value("${huawei.obs.endpoint}")
    private String endpoint;

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename(); //修改一下存储的名称
        obsClient.putObject(bucketName, "images/img/"+fileName, file.getInputStream());  //存储到OBS上
        obsClient.setObjectAcl(bucketName,"images/img/"+fileName,REST_CANNED_PUBLIC_READ_WRITE);
        return "https://comfyui-e07f.obs.cn-south-1.myhuaweicloud.com/images/img/"+fileName;
//        return String.format("https://%s.%s/%s", bucketName, endpoint, "images/img/"+fileName);
    }

    public String uploadAvatar(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename(); //修改一下存储的名称
        obsClient.putObject(bucketName, "picture/"+fileName, file.getInputStream());  //存储到OBS上
        obsClient.setObjectAcl(bucketName,"picture/"+fileName,REST_CANNED_PUBLIC_READ_WRITE);
        return "https://comfyui-e07f.obs.cn-south-1.myhuaweicloud.com/picture/"+fileName;
//        return String.format("https://%s.%s/%s", bucketName, endpoint, "images/img/"+fileName);
    }

   public String getFileUrl(String fileName) {
       return String.format("https://%s.%s/%s", bucketName, endpoint, fileName);
   }

   public String uploadMaskbase64(String maskbase64,int taskId)throws IOException {
        String fileName = taskId+"_mask.png";
        String[] parts = maskbase64.split(",");
        maskbase64 = parts.length > 1 ? parts[1] : maskbase64;
       System.out.println("maskbase64:"+maskbase64);
        byte[] decodedBytes = Base64.getDecoder().decode(maskbase64);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
        obsClient.putObject(bucketName, "images/masks/"+fileName, inputStream);
        obsClient.setObjectAcl(bucketName,"images/masks/"+fileName,REST_CANNED_PUBLIC_READ_WRITE);
        return "https://comfyui-e07f.obs.cn-south-1.myhuaweicloud.com/images/masks/"+fileName;
   }
}
