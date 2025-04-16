package com.yibei.supporttrack.entity.dto;

import lombok.Data;

@Data
public class CustomerQueryParam {

    private String customerName;
    private Integer pageNum;
    private Integer pageSize;
}
