package com.melonecom.model.dto;

import lombok.Data;

@Data
public class TryOnCandidateSkuQueryDTO {
    private String slotType;
    private String photoType;
    private String keyword;
    private Integer page;
    private Integer pageSize;
}
