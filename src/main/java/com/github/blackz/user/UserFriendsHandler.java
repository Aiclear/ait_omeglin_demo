package com.github.blackz.user;

import com.github.blackz.ResultDto;
import com.github.blackz.db.AppHibernate;
import com.github.blackz.db.entity.UserFriends;
import io.javalin.http.Context;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户朋友的 API
 *
 * @author xinzheyu
 * @since 2025/12/27 15:41
 */
public class UserFriendsHandler {

    public static void makeFriend(Context context) {
        // 加好友的参数
        MakeFriendDto makeFriendDto = context.bodyAsClass(MakeFriendDto.class);

        UserFriends userFriends = new UserFriends();
        userFriends.setUserCodeX(makeFriendDto.getUserCode());
        userFriends.setUserCodeY(makeFriendDto.getFriendCode());
        userFriends.setProactiveUserCode(makeFriendDto.getUserCode());
        userFriends.setXToYState("1");
        userFriends.setYToXState("1");
        userFriends.setCreateTime(LocalDateTime.now());

        AppHibernate.inTransaction(statelessSession -> statelessSession.insert(userFriends));

        context.json(ResultDto.ok());
    }

    @Data
    public static class MakeFriendDto {

        private String userCode;
        private String friendCode;
    }
}
