package com.melonecom.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductAddDTO {
    private String name;
    private String subTitle;
    private Long brandId;
    private Long categoryId;
    private String mainImageUrl;
    private String detailHtml;

    // 可选：商品图片
    private List<String> imageUrls;

    // 可选：创建时一并创建 sku
    private List<SkuAddDTO> skus;
}
