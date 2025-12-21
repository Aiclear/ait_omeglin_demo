package com.github.blackz.jwt;

import com.alibaba.fastjson2.JSON;
import com.github.blackz.security.UserInformation;
import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

/**
 * JWT 工具类
 *
 * @author xinzheyu
 * @since 2025/12/20 10:35
 */
public final class JwtUtils {

    private static final String SECRET_KEY = "019b39a8-de97-74e2-a40b-a6304d8a2edb";


    private static String getToken(Context context) {
        // from header
        String authorization = context.header("Authorization");
        if (StringUtils.isNotEmpty(authorization)) {
            return authorization.substring("Bearer ".length());
        }

        return context.cookie("Authorization");
    }

    public static String generateToken(UserInformation userInformation) {
        return Jwts.builder()
            .subject(userInformation.getUsername())
            .issuer("blackz")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
            .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
            .claim("json", JSON.toJSONString(userInformation))
            .compact();
    }

    public static UserInformation validateToken(Context context) {
        String token = getToken(context);
        if (null == token) {
            return null;
        }

        Jws<Claims> claimsJws = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token);

        return JSON.parseObject(claimsJws.getPayload().get("json").toString(), UserInformation.class);
    }
}
