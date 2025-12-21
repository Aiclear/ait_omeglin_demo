package com.github.blackz.friend;

import com.github.blackz.db.entity.UserFriends;
import com.github.blackz.jwt.JwtUtils;
import com.github.blackz.security.UserInformation;
import com.github.blackz.auth.dao.UserRepository;
import com.github.blackz.db.entity.User;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class FriendHandler {
    private static final Logger logger = LoggerFactory.getLogger(FriendHandler.class);

    public static Handler getFriendsList = ctx -> {
        try {
            String userCode = getUserCodeFromContext(ctx);
            logger.info("Getting friends list for userCode: {}", userCode);
            if (userCode == null) {
                ctx.status(401).json(Map.of("code", "401", "message", "User not authenticated", "data", null));
                return;
            }

            List<UserFriends> friends = FriendRepository.getUserFriends(userCode);
            logger.info("Found {} friends for userCode: {}", friends.size(), userCode);
            ctx.json(Map.of("code", "200", "message", "", "data", friends));
        } catch (Exception e) {
            logger.error("Error getting friends list", e);
            System.err.println("Detailed error in getFriendsList:");
            e.printStackTrace();
            ctx.status(500).json(Map.of("code", "999", "message", "服务器异常请联系管理员: " + e.getMessage(), "data", null));
        }
    };

    public static Handler getPendingRequests = ctx -> {
        try {
            String userCode = getUserCodeFromContext(ctx);
            if (userCode == null) {
                ctx.status(401).json(Map.of("code", "401", "message", "User not authenticated", "data", null));
                return;
            }

            List<UserFriends> pendingRequests = FriendRepository.getPendingFriendRequests(userCode);
            ctx.json(Map.of("code", "200", "message", "", "data", pendingRequests));
        } catch (Exception e) {
            logger.error("Error getting pending requests", e);
            e.printStackTrace();
            ctx.status(500).json(Map.of("code", "999", "message", "服务器异常请联系管理员", "data", null));
        }
    };

    private static String getUserCodeFromContext(Context ctx) {
        // 从JWT token或session中获取用户code
        try {
            UserInformation userInfo = JwtUtils.validateToken(ctx);
            if (userInfo != null && userInfo.getUsername() != null) {
                // 根据用户名获取用户code
                User user = UserRepository.findUserByUsername(userInfo.getUsername());
                return user != null ? user.getCode() : null;
            }
        } catch (Exception e) {
            logger.error("Error parsing JWT token", e);
        }
        
        // 尝试从请求体中获取userCode作为备选
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            if (body != null && body.containsKey("userCode")) {
                return (String) body.get("userCode");
            }
        } catch (Exception e) {
            logger.debug("No userCode in request body");
        }
        
        return null;
    }
}