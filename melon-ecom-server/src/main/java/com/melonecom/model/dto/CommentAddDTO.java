package com.melonecom.model.dto;

import lombok.Data;

@Data
public class CommentAddDTO {
    private Long productId;
    private Long skuId;      // 可空
    private Integer rating;  // 1-5
    private String content;
}
