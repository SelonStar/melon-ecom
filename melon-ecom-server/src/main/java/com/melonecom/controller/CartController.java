package com.melonecom.controller;

import com.melonecom.model.dto.CartAddDTO;
import com.melonecom.model.dto.CartBatchAddDTO;
import com.melonecom.model.dto.CartUpdateDTO;
import com.melonecom.result.Result;
import com.melonecom.service.ICartService;
import com.melonecom.util.ThreadLocalUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final ICartService cartService;
    public CartController(ICartService cartService) { this.cartService = cartService; }

    private Long uid() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Object v = claims.get("userId");
        return v == null ? null : Long.valueOf(v.toString());
    }

    @PostMapping("/items")
    public Result<?> add(@RequestBody CartAddDTO dto) {
        return cartService.addItem(uid(), dto.getSkuId(), dto.getQuantity());
    }

    @PutMapping("/items/{skuId}")
    public Result<?> update(@PathVariable Long skuId, @RequestBody CartUpdateDTO dto) {
        return cartService.updateQty(uid(), skuId, dto.getQuantity());
    }

    @DeleteMapping("/items/{skuId}")
    public Result<?> remove(@PathVariable Long skuId) {
        return cartService.removeItem(uid(), skuId);
    }

    @GetMapping
    public Result<?> get() {
        return cartService.getCart(uid());
    }

    @PostMapping("/items/batch")
    public Result<?> batchAdd(@RequestBody CartBatchAddDTO dto) {
        return cartService.batchAddItems(uid(), dto);
    }

    @PostMapping("/checkout")
    public Result<?> checkout() {
        return cartService.checkout(uid());
    }
}
