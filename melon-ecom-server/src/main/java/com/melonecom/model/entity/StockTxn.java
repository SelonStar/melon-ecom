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
@TableName("tb_stock_txn")
public class StockTxn implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 库存流水 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long txnId;

    /**
     * 业务类型：1-LOCK 2-RELEASE 3-DEDUCT 4-ADJUST 5-INBOUND 6-OUTBOUND
     */
    @TableField("biz_type")
    private Integer bizType;

    /**
     * 业务单号(订单号/调整单号等)
     */
    @TableField("biz_no")
    private String bizNo;

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
     * available 变化量
     */
    @TableField("delta_available")
    private Integer deltaAvailable;

    /**
     * locked 变化量
     */
    @TableField("delta_locked")
    private Integer deltaLocked;

    /**
     * 变更前 available(可选)
     */
    @TableField("before_available")
    private Integer beforeAvailable;

    /**
     * 变更前 locked(可选)
     */
    @TableField("before_locked")
    private Integer beforeLocked;

    /**
     * 变更后 available(可选)
     */
    @TableField("after_available")
    private Integer afterAvailable;

    /**
     * 变更后 locked(可选)
     */
    @TableField("after_locked")
    private Integer afterLocked;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}
