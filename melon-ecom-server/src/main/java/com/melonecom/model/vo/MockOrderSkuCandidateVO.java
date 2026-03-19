package com.melonecom.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MockOrderSkuCandidateVO {
    private Long skuId;
    private Long productId;
    private Long warehouseId;
    private BigDecimal price;
    private Integer available;
}
