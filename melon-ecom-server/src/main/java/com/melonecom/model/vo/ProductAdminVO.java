package com.melonecom.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductAdminVO {
    private Long productId;
    private String name;
    private String subTitle;
    private Long brandId;
    private Long categoryId;
    private String mainImageUrl;
    private Integer status;
    private BigDecimal minPrice;
    private BigDecimal ratingAvg;
    private Long commentCount;
    private Long salesCount;
}
