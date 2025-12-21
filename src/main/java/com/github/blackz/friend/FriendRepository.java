package com.github.blackz.friend;

import com.github.blackz.db.AppHibernate;
import com.github.blackz.db.entity.UserFriends;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 好友关系数据库操作类
 *
 * @author xinzheyu
 * @since 2025/12/21
 */
public class FriendRepository {

    /**
     * 检查两个用户是否已经是好友
     */
    public static boolean areFriends(String userCode1, String userCode2) {
        return AppHibernate.fromTransaction(session -> {
            String query = "SELECT COUNT(f) FROM UserFriends f " +
                          "WHERE ((f.userCodeX = :code1 AND f.userCodeY = :code2) " +
                          "OR (f.userCodeX = :code2 AND f.userCodeY = :code1)) " +
                          "AND f.xToYState = '1' AND f.yToXState = '1'";
            
            Long count = session.createQuery(query, Long.class)
                    .setParameter("code1", userCode1)
                    .setParameter("code2", userCode2)
                    .getSingleResult();
            return count > 0;
        });
    }

    /**
     * 创建好友申请
     */
    public static void createFriendRequest(String fromUserCode, String toUserCode) {
        AppHibernate.inTransaction(session -> {
            // 检查是否已存在好友关系或申请
            String checkQuery = "SELECT COUNT(f) FROM UserFriends f " +
                               "WHERE ((f.userCodeX = :code1 AND f.userCodeY = :code2) " +
                               "OR (f.userCodeX = :code2 AND f.userCodeY = :code1))";
            
            Long existing = session.createQuery(checkQuery, Long.class)
                    .setParameter("code1", fromUserCode)
                    .setParameter("code2", toUserCode)
                    .getSingleResult();
            
            if (existing == 0) {
                // 创建新的好友申请
                UserFriends friends = new UserFriends();
                friends.setId(System.currentTimeMillis());
                friends.setUserCodeX(fromUserCode);
                friends.setUserCodeY(toUserCode);
                friends.setProactiveUserCode(fromUserCode);
                friends.setXToYState("0"); // 0 表示待确认
                friends.setYToXState("1"); // 对方默认是正常状态
                friends.setCreateTime(LocalDateTime.now());
                friends.setUpdateTime(LocalDateTime.now());
                
                session.insert(friends);
            }
        });
    }

    /**
     * 接受好友申请
     */
    public static void acceptFriendRequest(String userCode1, String userCode2) {
        AppHibernate.inTransaction(session -> {
            String query = "SELECT f FROM UserFriends f " +
                          "WHERE ((f.userCodeX = :code1 AND f.userCodeY = :code2) " +
                          "OR (f.userCodeX = :code2 AND f.userCodeY = :code1)) " +
                          "AND f.xToYState = '0'";
            
            UserFriends friends = session.createQuery(query, UserFriends.class)
                    .setParameter("code1", userCode1)
                    .setParameter("code2", userCode2)
                    .getSingleResultOrNull();
            
            if (friends != null) {
                friends.setXToYState("1"); // 设置为正常好友状态
                friends.setYToXState("1");
                friends.setUpdateTime(LocalDateTime.now());
                session.update(friends);
            }
        });
    }

    /**
     * 获取用户的好友列表
     */
    public static List<UserFriends> getUserFriends(String userCode) {
        return AppHibernate.fromTransaction(session -> {
            String query = "SELECT f FROM UserFriends f " +
                          "WHERE (f.userCodeX = :userCode OR f.userCodeY = :userCode) " +
                          "AND f.xToYState = '1' AND f.yToXState = '1'";
            
            return session.createQuery(query, UserFriends.class)
                    .setParameter("userCode", userCode)
                    .getResultList();
        });
    }

    /**
     * 获取待确认的好友申请
     */
    public static List<UserFriends> getPendingFriendRequests(String userCode) {
        return AppHibernate.fromTransaction(session -> {
            String query = "SELECT f FROM UserFriends f " +
                          "WHERE f.userCodeY = :userCode AND f.xToYState = '0'";
            
            return session.createQuery(query, UserFriends.class)
                    .setParameter("userCode", userCode)
                    .getResultList();
        });
    }
}