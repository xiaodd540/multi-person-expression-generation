package com.example.springboot_part.service.impl;

import com.example.springboot_part.mapper.GeneratedPhotosMapper;
import com.example.springboot_part.pojo.generated_photos;
import com.example.springboot_part.service.GeneratedPhotosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeneratedPhotosServiceImpl implements GeneratedPhotosService {

    @Autowired
    private GeneratedPhotosMapper generatedPhotosMapper;

    @Override
    public generated_photos findById(int GeneratedPhotoId) {
        return generatedPhotosMapper.findById(GeneratedPhotoId);
    }

    @Override
    public List<generated_photos> findAll() {
        return generatedPhotosMapper.findAll();
    }

    @Override
    public int insert(generated_photos generatedPhotos) {
        return generatedPhotosMapper.insert(generatedPhotos);
    }

    @Override
    public void update(int GeneratedPhotoId,String GeneratedPath) {
        generatedPhotosMapper.update(GeneratedPhotoId,GeneratedPath);

    }

    @Override
    public void delete(int GeneratedPhotoId) {
        generatedPhotosMapper.delete(GeneratedPhotoId);
    }
}
