package com.melonecom.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletAdjustDTO {

    @NotNull(message = "userId 不能为空")
    private Long userId;

    @NotNull(message = "amount 不能为空")
    @DecimalMin(value = "0.01", message = "amount 必须大于 0")
    private BigDecimal amount;

    /** ADD 或 SUB */
    @NotBlank(message = "type 不能为空")
    private String type;

    private String remark;
}
