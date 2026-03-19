package com.melonecom.constant;

/**
 * 库存流水业务类型：必须与 tb_stock_txn.biz_type 一致
 * 1-LOCK 2-RELEASE 3-DEDUCT 4-ADJUST 5-INBOUND 6-OUTBOUND
 */
public class StockBizConstant {

    private StockBizConstant() {}

    public static final int LOCK = 1;      // 原 RESERVE 的语义（锁定/占用）
    public static final int RELEASE = 2;   // 释放锁定库存
    public static final int DEDUCT = 3;    // 支付成功后扣减锁定库存
    public static final int ADJUST = 4;    // 后台调整
    public static final int INBOUND = 5;   // 入库
    public static final int OUTBOUND = 6;  // 出库
}
