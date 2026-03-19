package com.melonecom.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductUpdateDTO {
    private Long productId;
    private String name;
    private String subTitle;
    private Long brandId;
    private Long categoryId;
    private String mainImageUrl;
    private String detailHtml;
    private Integer status;

    private List<String> imageUrls; // 简化：传全量，服务端先删后插
    private List<SkuAddDTO> skus;   // 传全量，服务端先删后插
}
