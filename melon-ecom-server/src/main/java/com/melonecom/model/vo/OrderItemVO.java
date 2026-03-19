package com.melonecom.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Long skuId;
    private String skuName;
    private Long warehouseId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
