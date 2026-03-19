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
@TableName("tb_stock")
public class Stock implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 库存记录 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long stockId;

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
     * 可售库存
     */
    @TableField("available")
    private Integer available;

    /**
     * 锁定库存
     */
    @TableField("locked")
    private Integer locked;

    /**
     * 乐观锁版本号
     */
    @TableField("version")
    private Integer version;

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
