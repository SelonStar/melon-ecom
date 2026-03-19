package com.melonecom.model.dto;

import lombok.Data;

@Data
public class ProductSearchDTO {
    private Integer page = 1;
    private Integer pageSize = 10;
    private Integer status;
    private Long categoryId;
    private Long brandId;
    private String keyword;
}
