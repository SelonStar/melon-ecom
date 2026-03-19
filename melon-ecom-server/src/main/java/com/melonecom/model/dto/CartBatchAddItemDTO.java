package com.melonecom.model.dto;

import lombok.Data;

@Data
public class CartBatchAddItemDTO {
    private Long skuId;
    private Integer quantity;
}
