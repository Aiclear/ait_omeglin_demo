package com.github.blackz.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "user_code_x")
    private String userCodeX;
    @Column(name = "user_code_y")
    private String userCodeY;

    /**
     * 谁是主动发起的好友申请
     */
    @Column(name = "proactive_user_code")
    private String proactiveUserCode;

    /**
     *
     * 0 申请中 1 正常 2 拉黑 当前好友状态
     */
    @Column(name = "x_to_y_state")
    private String xToYState;
    /**
     * 0 申请中 1 正常 2 拉黑 当前好友状态
     */
    @Column(name = "y_to_x_state")
    private String yToXState;

    @Column(name = "create_at")
    private LocalDateTime createTime;
    @Column(name = "update_at")
    private LocalDateTime updateTime;
}
