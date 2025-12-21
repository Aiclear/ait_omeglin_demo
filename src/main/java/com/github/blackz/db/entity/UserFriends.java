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
     * 谁是主动发起的好友申请
     */
    private String proactiveUserCode;

    /**
     * 1 正常 2 拉黑 当前好友状态
     */
    private String xToYState;
    /**
     * 1 正常 2 拉黑 当前好友状态
     */
    private String yToXState;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
