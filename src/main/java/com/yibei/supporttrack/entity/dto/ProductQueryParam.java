package com.yibei.supporttrack.entity.dto;

import lombok.Data;

@Data
public class ProductQueryParam {

    private String productName;
    private Integer pageNum;
    private Integer pageSize;
}
