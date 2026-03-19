package com.melonecom.model.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_product_comment")
public class ProductComment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // 评论ID
    @TableId(value = "id", type = IdType.AUTO)
    private Long commentId;

    // 商品ID（SPU）
    @TableField("product_id")
    private Long productId;

    // SKU ID（可选）
    @TableField("sku_id")
    private Long skuId;

    // 用户ID（评论者 or 管理员回复）
    @TableField("user_id")
    private Long userId;

    // 父评论ID（0=主评论）
    @TableField("parent_id")
    private Long parentId;

    // 根评论ID（0=主评论）
    @TableField("root_id")
    private Long rootId;

    // 评分（1-5，仅主评论有意义）
    @TableField("rating")
    private Integer rating;

    // 评论内容
    @TableField("content")
    private String content;

    // 点赞数
    @TableField("like_count")
    private Long likeCount;

    // 状态：1显示 0隐藏
    @TableField("status")
    private Integer status;

    // 创建时间
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新时间
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}