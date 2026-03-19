package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_stock_reservation")
public class StockReservation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 锁定记录 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long reservationId;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 仓库 id
     */
    @TableField("warehouse_id")
    private Long warehouseId;

    /**
     * sku id
     */
    @TableField("sku_id")
    private Long skuId;

    /**
     * 锁定数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 状态：1-LOCKED 2-RELEASED 3-DEDUCTED 4-FAILED
     */
    @TableField("status")
    private Integer status;

    /**
     * 锁定过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expire_at")
    private LocalDateTime expireAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
