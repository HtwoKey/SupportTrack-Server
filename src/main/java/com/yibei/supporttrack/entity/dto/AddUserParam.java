package com.yibei.supporttrack.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AddUserParam {

    private Integer userId;
    @NotEmpty
    private String username;
    @Email
    private String email;
    private String fullName;
    private String phone;
    private String avatar;

    private Integer[] roles;
}
