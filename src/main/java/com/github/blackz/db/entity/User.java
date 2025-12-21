package com.github.blackz.db.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 对应数据库 users 表的实体对象
 *
 * @author xinzheyu
 * @since 2025/12/20 9:02
 */
@Data
@Entity
@Table(name = "users")
public class User {

    /**
     * 用户主键
     */
    @Id
    private Long id;

    /**
     * 主键无关用户 code
     */
    private String code;

    private String username;

    private String password;

    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birth_date")
    private LocalDate dateOfBirth;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "update_at")
    private LocalDateTime updatedAt;
}
