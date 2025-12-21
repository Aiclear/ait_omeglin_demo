package com.github.blackz;

import com.github.blackz.db.entity.User;
import com.github.blackz.db.AppHibernate;
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
}
