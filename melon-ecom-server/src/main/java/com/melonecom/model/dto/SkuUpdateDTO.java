package com.melonecom.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuUpdateDTO {
    private Long skuId;
    private String name;
    private String specJson;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stock;
    private String imageUrl;
    private Integer aiTryonEnabled;
    private String tryonCategory;
    private String tryonImageUrl;
    private String tryonMaskUrl;
    private Integer tryonSort;
    private BigDecimal weight;
    private Integer status;
}
