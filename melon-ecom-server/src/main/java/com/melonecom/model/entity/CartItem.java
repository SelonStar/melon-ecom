package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_cart_item")
public class CartItem {

  @TableId(value = "id", type = IdType.AUTO)
    private Long cartItemId;

    private Long userId;
    private Long skuId;
    private Integer quantity;
    private Integer checked;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
