package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_tryon_task")
public class TryOnTask implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long taskId;

    @TableField("user_id")
    private Long userId;

    @TableField("photo_url")
    private String photoUrl;

    @TableField("photo_type")
    private String photoType;

    @TableField("top_sku_id")
    private Long topSkuId;

    @TableField("bottom_sku_id")
    private Long bottomSkuId;

    @TableField("dress_sku_id")
    private Long dressSkuId;

    @TableField("shoes_sku_id")
    private Long shoesSkuId;

    @TableField("status")
    private String status;

    @TableField("provider_code")
    private String providerCode;

    @TableField("provider_task_id")
    private String providerTaskId;

    @TableField("result_image_url")
    private String resultImageUrl;

    @TableField("error_message")
    private String errorMessage;

    @TableField("request_snapshot")
    private String requestSnapshot;

    @TableField("provider_response")
    private String providerResponse;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
