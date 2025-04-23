package com.example.springboot_part.service.impl;

import com.example.springboot_part.mapper.UsersMapper;
import com.example.springboot_part.pojo.Users;
import com.example.springboot_part.service.RedisService;
import com.example.springboot_part.service.UsersService;
import com.example.springboot_part.utils.ObsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersServiceImpl implements UsersService, UserDetailsService {

    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ObsUtils obsUtils;

    @Override
    public Users findByName(String username) {
        return usersMapper.findByName(username);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public Users findById(int id) {
        return usersMapper.findById(id);
    }


    @Override
    public List<Users> findAll() {
        return usersMapper.findAll();
    }

    @Override
    public int insert(Users user) {
        usersMapper.insert(user);
        user=usersMapper.findById(user.getUserId());
        redisService.saveUser(user);
        redisService.saveALL(user.getUserId());
        return user.getUserId();
    }

    @Override
    public void update(Users user) {
        usersMapper.update(user);
    }

    @Override
    public void delete(int UserId) {
        usersMapper.delete(UserId);
    }

    /**
     * @param  username
     * @return  user
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usersMapper.findByUserName(username);
    }
}

