package com.myforum.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "ok", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    // Common error codes
    public static final int CODE_PARAM_ERROR = 40001;
    public static final int CODE_UNAUTHORIZED = 40100;
    public static final int CODE_FORBIDDEN = 40300;
    public static final int CODE_NOT_FOUND = 40400;
    public static final int CODE_SERVER_ERROR = 50000;
}
