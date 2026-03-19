package com.melonecom.model.dto;

import lombok.Data;

@Data
public class WarehouseAllocationSearchDTO {

    /** 分页 */
    private Integer page = 1;
    private Integer pageSize = 10;

    /** 订单号 */
    private String orderNo;

    /** SKU */
    private Long skuId;

    /** 仓库 */
    private Long warehouseId;
}
