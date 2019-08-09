package com.njupt.passbook.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>统一的错误信息</h1>
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo<T> {

    /** 统一的错误码标识 */
    public static final Integer ERROR=-1;

    /** 特定的错误码 */
    private Integer code;

    /** 错误信息 */
    private String message;

    /** 请求url */
    private String url;

    /** 请求返回的数据 */
    private T data;


}
