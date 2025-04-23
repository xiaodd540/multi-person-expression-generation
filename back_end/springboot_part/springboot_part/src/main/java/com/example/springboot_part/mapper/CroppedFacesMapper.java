package com.example.springboot_part.mapper;

import com.example.springboot_part.pojo.CroppedFaces;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CroppedFacesMapper {

    @Select("SELECT * FROM CroppedFaces WHERE FaceId = #{FaceId}")
    CroppedFaces findById(int FaceId);

    @Select("SELECT PhotoId FROM CroppedFaces WHERE FaceId = #{FaceId}")
    int findPhotoId(int FaceId);

    @Select("SELECT * FROM CroppedFaces")
    List<CroppedFaces> findAll();

    @Insert("INSERT INTO CroppedFaces (PhotoId, X, Y, Width, Height) " +
            "VALUES (#{PhotoId}, #{X}, #{Y}, #{Width}, #{Height})")
    @Options(useGeneratedKeys = true, keyProperty = "FaceId")
    int insert(CroppedFaces croppedFaces);

    @Update("UPDATE CroppedFaces SET FacePath = #{FacePath} " +
            "WHERE FaceId = #{FaceId}")
    void update(int FaceId, String FacePath);

    @Delete("DELETE FROM CroppedFaces WHERE FaceId = #{FaceId}")
    void delete(int FaceId);
}
