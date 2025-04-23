package com.example.springboot_part.service;

import com.example.springboot_part.pojo.CompositePhotos;

import java.util.List;

public interface CompositePhotosService {
    CompositePhotos findById(int CompositedPhotoId);
    List<CompositePhotos> findAll();
    int insert(CompositePhotos compositePhotos);
    void update(int compositedPhotoId, String compositedPath);
    void delete(int CompositedPhotoId);
}
