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
@TableName("tb_warehouse_allocation")
public class WarehouseAllocation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分仓记录 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long allocationId;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * sku id
     */
    @TableField("sku_id")
    private Long skuId;

    /**
     * 仓库 id
     */
    @TableField("warehouse_id")
    private Long warehouseId;

    /**
     * 分配数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}
