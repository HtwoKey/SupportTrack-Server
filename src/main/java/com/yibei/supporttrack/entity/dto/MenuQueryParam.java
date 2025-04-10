package com.yibei.supporttrack.entity.dto;

import lombok.Data;

@Data
public class MenuQueryParam {
    private String title;
    private String name;
    private Integer isHide;
    private String permissionName;
    private Integer pageNum;
    private Integer pageSize;
}
