package com.example.springboot_part.controller;

import com.example.springboot_part.pojo.Result;
import com.example.springboot_part.pojo.Users;
import com.example.springboot_part.service.RedisService;
import com.example.springboot_part.service.UsersService;
import com.example.springboot_part.utils.JwtUtil;

import com.example.springboot_part.utils.ObsUtils;
import com.sun.net.httpserver.Authenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UsersService usersService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ObsUtils obsUtils;

    @PostMapping("/login")
    public Result<?> Login(@RequestBody Users request) {
        System.out.println("开始登录");
        try {
            Users users = usersService.findByName(request.getUserName());
            if (users == null) {
                return Result.error("用户不存在");
            }
            if(redisService.getUser(users.getUserId()).isEmpty()){
                redisService.saveUser(users);
            }
            if (passwordEncoder.matches(request.getPassword(), users.getPassword())) {
                System.out.println("已经进入这个登录成功的里面了");
                String token = jwtUtil.generateToken(users.getUserName());
                String refreshToken = jwtUtil.generateRefreshToken(users.getUserName());
                return Result.success("登录成功", Map.of("token", token, "refreshToken", refreshToken,"userId", users.getUserId()));
            }
            return Result.error("密码错误");
        } catch (Exception e) {
            return Result.error("系统异常: " + e.getMessage());
        }
    }

    @GetMapping("/check-username")
    public boolean findByName(@RequestParam String username) {
        Users users = usersService.findByName(username);
        return users == null;
    }


    @PostMapping("/register")
    public Integer insert(@RequestBody Users user) {
        if (user == null) {
            return 0;
        }
        String new_password=passwordEncoder.encode(user.getPassword());
        user.setPassword(new_password);
        if (user.getAvatar().isEmpty()) {
            user.setAvatar("https://comfyui-e07f.obs.cn-south-1.myhuaweicloud.com/picture/img.png");
        }
        return usersService.insert(user);
    }

    @PostMapping("/avatar")
    public String uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            return obsUtils.uploadAvatar(file);
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/password")
    public Result<?> updatePassword(@RequestBody Users user) {
        try {
            Users users = usersService.findByName(user.getUserName());
            if (users == null) {
                return Result.error("用户不存在");
            }
            users.setPassword(passwordEncoder.encode(user.getPassword()));
            redisService.saveUser(users);
            usersService.update(users);
            return Result.success("修改成功");
        } catch (Exception e) {
            return Result.error("系统异常: " + e.getMessage());
        }
    }

    @PostMapping("/changeAvatar")
    public String changeAvatar(@RequestBody Users user) {
        try {
            System.out.println("用户修改头像的信息："+user.getUserId()+" "+user.getAvatar());
            Users users = usersService.findById(user.getUserId());
            if (users == null) {
                return "没有找到目标";
            }
            users.setAvatar(user.getAvatar());
            redisService.saveUser(users);
            usersService.update(users);
            return "上传成功";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PutMapping
    public String update(@RequestBody Users user) {
        usersService.update(user);
        return "User updated successfully!";
    }

    @DeleteMapping("/{UserId}")
    public String delete(@PathVariable int UserId) {
        usersService.delete(UserId);
        return "User deleted successfully!";
    }
}
