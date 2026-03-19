package com.melonecom.model.vo;

import lombok.Data;

@Data
public class TryOnProviderQueryVO {
    private String providerTaskId;
    private String taskStatus;
    private String resultImageUrl;
    private String rawResponse;
    private String errorMessage;
}
