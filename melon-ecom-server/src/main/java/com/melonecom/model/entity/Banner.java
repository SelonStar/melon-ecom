package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@TableName("tb_banner")
public class Banner {

    @TableId(value = "id", type = IdType.AUTO)
    private Long bannerId;

    private String title;

    @TableField("banner_url")
    private String bannerUrl;

    @TableField("link_url")
    private String linkUrl;

    @TableField("jump_type")
    private String jumpType;

    @TableField("jump_target_id")
    private Long jumpTargetId;

    private Integer sort;

    /**
     * 1启用 0禁用
     */
    private Integer status;

    @TableField(value = "start_time")
    private LocalDateTime startTime;

    @TableField(value = "end_time")
    private LocalDateTime endTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}