package com.guxian.result;

import lombok.Data;

/**
 * 全局统一返回结果封装
 * @param <T> 泛型返回数据
 */
@Data
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    // 成功无返回数据
    public static <T> Result<T> success() {
        return build(200, "操作成功", null);
    }

    // 成功携带数据
    public static <T> Result<T> success(T data) {
        return build(200, "操作成功", data);
    }

    // 成功自定义提示 + 数据
    public static <T> Result<T> success(String msg, T data) {
        return build(200, msg, data);
    }

    // 失败自定义错误码+信息
    public static <T> Result<T> fail(Integer code, String msg) {
        return build(code, msg, null);
    }

    // 失败默认500错误码
    public static <T> Result<T> fail(String msg) {
        return build(500, msg, null);
    }

    private static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}