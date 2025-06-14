package com.cosmos.aiagent.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {


    private int code;   //响应码
    private String msg; //响应信息
    private Object data;    //响应数据
    private String description; //响应描述

    //请求成功无数据

    /**
     * 返回成功的信息，无数据
     *
     * @param msg 请求成功的信息，无数据
     * @return 返回成功
     */
    public static Result success(String msg) {
        return new Result(200, msg, null, "");
    }


    /**
     * 请求成功有数据
     *
     * @param msg  请求成功的信息
     * @param data 请求成功的数据
     * @return 返回成功
     */
    //请求成功有数据
    public static Result success(String msg, Object data) {
        return new Result(200, msg, data, "");
    }


    /**
     * 用于返回自定义的错误信息
     *
     * @param msg 自定义错误信息
     * @return 返回错误
     */
    public static Result error(String msg) {
        return new Result(500, msg, null, "");
    }


    public static Result error(int code, String msg) {
        return new Result(code, msg, null, "");
    }


}
