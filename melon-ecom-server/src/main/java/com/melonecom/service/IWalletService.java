package com.melonecom.service;

import com.melonecom.result.Result;

import java.math.BigDecimal;

public interface IWalletService {
    /** 管理员调整余额（ADD/SUB） */
    Result<?> adminAdjust(Long userId, BigDecimal amount, String type, String remark);

    /** 查询余额（用户端） */
    Result<?> getBalance(Long userId);
}
