package com.example.springboot_part.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
public class FastAPIService {

    private final RestTemplate restTemplate;

    // FastAPI 的 URL 地址，可以通过配置文件管理
    private String fastApiUrl="http://localhost:6006";

    public FastAPIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 发送 GET 请求到 FastAPI，并返回响应结果
    public String getFastAPIResponse() {
        // 发送请求到 FastAPI 服务
        ResponseEntity<String> response = restTemplate.getForEntity(fastApiUrl + "/", String.class);

        // 返回 FastAPI 返回的字符串
        return response.getBody();
    }
}

