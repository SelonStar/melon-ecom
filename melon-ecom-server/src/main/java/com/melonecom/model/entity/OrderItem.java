package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_order_item")
public class OrderItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long orderItemId;

    private Long orderId;
    private Long skuId;
    private Long warehouseId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
