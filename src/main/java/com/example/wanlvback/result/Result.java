package com.example.wanlvback.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; // 响应码 200成功，500失败

    private String msg; // 响应消息

    private T data; // 响应数据

    /**
     * 返回成功结果，不携带数据。
     *
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "success";
        return result;
    }

    /**
     * 返回成功结果，携带数据。
     *
     * @param object 返回数据
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<>();
        result.data = object;
        result.code = 200;
        result.msg = "success";
        return result;
    }

    /**
     * 返回失败结果。
     *
     * @param msg 错误信息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = 500;
        return result;
    }
}
