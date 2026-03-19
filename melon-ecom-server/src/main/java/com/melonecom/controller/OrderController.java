package com.melonecom.controller;

import com.melonecom.result.Result;
import com.melonecom.service.IOrderService;
import com.melonecom.util.ThreadLocalUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final IOrderService orderService;
    public OrderController(IOrderService orderService) { this.orderService = orderService; }

    private Long uid() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Object v = claims.get("userId");
        return v == null ? null : Long.valueOf(v.toString());
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "10") Integer size) {
        return orderService.listMyOrders(uid(), page, size);
    }

    @GetMapping("/{orderId}")
    public Result<?> detail(@PathVariable Long orderId) {
        return orderService.getDetail(uid(), orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public Result<?> cancel(@PathVariable Long orderId) {
        return orderService.cancel(uid(), orderId);
    }
}
