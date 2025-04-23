package com.example.springboot_part.service;

import com.example.springboot_part.pojo.CroppedFaces;

import java.util.List;

public interface CroppedFacesService {
    CroppedFaces findById(int FaceId);
    List<CroppedFaces> findAll();
    int insert(CroppedFaces croppedFace);
    void update(CroppedFaces croppedFace);
    void delete(int FaceId);
}
