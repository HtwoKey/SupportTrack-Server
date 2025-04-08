package com.yibei.supporttrack.entity.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AddUserParam {

    private Integer userId;
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;

    @Email
    private String email;
    private String fullName;
    /**
     * 状态
     */
    private Boolean isActive;

    private Integer[] roles;
}
