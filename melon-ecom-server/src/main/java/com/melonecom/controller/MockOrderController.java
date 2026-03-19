package com.melonecom.controller;

import com.melonecom.result.Result;
import com.melonecom.service.IMockOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/mock")
public class MockOrderController {

    private final IMockOrderService mockOrderService;

    public MockOrderController(IMockOrderService mockOrderService) {
        this.mockOrderService = mockOrderService;
    }

    /**
     * 该接口用于本地模拟订单数据，生成销量数据，为首页推荐和热销排序提供数据支撑。
     */
    @PostMapping("/order")
    public Result<?> mockOrder(@RequestParam("count") Integer count) {
        return mockOrderService.mockOrder(count);
    }
}
