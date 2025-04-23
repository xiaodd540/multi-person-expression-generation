package com.example.springboot_part;

import com.example.springboot_part.mapper.PhotosMapper;
import com.example.springboot_part.pojo.Photos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class SpringbootPartApplicationTests {
    @Autowired
    private PhotosMapper photosMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
////        userMapper.Add(3,"Duck",18);
////        userServiceImpl.InsertUser(new User(4,"Tanta",18));
//         Users user = new Users();
//         user.setUserName("test");
//         user.setPassword("123456");
//         user.setAvatar("test");
//          userService.insert(user);
         Photos photos = new Photos();
         photos.setUserId(1);
         photos.setPhotosHash("c255f792df4547309d07c9e5224799cca9b56de4a9799375a4bb9908ca6a1d72");
         photos.setPhotoPath("https://comfyui-e07f.https://obs.cn-south-1.myhuaweicloud.com/e0f1cb29-5b16-42a4-b7d2-e9993719ac2a_1.png");
         photosMapper.insert(photos);
    }
    @Test
    void getEncode() {
        System.out.println(passwordEncoder.encode("123456"));
    }

}
