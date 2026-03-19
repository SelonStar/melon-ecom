package com.melonecom.model.dto;

import lombok.Data;

@Data
public class AdminCommentSearchDTO {
    private Integer page = 1;
    private Integer pageSize = 10;

    private Long productId;
    private Long userId;
    private Integer status;      // 1显示/0隐藏
    private String keyword;      // 内容模糊
}
