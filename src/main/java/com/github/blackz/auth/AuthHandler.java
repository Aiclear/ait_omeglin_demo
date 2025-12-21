package com.github.blackz.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.blackz.LoginError;
import com.github.blackz.ResultDto;
import com.github.blackz.db.entity.User;
import com.github.blackz.auth.dao.UserRepository;
import com.github.blackz.jwt.JwtUtils;
import com.github.blackz.security.UserInformation;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 认证 授权
 *
 * @author xinzheyu
 * @since 2025/12/20 12:06
 */
public class AuthHandler {

    /**
     * 用户注册
     */
    public static void register(@NotNull Context context) {
        User user = context.bodyAsClass(User.class);
        user.setCode(user.getEmail().split("@")[0]);

        UserRepository.saveUser(user);
    }

    /**
     * 登录时生成对应的 token
     */
    public static void token(Context context) {
        // 获取用户的认证信息
        UserCredentials userCredentials = context.bodyAsClass(UserCredentials.class);

        // 基本属于验证不通过
        if (null == userCredentials || !userCredentials.validate()) {
            context.json(ResultDto.from(LoginError.USERNAME_AND_PASSWORD_IS_EMPTY));
            return;
        }

        // 基本输入校验通过，用户登录信息验证
        User user = UserRepository.findUserByEmail(userCredentials.username);
        if (null == user) {
            context.json(ResultDto.from(LoginError.USERNAME_OR_PASSWORD_IS_ERROR));
            return;
        }

        // 如果用户存在则对比密码
        if (!userCredentials.password.equals(user.getPassword())) {
            context.json(ResultDto.from(LoginError.USERNAME_OR_PASSWORD_IS_ERROR));
            return;
        }

        // 设置对应的 token
        context.cookie(new Cookie(
            "Authorization",
            JwtUtils.generateToken(UserInformation.initWith(user)),
            "/",
            -1,
            false,
            0,
            true,
            null,
            null,
            SameSite.LAX
        ));

        user.setPassword(null);
        context.json(ResultDto.ok(user));
    }

    public static void refreshToken(Context context) {
    }

    public static void logout(Context context) {
        // 验证 token
        UserInformation _ = JwtUtils.validateToken(context);
        // 删除 token
        context.removeCookie("Authorization");
        // 重定向至 login页面
        context.json(ResultDto.ok());
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class UserCredentials {

        String username;
        String password;

        public boolean validate() {
            return !(StringUtils.isEmpty(username) || StringUtils.isEmpty(password));
        }
    }
}
