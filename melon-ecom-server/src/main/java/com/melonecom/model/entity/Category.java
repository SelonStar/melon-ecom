package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_category")
public class Category {

    @TableId(value = "id", type = IdType.AUTO)
    private Long categoryId;

    @TableField("parent_id")
    private Long parentId;

    private String name;

    private Integer sort;

    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
