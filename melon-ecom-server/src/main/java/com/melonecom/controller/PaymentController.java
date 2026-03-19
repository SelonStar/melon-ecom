package com.melonecom.controller;

import com.melonecom.model.dto.BalancePayDTO;
import com.melonecom.result.Result;
import com.melonecom.service.IPaymentService;
import com.melonecom.util.ThreadLocalUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final IPaymentService IPaymentService;
    public PaymentController(IPaymentService IPaymentService) { this.IPaymentService = IPaymentService; }

    private Long uid() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Object v = claims.get("userId");
        return v == null ? null : Long.valueOf(v.toString());
    }

    @PostMapping("/balance/pay")
    public Result<?> balancePay(@RequestBody BalancePayDTO dto) {
        return IPaymentService.balancePay(uid(), dto.getOrderId());
    }

    @GetMapping("/wallet")
    public Result<?> wallet() {
        return IPaymentService.getWallet(uid());
    }
}
