package com.melonecom.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BannerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private Integer pageNum;

    @NotNull
    private Integer pageSize;

    /**
     * 可选过滤：1启用 0禁用；为空则不过滤
     */
    private Integer status;
}