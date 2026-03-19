package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_order")
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Long orderId;

    private String orderNo;
    private Long userId;
    private Integer status; // 0未支付 1已支付 2已取消
    private BigDecimal totalAmount;
    private LocalDateTime payTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
