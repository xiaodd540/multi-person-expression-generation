package com.example.springboot_part.service.impl;

import com.example.springboot_part.mapper.CroppedFacesMapper;
import com.example.springboot_part.pojo.CroppedFaces;
import com.example.springboot_part.service.CroppedFacesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CroppedFacesServiceImpl implements CroppedFacesService {

    @Autowired
    private CroppedFacesMapper croppedFacesMapper;

    @Override
    public CroppedFaces findById(int FaceId) {
        return croppedFacesMapper.findById(FaceId);
    }

    @Override
    public List<CroppedFaces> findAll() {
        return croppedFacesMapper.findAll();
    }

    @Override
    public int insert(CroppedFaces croppedFace) {
        return croppedFacesMapper.insert(croppedFace);
    }

    @Override
    public void update(CroppedFaces croppedFace) {
//        croppedFacesMapper.update(croppedFace);
    }

    @Override
    public void delete(int FaceId) {
        croppedFacesMapper.delete(FaceId);
    }
}
