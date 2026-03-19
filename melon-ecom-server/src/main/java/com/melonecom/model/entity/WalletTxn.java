package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_wallet_txn")
public class WalletTxn {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal amount;

    /** ADD / SUB / PAY / REFUND */
    private String type;

    private BigDecimal balanceAfter;

    private Long relatedOrderId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
