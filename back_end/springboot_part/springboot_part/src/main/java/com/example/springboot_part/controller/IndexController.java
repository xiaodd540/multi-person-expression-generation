package com.example.springboot_part.controller;


import com.example.springboot_part.pojo.UserHistory;
import com.example.springboot_part.pojo.Users;
import com.example.springboot_part.service.RedisService;
import com.example.springboot_part.service.UsersService;
import com.example.springboot_part.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IndexController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UsersService usersService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        try {
            Claims claims = jwtUtil.parseToken(refreshToken.replace("Bearer ", ""));
            String newAccessToken = jwtUtil.generateToken(claims.getSubject());
            return ResponseEntity.ok(Map.of("token", newAccessToken));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkToken(@RequestHeader("Authorization") String token) {
        try {
            jwtUtil.parseToken(token.replace("Bearer ", ""));
            return ResponseEntity.ok().build();
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/data")
    public ResponseEntity<?> data(@RequestParam Integer userId) {
        try {
            Map<Object,Object> user=redisService.getUser(userId);
            System.out.println("用户的信息在redis："+user);
            Map<String, List<String>> list= redisService.getUserTempBySource(userId);
            System.out.println("临时的上传的历史记录：\n"+list);
            if (user == null) {
                Users users=usersService.findById(userId);
                usersService.insert(users);
                String avatar=users.getAvatar();
                String userName=users.getUserName();
                if ((avatar==null||avatar.equals("null"))){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
                else {
                    return ResponseEntity.ok().body(Map.of("avatar", avatar,"userName", userName ,"data", list));
                }
            }
            else {
                user=redisService.getUser(userId);
                String avatar=String.valueOf(user.get("Avatar"));
                String userName=String.valueOf(user.get("Username"));
                System.out.println("头像的值:"+avatar);
                if ((avatar==null||avatar.equals("null"))){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
                else {
                    return ResponseEntity.ok().body(Map.of("avatar", avatar,"data", list,"userName", userName ));
                }
            }
        }catch (Exception e) {  
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/history")
    public ResponseEntity<?> history(@RequestBody UserHistory userHistory) {
        try {
            int userId=userHistory.getUserId();
            int page=userHistory.getPage();
            int size=userHistory.getSize();
            // 计算 Redis 查询的 start 和 end 索引
            int start = (userHistory.getPage() - 1) * size;
            int end = start + size - 1;

            // 查询 Redis 里的 ZSet，按上传时间排序
            List<Map<String,Object>> photos = redisService.getUserPhotos(userId, start, end);

            // 计算总数
            long total = redisService.countUserPhotos(userId);

            // 构建分页结果
            Map<String, Object> response = new HashMap<>();
            response.put("photos", photos);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) total / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/resource/{userId}")
    public ResponseEntity<?> resource(@PathVariable  Integer userId) {
        try {
            Map<String, List<String>> list=redisService.getUserPhotosBySource(userId);
            return ResponseEntity.ok().body(list);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


