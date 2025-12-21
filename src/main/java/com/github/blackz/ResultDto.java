package com.github.blackz;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应对象
 *
 * @author xinzheyu
 * @since 2025/12/20 12:16
 */
@Data
@NoArgsConstructor
public class ResultDto {

    public static final String SUCCESS_CODE = "200";
    public static final String ERROR_CODE = "999";

    private String code;
    private String message;
    private Object data;


    public ResultDto(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ResultDto ok() {
        return new ResultDto(SUCCESS_CODE, "", null);
    }

    public static ResultDto ok(Object data) {
        return new ResultDto(SUCCESS_CODE, "", data);
    }

    public static ResultDto error() {
        return new ResultDto(ERROR_CODE, "服务器异常请联系管理员", null);
    }

    public static ResultDto from(ErrorDesc error) {
        return new ResultDto(error.getCode(), error.getMessage(), null);
    }

    public static ResultDto with(String code, String message) {
        return ResultDto.with(code, message, null);
    }

    public static ResultDto with(String code, String message, Object data) {
        return new ResultDto(code, message, data);
    }
}
