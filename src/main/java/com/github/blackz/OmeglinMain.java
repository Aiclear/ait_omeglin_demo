package com.github.blackz;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

import com.github.blackz.auth.AuthHandler;
import com.github.blackz.db.OmeglinSchema;
import com.github.blackz.friend.FriendHandler;
import com.github.blackz.security.SecurityContext;
import com.github.blackz.security.SecurityHandler;
import com.github.blackz.user.UserHandler;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OmeglinMain {

    static void main() {
        // init schema
        OmeglinSchema.init();

        Javalin.create(config -> {
            config.staticFiles.add("src/main/resources/public", Location.EXTERNAL);

            // security filter
            config.router.mount(router -> {
                // 授权验证 token解析，设置对象的认证上下文
                router.before(new SecurityHandler(config.router));

                // 请求完成之后
                router.after(_ -> SecurityContext.removeContext());
            });

            // api
            config.router.apiBuilder(() -> {
                // auth
                path("/api/auth", () -> {
                    post("/register", AuthHandler::register);
                    post("/token", AuthHandler::token);
                    post("/refreshToken", AuthHandler::refreshToken);
                    post("/logout", AuthHandler::logout);
                });

                // user
                path("/api/user", () -> {
                    post("/userInfo", UserHandler::queryUserInfo);
                });

                // friends
                path("/api/friends", () -> {
                    post("/list", FriendHandler.getFriendsList);
                    post("/pending", FriendHandler.getPendingRequests);
                });
            });

            // ws
            config.router.mount(router -> {
                router.ws("/api/matchmaking", Matchmaking::websocket);
            });
        }).exception(Exception.class, (e, context) -> {
            // 处理 token过期的异常
            if (e instanceof ExpiredJwtException) {
                context.redirect("/login.html");
            } else {
                log.error("An error occurred while processing the request", e);
                context.json(ResultDto.error());
            }
        }).start(7070);
    }
}
