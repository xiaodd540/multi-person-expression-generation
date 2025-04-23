package com.example.springboot_part.service;

import com.example.springboot_part.pojo.Photos;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface PhotosService {
    Photos getPhoto(int photoId) throws JsonProcessingException;
    List<Photos> findAll();
    int uploadPhoto(String photoPath, int userId,String sha256Hash);
    void update(Photos photo);
    boolean deletePhoto(int photoId);
    boolean IsExistPhoto(String sha256Hash);
    String obsUploadPhoto(String sha256Hash);
}

