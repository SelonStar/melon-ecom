package com.melonecom.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class CartBatchAddVO {
    private Integer successCount;
    private Integer failCount;
    private List<CartBatchAddItemVO> items;
}
