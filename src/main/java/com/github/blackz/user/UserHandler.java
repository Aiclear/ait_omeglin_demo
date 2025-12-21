package com.github.blackz.user;

import com.github.blackz.db.entity.User;
import com.github.blackz.auth.dao.UserRepository;
import com.github.blackz.security.SecurityContext;
import com.github.blackz.security.UserInformation;
import com.github.blackz.friend.FriendsRepository;
import io.javalin.http.Context;
import java.util.List;
import java.util.Map;

/**
 * 用户相关信息处理
 *
 * @author xinzheyu
 * @since 2025/12/21 12:21
 */
public class UserHandler {

    /**
     * 获取当前登录用户的信息
     */
    public static void queryUserInfo(Context context) {
        // 根据 token获取当前的信息
        UserInformation userInformation = SecurityContext.getContext();

        // 查询用户信息
        User user = UserRepository.findUserByEmail(userInformation.getEmail());

        // 删除密码的回显
        user.setPassword(null);

        context.json(user);
    }
    
    /**
     * 获取当前用户的好友列表
     */
    public static void getFriendsList(Context ctx) {
        UserInformation userInfo = SecurityContext.getContext();
        if (userInfo != null) {
            List<User> friends = FriendsRepository.getUserFriends(userInfo.getUserCode());
            
            // 移除密码信息，只返回安全的用户信息
            friends.forEach(user -> user.setPassword(null));
            
            ctx.json(friends);
        } else {
            ctx.status(401).json(Map.of("error", "用户未登录"));
        }
    }

}
