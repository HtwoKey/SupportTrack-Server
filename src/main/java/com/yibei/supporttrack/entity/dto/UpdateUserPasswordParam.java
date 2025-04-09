package com.yibei.supporttrack.entity.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateUserPasswordParam {
    @NotEmpty
    private Integer id;
    @NotEmpty
    private String username;
    @NotEmpty
    private String newPassword;
}
