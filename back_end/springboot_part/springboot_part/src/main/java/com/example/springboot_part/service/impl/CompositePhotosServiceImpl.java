package com.example.springboot_part.service.impl;

import com.example.springboot_part.mapper.CompositePhotosMapper;
import com.example.springboot_part.pojo.CompositePhotos;
import com.example.springboot_part.service.CompositePhotosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompositePhotosServiceImpl implements CompositePhotosService {

    @Autowired
    private CompositePhotosMapper compositePhotosMapper;

    @Override
    public CompositePhotos findById(int CompositedPhotoId) {
        return compositePhotosMapper.findById(CompositedPhotoId);
    }

    @Override
    public List<CompositePhotos> findAll() {
        return compositePhotosMapper.findAll();
    }

    @Override
    public int insert(CompositePhotos compositePhotos) {
        return compositePhotosMapper.insert(compositePhotos);
    }

    @Override
    public void update(int CompositedPhotoId,String compositedPath) {
        compositePhotosMapper.update(compositedPath,CompositedPhotoId);
    }

    @Override
    public void delete(int CompositedPhotoId) {
        compositePhotosMapper.delete(CompositedPhotoId);
    }
}
