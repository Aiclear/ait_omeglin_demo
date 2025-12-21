package com.github.blackz;

import lombok.Getter;

/**
 * 获取 token 错误原因
 *
 * @author xinzheyu
 * @since 2025/12/20 12:18
 */
@Getter
public enum LoginError implements ErrorDesc {
    USERNAME_AND_PASSWORD_IS_EMPTY("001", "用户和密码不可为空"),
    USERNAME_OR_PASSWORD_IS_ERROR("002", "用户或者密码错误"),
    ;

    LoginError(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    private final String errorCode;
    private final String message;

    @Override
    public String getCode() {
        return getErrorCode();
    }
}
