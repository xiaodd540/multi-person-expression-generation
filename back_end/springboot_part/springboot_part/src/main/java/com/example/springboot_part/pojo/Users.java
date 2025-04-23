package com.example.springboot_part.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString()
public class Users {
    private Integer UserId;
    private String UserName;
    private String Password;
    private Timestamp CreatedAt;
    private String Avatar;

    public Users() {}

    public Users(int userId, String userName, String password, Timestamp createdAt, String avatar) {
        UserId = userId;
        UserName = userName;
        Password = password;
        CreatedAt = createdAt;
        Avatar = avatar;
    }
}
