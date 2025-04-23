package com.example.springboot_part.mapper;

import com.example.springboot_part.pojo.Photos;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PhotosMapper {

    @Select("SELECT * FROM Photos WHERE PhotoId = #{PhotoId}")
    Photos findById(int PhotoId);

    @Select("SELECT UserId FROM Photos WHERE PhotoId = #{PhotoId}")
    int findUserId(int PhotoId);

    @Select("SELECT * FROM Photos WHERE PhotosHash = #{PhotoHash}")
    List<Photos> findByHash(String PhotoHash);

    @Select("SELECT * FROM Photos")
    List<Photos> findAll();

    @Insert("INSERT INTO Photos (UserId, PhotoPath, PhotosHash) " +
            "VALUES (#{UserId}, #{PhotoPath}, #{PhotosHash})")
    @Options(useGeneratedKeys = true, keyProperty = "PhotoId")
    int insert(Photos photo);  //可以改成int 传photo_id

    @Update("UPDATE Photos SET Status = #{Status} " +
            "WHERE PhotoId = #{PhotoId}")
    void update(Photos photos);

    @Delete("DELETE FROM Photos WHERE PhotoId = #{PhotoId}")
    void delete(int PhotoId);
}
