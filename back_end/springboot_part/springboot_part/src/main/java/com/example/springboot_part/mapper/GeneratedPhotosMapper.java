package com.example.springboot_part.mapper;

import com.example.springboot_part.pojo.generated_photos;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GeneratedPhotosMapper {

    @Select("SELECT * FROM generated_photos WHERE GeneratedPhotoId = #{GeneratedPhotoId}")
    generated_photos findById(int GeneratedPhotoId);

    @Select("SELECT GeneratedPhotoId FROM generated_photos WHERE TaskId = #{TaskId}")
    int findGeneratedPhotoId(int TaskId);

    @Select("SELECT * FROM generated_photos")
    List<generated_photos> findAll();

//    @Insert("INSERT INTO generated_photos (TaskId) " +
//            "VALUES (#{TaskId})")
//    @Options(useGeneratedKeys = true, keyProperty = "GeneratedPhotoId")
//    void insert(int TaskId);
    @Insert("INSERT INTO generated_photos (TaskId, GeneratedPhotoPath, CreatedTime) VALUES (#{TaskId},#{GeneratedPhotoPath},#{CreatedTime})")
    @Options(useGeneratedKeys = true, keyProperty = "GeneratedPhotoId")
//    @Select("SELECT LAST_INSERT_ID()") // 获取生成的主键值
    Integer insert(generated_photos generated_photos);

    @Update("UPDATE generated_photos SET GeneratedPhotoPath = #{GeneratedPhotoPath}" +
            " WHERE GeneratedPhotoId = #{GeneratedPhotoId}")
    void update(int GeneratedPhotoId, String GeneratedPhotoPath);

    @Delete("DELETE FROM generated_photos WHERE GeneratedPhotoId = #{GeneratedPhotoId}")
    void delete(int GeneratedPhotoId);
}
