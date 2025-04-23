package com.example.springboot_part.service;

import com.example.springboot_part.pojo.generated_photos;

import java.util.List;

public interface GeneratedPhotosService {
    generated_photos findById(int GeneratedPhotoId);
    List<generated_photos> findAll();
    int insert(generated_photos generatedPhotos);
    void update(int GeneratedPhotoId, String GeneratedPath);
    void delete(int GeneratedPhotoId);
}
