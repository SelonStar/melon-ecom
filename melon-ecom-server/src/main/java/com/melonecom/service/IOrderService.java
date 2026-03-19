package com.melonecom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melonecom.model.entity.Order;
import com.melonecom.result.Result;

public interface IOrderService extends IService<Order> {

    Result<?> listMyOrders(Long userId, Integer page, Integer size);

    Result<?> getDetail(Long userId, Long orderId);

    Result<?> cancel(Long userId, Long orderId);
}
