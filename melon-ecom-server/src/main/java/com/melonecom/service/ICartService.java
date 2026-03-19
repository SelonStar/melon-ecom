package com.melonecom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melonecom.model.dto.CartBatchAddDTO;
import com.melonecom.model.entity.CartItem;
import com.melonecom.result.Result;

public interface ICartService extends IService<CartItem> {

    Result<?> addItem(Long userId, Long skuId, Integer qty);

    Result<?> updateQty(Long userId, Long skuId, Integer qty);

    Result<?> removeItem(Long userId, Long skuId);

    Result<?> getCart(Long userId);

    Result<?> batchAddItems(Long userId, CartBatchAddDTO dto);

    Result<?> checkout(Long userId);
}
