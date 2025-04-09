package com.yibei.supporttrack.entity.dto;

import lombok.Data;

@Data
public class UserQueryParam {
    private String keyword;
    private Integer status;
    private String startTime;
    private String endTime;
    private Integer pageSize;
    private Integer pageNum;
}
