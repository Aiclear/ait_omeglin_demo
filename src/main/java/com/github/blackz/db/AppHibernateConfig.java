package com.github.blackz.db;

import com.github.blackz.db.entity.User;
import java.util.Properties;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.schema.Action;

/**
 * hibernate orm config
 *
 * @author xinzheyu
 * @since 2025/12/19 22:07
 */
public class AppHibernateConfig {

    static Configuration configuration() {
        var configuration = new Configuration();
        var settings = new Properties();
        settings.put(AvailableSettings.JAKARTA_JDBC_DRIVER, "org.sqlite.JDBC");
        settings.put(AvailableSettings.JAKARTA_JDBC_URL, "jdbc:sqlite:src/main/resources/db/omeglin.sqlite");
        settings.put(AvailableSettings.JAKARTA_JDBC_USER, "sa");
        settings.put(AvailableSettings.JAKARTA_JDBC_PASSWORD, "");
        settings.put(AvailableSettings.HIGHLIGHT_SQL, true);
        settings.put(AvailableSettings.HBM2DDL_AUTO, Action.ACTION_CREATE);

        configuration.setProperties(settings);
        configuration.addAnnotatedClass(User.class);

        return configuration;
    }
}
