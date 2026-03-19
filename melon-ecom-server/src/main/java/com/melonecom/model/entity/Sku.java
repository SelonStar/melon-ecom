package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_sku")
public class Sku implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long skuId;

    @TableField("product_id")
    private Long productId;

    @TableField("sku_code")
    private String skuCode;

    @TableField("name")
    private String name;

    @TableField("spec_json")
    private String specJson;

    @TableField("price")
    private BigDecimal price;

    @TableField("cost_price")
    private BigDecimal costPrice;

    @TableField("stock")
    private Integer stock;

    @TableField("sales_count")
    private Long salesCount;

    @TableField("image_url")
    private String imageUrl;

    @TableField("ai_tryon_enabled")
    private Integer aiTryonEnabled;

    @TableField("tryon_category")
    private String tryonCategory;

    @TableField("tryon_image_url")
    private String tryonImageUrl;

    @TableField("tryon_mask_url")
    private String tryonMaskUrl;

    @TableField("tryon_sort")
    private Integer tryonSort;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
