package com.itick.client.model;

import lombok.Data;
import java.util.List;

/**
 * API响应模型
 * 用于封装API的响应数据
 */
@Data
public class ApiResponse<T> {
    private int code;       // 响应码 0:成功
    private String msg;     // 异常信息 不为0时存在
    private T data;         // 响应数据
}
