package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_product")
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // 表主键：tb_product.id
    @TableId(value = "id", type = IdType.AUTO)
    private Long productId;

    @TableField("name")
    private String name;

    @TableField("sub_title")
    private String subTitle;

    @TableField("brand_id")
    private Long brandId;

    @TableField("category_id")
    private Long categoryId;

    @TableField("main_image_url")
    private String mainImageUrl;

    @TableField("detail_html")
    private String detailHtml;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    // // 评论数（仅统计可见主评论）
    // @TableField("comment_count")
    // private Long commentCount;

    // // 平均评分（1.00 ~ 5.00）
    // @TableField("rating_avg")
    // private BigDecimal ratingAvg;
}
