package com.example.springboot_part.service;

import com.example.springboot_part.pojo.Users;

import java.util.List;

public interface UsersService {
    Users findByName(String Username);
    Users findById(int id);
    List<Users> findAll();
    int insert(Users user);
    void update(Users user);
    void delete(int UserId);
}
