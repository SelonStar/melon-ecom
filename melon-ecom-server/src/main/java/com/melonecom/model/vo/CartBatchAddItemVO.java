package com.melonecom.model.vo;

import lombok.Data;

@Data
public class CartBatchAddItemVO {
    private Long skuId;
    private Integer quantity;
    private Boolean success;
    private String message;
}
