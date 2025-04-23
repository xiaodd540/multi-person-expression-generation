package com.example.springboot_part.mapper;

import com.example.springboot_part.pojo.Tasks;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TasksMapper {

    @Select("SELECT * FROM Tasks WHERE TaskId = #{TaskId}")
    Tasks findById(int TaskId);

    @Select("SELECT * FROM Tasks")
    List<Tasks> findAll();

    @Insert("INSERT INTO Tasks (FaceId, EndTime,TaskType, TaskModel) " +
            "VALUES (#{FaceId}, #{EndTime}, #{TaskType}, #{TaskModel})")
    @Options(useGeneratedKeys = true, keyProperty = "TaskId")
    int insert(Tasks task);

    @Update("UPDATE Tasks SET FaceId = #{FaceId}, StartTime = #{StartTime}, EndTime = #{EndTime}, " +
            "Status = #{Status}, TaskType = #{TaskType}, TaskModel = #{TaskModel} WHERE TaskId = #{TaskId}")
    void update(Tasks task);

    @Delete("DELETE FROM Tasks WHERE TaskId = #{TaskId}")
    void delete(int TaskId);
}
