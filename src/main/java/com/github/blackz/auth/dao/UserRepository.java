package com.github.blackz.auth.dao;

import com.github.blackz.db.AppHibernate;
import com.github.blackz.db.entity.User;

/**
 * User 操作数据库的类
 *
 * @author xinzheyu
 * @since 2025/12/20 9:33
 */
public class UserRepository {

    public static void saveUser(User user) {
        AppHibernate.inTransaction(session -> session.insert(user));
    }

    public static User findUserByEmail(String email) {
        return AppHibernate.fromTransaction(session ->
            session.createQuery(
                    "select u from User u where u.email = :email", User.class)
                .setParameter("email", email)
                .getSingleResultOrNull()
        );
    }
}
