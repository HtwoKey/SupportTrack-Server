package com.yibei.supporttrack.entity.dto;

import lombok.Data;

@Data
public class MenuQueryParam {
    private String title;
    private Integer type;
    private String startTime;
    private String endTime;
}
