package com.melonecom.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockTxnSearchDTO {

    /** 分页 */
    private Integer page = 1;
    private Integer pageSize = 10;

    /** 业务类型：ADJUST/INBOUND/OUTBOUND/RESERVE/RELEASE/DEDUCT */
    private Integer bizType;

    /** 业务单号：入库单号/订单号/盘点单号 */
    private String bizNo;

    /** SKU */
    private Long skuId;

    /** 仓库 */
    private Long warehouseId;

    /** 时间范围 */
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
}
