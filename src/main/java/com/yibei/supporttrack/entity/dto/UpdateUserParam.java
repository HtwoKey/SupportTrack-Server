package com.yibei.supporttrack.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateUserParam {

    private Integer userId;
    @NotEmpty
    private String username;
    @Email
    private String email;
    private String fullName;

    private String phone;
    private String avatar;

    /**
     * 状态
     */
    private Boolean isActive;

    private Integer[] roles;
}
