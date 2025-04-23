package com.example.springboot_part.service.impl;

import com.example.springboot_part.mapper.PhotosMapper;
import com.example.springboot_part.pojo.Photos;
import com.example.springboot_part.service.PhotosService;
import com.example.springboot_part.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhotosServiceImpl implements PhotosService {

    @Autowired
    private PhotosMapper photosMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 查找图片：先查 Redis，再查 MySQL
     */
    @Override
    public Photos getPhoto(int photoId) throws JsonProcessingException {
        // 1. Redis 查询
        Photos photo = (Photos) redisService.getPhoto(photoId);
        if (photo != null) {
            return photo;
        }

        // 2. MySQL 查询
        Photos optionalPhoto = photosMapper.findById(photoId);
        if (optionalPhoto!=null) {
            photo = optionalPhoto;
            // 写入 Redis
            redisService.savePhoto(photo);
            return photo;
        }

        return null;
    }

    @Override
    public List<Photos> findAll() {
        return photosMapper.findAll();
    }

    /**
     * 上传图片：保存到 MySQL 和 Redis，返回 PhotoId
     */
    @Override
    public int uploadPhoto(String photoPath, int userId, String sha256Hash) {
        Photos photo = new Photos();
//        String sha256Hash = calculateSHA256(photoPath);
        photo.setUserId(userId);
        photo.setPhotoPath(photoPath);
//        photo.setUploadTime(Timestamp.from(Instant.now()));  //数据库会自动生成
//        photo.setStatus(Photos.Status.Pending); // 初始状态为 Pending
        photo.setPhotosHash(sha256Hash);

        // 存储到数据库
        photosMapper.insert(photo);

        photo=photosMapper.findById(photo.getPhotoId());

        // 保存到 Redis
        redisService.savePhoto(photo);
        redisService.addUserPhoto(userId,photo.getPhotoId(),photo.getUploadTime());

        // 返回 PhotoId
        return photo.getPhotoId();
    }

    @Override
    public void update(Photos photo) {
        photosMapper.update(photo);
    }

    /**
     * 删除图片：MySQL 和 Redis 都删除
     */
    @Override
    public boolean deletePhoto(int photoId) {
        if (photosMapper.findById(photoId)!=null) {
            // 1. 删除 MySQL
            photosMapper.delete(photoId);

            // 2. 删除 Redis
            redisService.deletePhoto(photoId);

            return true;
        }
        return false;
    }

    /**
     * 判断是否有重复的图片内容
     */
    @Override
    public boolean IsExistPhoto(String Sha256Hash) {
        if (photosMapper.findByHash(Sha256Hash).isEmpty()){
            return false;
        };
        return true;
    }

    /**
     * 判断是否存储图片
     */
    @Override
    public String obsUploadPhoto(String sha256Hash) {
        return photosMapper.findByHash(sha256Hash).get(0).getPhotoPath();
    }
}
