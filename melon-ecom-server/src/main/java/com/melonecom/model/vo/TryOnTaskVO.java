package com.melonecom.model.vo;

import lombok.Data;

@Data
public class TryOnTaskVO {
    private Long taskId;
    private String status;
    private String photoUrl;
    private String photoType;
    private Long topSkuId;
    private Long bottomSkuId;
    private Long dressSkuId;
    private Long shoesSkuId;
    private String providerCode;
    private String providerTaskId;
    private String resultImageUrl;
    private String errorMessage;
}
