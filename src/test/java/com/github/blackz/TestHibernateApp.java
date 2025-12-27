package com.github.blackz;

import com.github.blackz.db.entity.User;
import com.github.blackz.db.AppHibernate;
import com.github.blackz.db.entity.UserFriends;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Hibernate Test
 *
 * @author xinzheyu
 * @since 2025/12/20 9:09
 */
public class TestHibernateApp {

    @Test
    public void testEntityInsert() {
        AppHibernate.inTransaction(session -> {
            User user = new User();
            user.setId(0L);
            user.setCode("admin");
            user.setUsername("admin");
            user.setPassword("admin");
            user.setEmail("admin@qq.com");
            user.setDateOfBirth(LocalDate.of(2025, 12, 20));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            session.insert(user);
        });
    }

    @Test
    public void testEntityQuery() {
        User user = AppHibernate.fromTransaction(session ->
            session.createQuery("select u from User u where u.id = :id", User.class)
                .setParameter("id", 0)
                .getSingleResult()
        );

        Assertions.assertNotNull(user);
    }

    @Test
    public void testUserFriendsInsert() {
        AppHibernate.inTransaction(session -> {
            UserFriends userFriends = new UserFriends();
            userFriends.setUserCodeX("00000");
            userFriends.setUserCodeY("00001");
            userFriends.setProactiveUserCode("00000");
            userFriends.setXToYState("0");
            userFriends.setYToXState("0");
            userFriends.setCreateTime(LocalDateTime.now());

            session.insert(userFriends);
        });

        Assertions.assertTrue(true);
    }

    @Test
    public void testUserFriendsSelect() {
        UserFriends userFriends =
            AppHibernate.fromTransaction(session -> session.get(UserFriends.class, 51));

        System.out.println(userFriends);
        Assertions.assertNotNull(userFriends);
    }
}
