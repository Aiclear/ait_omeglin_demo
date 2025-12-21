package com.github.blackz.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户朋友列表
 *
 * @author xinzheyu
 * @since 2025/12/21 14:17
 */
@Data
@Entity
@Table(name = "user_friends")
public class UserFriends {

    @Id
    private Long id;

    private String userCodeX;
    private String userCodeY;

    /**
     * 谁是主动发起的
     */
    private String proactiveUserCode;

    /**
     * 当前状态
     */
    private String state;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
