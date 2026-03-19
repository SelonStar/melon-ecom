package com.melonecom.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_pay_txn")
public class PayTxn {

    @TableId(value = "id", type = IdType.AUTO)
    private Long payTxnId;

    private String orderNo;
    private Long userId;
    private Integer payType; // 1余额
    private BigDecimal amount;
    private Integer status;  // 0处理中 1成功 2失败

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
