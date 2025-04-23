package com.example.springboot_part.mapper;

import com.example.springboot_part.pojo.Users;
import org.apache.ibatis.annotations.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Mapper
public interface UsersMapper {

    @Select("SELECT * FROM Users WHERE UserName = #{Username}")
    UserDetails findByUserName(String Username);

    @Select("SELECT * FROM Users WHERE UserName = #{Username}")
    Users findByName(String Username);

    @Select("SELECT * FROM Users WHERE UserId = #{UserId}")
    Users findById(Integer UserId);

    @Select("SELECT * FROM Users")
    List<Users> findAll();

    @Insert("INSERT INTO Users (UserName, Password,Avatar) VALUES (#{UserName}, #{Password},#{Avatar})")
    @Options(useGeneratedKeys = true, keyProperty = "UserId")
    int insert(Users user);

    @Update("UPDATE Users SET UserName = #{UserName}, Password = #{Password} , Avatar = #{Avatar} WHERE UserId = #{UserId}")
    void update(Users user);

    @Delete("DELETE FROM Users WHERE UserId = #{UserId}")
    void delete(int UserId);
}
