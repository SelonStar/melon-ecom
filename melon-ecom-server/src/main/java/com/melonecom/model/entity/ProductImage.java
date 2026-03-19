package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_product_image")
public class ProductImage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value="id", type=IdType.AUTO)
    private Long imageId;

    @TableField("product_id")
    private Long productId;

    @TableField("url")
    private String url;

    @TableField("sort")
    private Integer sort;

    @TableField("create_time")
    private LocalDateTime createTime;
}
