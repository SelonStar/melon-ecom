package com.melonecom.model.dto;

import lombok.Data;

@Data
public class StockAdjustDTO {
    private Long warehouseId;
    private Long skuId;

    /**
     * 调整量：正数=入库/增加；负数=出库/扣减
     */
    private Integer delta;

    /**
     * 业务类型：ADJUST/INBOUND/OUTBOUND（你也可以用 int 常量）
     */
    private Integer bizType;

    /**
     * 业务单号：如入库单号/盘点单号/人工调整单号，可空
     */
    private String bizNo;

    /**
     * 备注，可空
     */
    private String remark;
}
