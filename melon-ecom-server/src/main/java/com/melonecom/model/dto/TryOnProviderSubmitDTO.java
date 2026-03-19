package com.melonecom.model.dto;

import lombok.Data;

@Data
public class TryOnProviderSubmitDTO {
    private String taskId;
    private String providerCode;
    private String personImageUrl;
    private String photoType;
    private String currentResultImageUrl;
    private String topImageUrl;
    private String bottomImageUrl;
    private String dressImageUrl;
    private String shoesImageUrl;
}
