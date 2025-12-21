package com.github.blackz.security;

import com.github.blackz.db.entity.User;
import java.util.List;
import lombok.Data;

/**
 * Login User Information
 *
 * @author xinzheyu
 * @since 2025/12/20 10:50
 */
@Data
public class UserInformation {

    private String userCode;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> menus;

    public static UserInformation initWith(User user) {
        UserInformation userInformation = new UserInformation();

        userInformation.setUserCode(user.getCode());
        userInformation.setUsername(user.getUsername());
        userInformation.setEmail(user.getEmail());
        userInformation.setRoles(List.of());
        userInformation.setMenus(List.of());

        return userInformation;
    }
}
