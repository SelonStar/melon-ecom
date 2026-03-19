package com.melonecom.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TryOnCandidateSkuVO {
    private Long skuId;
    private Long productId;
    private String productName;
    private String productSubTitle;
    private String skuName;
    private String imageUrl;
    private String tryOnImageUrl;
    private String tryOnCategory;
    private BigDecimal price;
    private Integer stock;
    private Integer tryOnSort;
}
