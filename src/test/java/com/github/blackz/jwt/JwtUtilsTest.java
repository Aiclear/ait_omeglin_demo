package com.github.blackz.jwt;

import static org.junit.jupiter.api.Assertions.*;

import com.alibaba.fastjson2.JSON;
import com.github.blackz.security.UserInformation;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 测试工具函数
 *
 * @author xinzheyu
 * @since 2025/12/20 22:42
 */

class JwtUtilsTest {

    private static final String SECRET_KEY = "019b39a8-de97-74e2-a40b-a6304d8a2edb";

    @Test
    void generateToken() {
        UserInformation userInformation = new UserInformation();
        userInformation.setUserCode("userCode");
        userInformation.setUsername("username");
        userInformation.setEmail("email");
        userInformation.setRoles(List.of());
        userInformation.setMenus(List.of());

        String token = JwtUtils.generateToken(userInformation);
        assertNotNull(token);
    }

    @Test
    void validateToken() {
        UserInformation userInformation = new UserInformation();
        userInformation.setUserCode("userCode");
        userInformation.setUsername("username");
        userInformation.setEmail("email");
        userInformation.setRoles(List.of());
        userInformation.setMenus(List.of());

        String token = JwtUtils.generateToken(userInformation);

        Jws<Claims> claimsJws = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token);

        UserInformation userInformation1 =
            JSON.parseObject(claimsJws.getPayload().get("json").toString(), UserInformation.class);

        assertEquals(userInformation, userInformation1);
    }
}