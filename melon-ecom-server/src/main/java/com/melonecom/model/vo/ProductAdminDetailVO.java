package com.melonecom.model.vo;

import com.melonecom.model.entity.Sku;
import lombok.Data;

import java.util.List;

@Data
public class ProductAdminDetailVO {
    private Long productId;
    private String name;
    private String subTitle;
    private Long brandId;
    private Long categoryId;
    private String mainImageUrl;
    private String detailHtml;
    private Integer status;

    private List<String> imageUrls;
    private List<Sku> skus; // 简化：详情直接返回 sku entity
}
