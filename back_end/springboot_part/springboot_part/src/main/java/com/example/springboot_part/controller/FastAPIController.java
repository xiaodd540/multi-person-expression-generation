package com.example.springboot_part.controller;

import com.example.springboot_part.service.FastAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FastAPIController {

    private final FastAPIService fastAPIService;

    @Autowired
    public FastAPIController(FastAPIService fastAPIService) {
        this.fastAPIService = fastAPIService;
    }

    // 创建一个路由来测试从 FastAPI 获取响应
    @GetMapping("/testFastAPI")
    public String testFastAPI() {
        // 获取 FastAPI 的响应
        String response = fastAPIService.getFastAPIResponse();

        // 返回 FastAPI 的响应内容
        return "FastAPI 返回的消息: " + response;
    }
}