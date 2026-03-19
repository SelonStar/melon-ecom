package com.melonecom.service;

import com.melonecom.result.Result;

public interface IPaymentService {
    Result<?> balancePay(Long userId, Long orderId);
    Result<?> getWallet(Long userId);
}
