package com.github.blackz.db;

import java.util.Objects;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AppHibernateSessionFactory {

    private static final Logger logger = LoggerFactory.getLogger(AppHibernateSessionFactory.class);

    private static SessionFactory sessionFactory;

    static SessionFactory getSessionFactory() {
        if (Objects.isNull(sessionFactory)) {
            try {
                var configuration = AppHibernateConfig.configuration();
                var serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Throwable ex) {
                logger.error("Failed to create session factory", ex);
            }
        }
        return sessionFactory;
    }

}