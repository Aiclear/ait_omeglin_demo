package com.github.blackz.db;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * 初始化 Omeglin 应用的数据库脚本
 *
 * @author xinzheyu
 * @since 2025/12/21 13:17
 */
@Slf4j
public class OmeglinSchema {

    public static void init() {
        // 判断omeglin.sqlite是否存在，不存在再进行初始化
        if (Files.exists(Path.of("src/main/resources/db/omeglin.sqlite"))) {
            log.info("omeglin.sqlite has been initialized");
            return;
        }

        // 开始初始化
        AppHibernate.doWork(connection -> {
            try (var statement = connection.createStatement()) {
                statement.execute("""
                    create table users (
                        id         bigint not null primary key,
                        code       varchar(255),
                        email      varchar(255),
                        birth_date date,
                        password   varchar(255),
                        username   varchar(255),
                        create_at  timestamp,
                        update_at  timestamp
                    )
                    """);

                statement.execute("""
                    create table if not exists user_friends
                    (
                        id                  bigint not null primary key,
                        user_code_x         varchar(255),
                        user_code_y         varchar(255),
                        proactive_user_code varchar(255),
                        state               char(2),
                        create_at           timestamp,
                        update_at           timestamp,
                        unique (user_code_x, user_code_y)
                    );
                    """);
            }
        });
    }
}
