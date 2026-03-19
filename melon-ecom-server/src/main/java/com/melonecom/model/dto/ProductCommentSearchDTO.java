package com.melonecom.model.dto;

import lombok.Data;

@Data
public class ProductCommentSearchDTO {
    private Integer page = 1;
    private Integer pageSize = 10;

    private Long productId;
    private Integer sortType = 0; // 0按时间 1按点赞
}
