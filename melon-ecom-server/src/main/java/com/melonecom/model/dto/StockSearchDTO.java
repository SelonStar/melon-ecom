package com.melonecom.model.dto;

import lombok.Data;

@Data
public class StockSearchDTO {
    private Integer page = 1;
    private Integer pageSize = 10;

    private Long skuId;
    private Long warehouseId;
}
