package com.example.springboot_part.mapper;

import com.example.springboot_part.pojo.CompositePhotos;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CompositePhotosMapper {

    @Select("SELECT * FROM CompositePhotos WHERE CompositePhotoId = #{CompositedPhotoId}")
    CompositePhotos findById(int CompositedPhotoId);

    @Select("SELECT CompositePhotoId FROM CompositePhotos WHERE PhotoId = #{PhotoId}")
    Integer findCompositedPhotoId(int PhotoId);

    @Select("SELECT * FROM CompositePhotos")
    List<CompositePhotos> findAll();

    @Insert("INSERT INTO CompositePhotos (PhotoId,CompositePhotoPath) " +
            "VALUES (#{PhotoId},#{CompositePhotoPath})")
    @Options(useGeneratedKeys = true, keyProperty = "CompositePhotoId")
    int insert(CompositePhotos compositePhotos);

    @Update("UPDATE CompositePhotos SET CompositePhotoPath = #{CompositedPhotoPath} " +
            "WHERE CompositePhotoId = #{CompositedPhotoId}")
    void update(String CompositedPhotoPath, int CompositedPhotoId);

    @Delete("DELETE FROM CompositePhotos WHERE CompositePhotoId = #{CompositedPhotoId}")
    void delete(int CompositedPhotoId);
}


