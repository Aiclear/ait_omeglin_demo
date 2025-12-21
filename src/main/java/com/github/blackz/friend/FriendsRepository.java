package com.github.blackz.friend;

import com.github.blackz.db.AppHibernate;
import com.github.blackz.db.entity.User;
import lombok.Getter;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import java.util.List;

public class FriendsRepository {

    /**
     * 添加好友关系
     * @param userCode1 用户1的code
     * @param userCode2 用户2的code
     */
    public static void addFriend(String userCode1, String userCode2) {
        AppHibernate.fromTransaction(session -> {
            // 检查好友关系是否已存在
            if (!isFriendExists(session, userCode1, userCode2)) {
                // 创建两个方向的好友关系记录
                FriendsRelation relation1 = new FriendsRelation();
                relation1.setUserCode(userCode1);
                relation1.setFriendCode(userCode2);
                relation1.setStatus("accepted");
                
                FriendsRelation relation2 = new FriendsRelation();
                relation2.setUserCode(userCode2);
                relation2.setFriendCode(userCode1);
                relation2.setStatus("accepted");
                
                session.insert(relation1);
                session.insert(relation2);
            }
            return null; // 返回null以满足函数接口要求
        });
    }

    /**
     * 检查好友关系是否已存在
     */
    private static boolean isFriendExists(StatelessSession session, String userCode1, String userCode2) {
        Query<Long> query = session.createQuery(
                "select count(r) from FriendsRelation r where (r.userCode = :user1 and r.friendCode = :user2) or " +
                        "(r.userCode = :user2 and r.friendCode = :user1)", Long.class);
        query.setParameter("user1", userCode1);
        query.setParameter("user2", userCode2);
        return query.uniqueResult() > 0;
    }

    /**
     * 获取用户的好友列表
     * @param userCode 用户code
     * @return 好友列表
     */
    public static List<User> getUserFriends(String userCode) {
        return AppHibernate.fromTransaction(session -> {
            Query<User> query = session.createQuery(
                    "select u from User u join FriendsRelation r on u.code = r.friendCode where r.userCode = :userCode and r.status = 'accepted'",
                    User.class);
            query.setParameter("userCode", userCode);
            return query.list();
        });
    }

    /**
     * 创建FriendsRelation实体类，映射好友关系表
     */
    @Getter
    public static class FriendsRelation {
        // Getters and Setters
        private Long id;
        private String userCode;
        private String friendCode;
        private String status;

        public void setId(Long id) {
            this.id = id;
        }

        public void setUserCode(String userCode) {
            this.userCode = userCode;
        }

        public void setFriendCode(String friendCode) {
            this.friendCode = friendCode;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}