package com.melonecom.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long skuId;
    private String skuName;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private Integer checked;
    private BigDecimal lineAmount;
}
