package com.melonecom.model.dto;

import lombok.Data;

@Data
public class TryOnCreateTaskDTO {
    private String photoUrl;
    private String photoType;
    private Long topSkuId;
    private Long bottomSkuId;
    private Long dressSkuId;
    private Long shoesSkuId;
    private String currentResultImageUrl;
    private String providerCode;
}
