package com.melonecom.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockReservationSearchDTO {

    /** 分页 */
    private Integer page = 1;
    private Integer pageSize = 10;

    /** 订单号 */
    private String orderNo;

    /** SKU */
    private Long skuId;

    /** 仓库 */
    private Long warehouseId;

    /** 锁定状态：0-锁定中 1-已释放 2-已扣减(完成发货) */
    private Integer status;

    /** 过期时间范围（排查超时未释放） */
    private LocalDateTime expireAtStart;
    private LocalDateTime expireAtEnd;
}
