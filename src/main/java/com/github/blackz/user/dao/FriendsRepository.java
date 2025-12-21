package com.github.blackz.user.dao;

import com.github.blackz.db.AppHibernate;
import com.github.blackz.db.entity.User;
import com.github.blackz.db.entity.UserFriends;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 好友关系操作数据库的类
 *
 * @author xinzheyu
 */
public class FriendsRepository {

    /**
     * 创建好友关系
     */
    public static void createFriendship(String userCodeX, String userCodeY, String proactiveUserCode) {
        AppHibernate.inTransaction(session -> {
            UserFriends friendship = new UserFriends();
            friendship.setId(System.currentTimeMillis());
            friendship.setUserCodeX(userCodeX);
            friendship.setUserCodeY(userCodeY);
            friendship.setProactiveUserCode(proactiveUserCode);
            friendship.setXToYState("1"); // 1 正常
            friendship.setYToXState("1"); // 1 正常
            friendship.setCreateTime(LocalDateTime.now());
            friendship.setUpdateTime(LocalDateTime.now());
            session.insert(friendship);
        });
    }

    /**
     * 检查是否已经是好友
     */
    public static boolean isFriends(String userCodeA, String userCodeB) {
        return AppHibernate.fromTransaction(session -> {
            Long count = session.createQuery(
                    "select count(*) from UserFriends f where " +
                    "(f.userCodeX = :codeA and f.userCodeY = :codeB) or " +
                    "(f.userCodeX = :codeB and f.userCodeY = :codeA) and " +
                    "f.xToYState = '1' and f.yToXState = '1'", Long.class)
                .setParameter("codeA", userCodeA)
                .setParameter("codeB", userCodeB)
                .getSingleResult();
            return count > 0;
        });
    }

    /**
     * 获取用户的好友列表
     */
    public static List<User> getUserFriends(String userCode) {
        return AppHibernate.fromTransaction(session -> {
            return session.createQuery(
                    "select u from User u where u.code in " +
                    "(select case when f.userCodeX = :userCode then f.userCodeY else f.userCodeX end " +
                    "from UserFriends f where (f.userCodeX = :userCode or f.userCodeY = :userCode) " +
                    "and f.xToYState = '1' and f.yToXState = '1')", User.class)
                .setParameter("userCode", userCode)
                .getResultList();
        });
    }
}
