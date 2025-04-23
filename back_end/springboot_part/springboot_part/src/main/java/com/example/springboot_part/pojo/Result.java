package com.example.springboot_part.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class Result<T> {
    private String status;  // 状态：success 或 error
    private String message; // 返回的消息
    private T data;         // 返回的数据

    // 成功静态方法，带数据
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setStatus("success");
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 成功静态方法，不带数据
    public static <T> Result<T> success(String message) {
        return success(message, null);
    }

    // 错误静态方法
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setStatus("error");
        result.setMessage(message);
        result.setData(null);
        return result;
    }
}
