package com.github.blackz.security;

import com.github.blackz.jwt.JwtUtils;
import io.javalin.config.RouterConfig;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.router.matcher.PathParser;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * http request url security filter
 *
 * @author xinzheyu
 * @since 2025/12/20 9:54
 */
public class SecurityHandler implements Handler {

    private static final List<SecurityUrlConfig> SECURITY_URL_CONFIG_LIST = new ArrayList<>();

    private final RouterConfig routerConfig;


    public SecurityHandler(RouterConfig routerConfig) {
        this.routerConfig = routerConfig;
        this.initSecurityConfig();
    }

    private void initSecurityConfig() {
        List<SecurityUrlConfig> objects = List.of(
            SecurityUrlConfig.with("/login.html", false, routerConfig),
            SecurityUrlConfig.with("/api/auth/register", false, routerConfig),
            SecurityUrlConfig.with("/api/auth/token", false, routerConfig),
            SecurityUrlConfig.with("/api/auth/refreshToken", false, routerConfig),
            SecurityUrlConfig.with("/**/*.css", false, routerConfig),
            SecurityUrlConfig.with("/**/*.js", false, routerConfig),
            SecurityUrlConfig.with("*", true, routerConfig)
        );

        SECURITY_URL_CONFIG_LIST.addAll(objects);
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        // 获取请求路径，判断是否需要进行安全拦截
        String reqPath = ctx.path();

        // 验证路径
        for (SecurityUrlConfig securityUrlConfig : SECURITY_URL_CONFIG_LIST) {
            if (securityUrlConfig.matches(reqPath)) {
                // 不需要认证
                if (!securityUrlConfig.authentication) {
                    return;
                }

                UserInformation userInformation = JwtUtils.validateToken(ctx);
                if (userInformation == null) {
                    ctx.redirect("/login.html");
                } else {
                    SecurityContext.setContext(userInformation);
                }

                break;
            }
        }
    }


    @Data
    static class SecurityUrlConfig {

        private String url;
        /**
         * 是否需要认证
         */
        private boolean authentication;

        private PathParser pathParser;

        public SecurityUrlConfig(String url, boolean authentication, RouterConfig routerConfig) {
            this.url = url;
            this.authentication = authentication;
            this.pathParser = new PathParser(url, routerConfig);
        }

        public static SecurityUrlConfig with(String url, boolean authentication, RouterConfig routerConfig) {
            return new SecurityUrlConfig(url, authentication, routerConfig);
        }

        public boolean matches(String url) {
            return this.pathParser.matches(url);
        }
    }
}
