package com.example.springboot_part.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class UserHistory {
    private int userId;
    private int page = 1;  // 默认值
    private int size = 10; // 默认值

    public UserHistory() {
    }

    public UserHistory(int userId, int page, int size) {
        this.userId = userId;
        this.page = page;
        this.size = size;
    }
}
