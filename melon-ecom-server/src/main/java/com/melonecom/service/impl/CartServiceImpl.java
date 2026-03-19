package com.melonecom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melonecom.constant.OrderConstant;
import com.melonecom.constant.StockBizConstant;
import com.melonecom.mapper.*;
import com.melonecom.model.dto.CartBatchAddDTO;
import com.melonecom.model.dto.CartBatchAddItemDTO;
import com.melonecom.model.entity.*;
import com.melonecom.model.vo.CartBatchAddItemVO;
import com.melonecom.model.vo.CartBatchAddVO;
import com.melonecom.result.Result;
import com.melonecom.service.ICartService;
import com.melonecom.model.vo.CartItemVO;
import com.melonecom.model.vo.CartVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements ICartService {

    private final CartItemMapper cartItemMapper;
    private final SkuMapper skuMapper;
    private final StockMapper stockMapper;
    private final StockTxnMapper stockTxnMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public CartServiceImpl(CartItemMapper cartItemMapper,
                           SkuMapper skuMapper,
                           StockMapper stockMapper,
                           StockTxnMapper stockTxnMapper,
                           OrderMapper orderMapper,
                           OrderItemMapper orderItemMapper) {
        this.cartItemMapper = cartItemMapper;
        this.skuMapper = skuMapper;
        this.stockMapper = stockMapper;
        this.stockTxnMapper = stockTxnMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public Result<?> addItem(Long userId, Long skuId, Integer qty) {
        if (userId == null) return Result.error("未登录");
        if (skuId == null || qty == null || qty <= 0) return Result.error("参数错误");

        Sku sku = skuMapper.selectById(skuId);
        if (sku == null || sku.getStatus() == 0) return Result.error("SKU 不存在或已停用");

        CartItem exist = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSkuId, skuId));

        if (exist == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setSkuId(skuId);
            item.setQuantity(qty);
            item.setChecked(1);
            cartItemMapper.insert(item);
        } else {
            exist.setQuantity(exist.getQuantity() + qty);
            cartItemMapper.updateById(exist);
        }
        return Result.success();
    }

    @Override
    public Result<?> updateQty(Long userId, Long skuId, Integer qty) {
        if (userId == null) return Result.error("未登录");
        if (skuId == null || qty == null || qty <= 0) return Result.error("参数错误");

        CartItem exist = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSkuId, skuId));
        if (exist == null) return Result.error("购物车条目不存在");

        exist.setQuantity(qty);
        cartItemMapper.updateById(exist);
        return Result.success();
    }

    @Override
    public Result<?> removeItem(Long userId, Long skuId) {
        if (userId == null) return Result.error("未登录");
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSkuId, skuId));
        return Result.success();
    }

    @Override
    public Result<?> getCart(Long userId) {
        if (userId == null) return Result.error("未登录");

        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCartItemId));
    


        if (items.isEmpty()) {
            CartVO vo = new CartVO();
            vo.setItems(Collections.emptyList());
            vo.setTotalAmount(BigDecimal.ZERO);
            return Result.success(vo);
        }

        Set<Long> skuIds = items.stream().map(CartItem::getSkuId).collect(Collectors.toSet());
        Map<Long, Sku> skuMap = skuMapper.selectBatchIds(skuIds).stream()
                .collect(Collectors.toMap(Sku::getSkuId, x -> x, (a, b) -> a));

        BigDecimal total = BigDecimal.ZERO;
        List<CartItemVO> voItems = new ArrayList<>();
        for (CartItem ci : items) {
            Sku sku = skuMap.get(ci.getSkuId());
            if (sku == null) continue;

            BigDecimal line = sku.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            CartItemVO vo = new CartItemVO();
            vo.setSkuId(ci.getSkuId());
            vo.setSkuName(sku.getName());
            vo.setImageUrl(sku.getImageUrl());
            vo.setPrice(sku.getPrice());
            vo.setQuantity(ci.getQuantity());
            vo.setChecked(ci.getChecked());
            vo.setLineAmount(line);
            voItems.add(vo);

            if (ci.getChecked() != null && ci.getChecked() == 1) total = total.add(line);
        }

        CartVO cartVO = new CartVO();
        cartVO.setItems(voItems);
        cartVO.setTotalAmount(total);
        return Result.success(cartVO);
    }

    @Override
    public Result<?> batchAddItems(Long userId, CartBatchAddDTO dto) {
        if (userId == null) return Result.error("未登录");
        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            return Result.error("请至少选择一件商品");
        }

        List<CartBatchAddItemVO> results = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (CartBatchAddItemDTO item : dto.getItems()) {
            if (item == null || item.getSkuId() == null) {
                CartBatchAddItemVO resultItem = new CartBatchAddItemVO();
                resultItem.setSuccess(false);
                resultItem.setMessage("skuId 不能为空");
                results.add(resultItem);
                failCount++;
                continue;
            }

            int quantity = item.getQuantity() == null || item.getQuantity() <= 0 ? 1 : item.getQuantity();
            Result<?> addResult = addItem(userId, item.getSkuId(), quantity);

            CartBatchAddItemVO resultItem = new CartBatchAddItemVO();
            resultItem.setSkuId(item.getSkuId());
            resultItem.setQuantity(quantity);
            resultItem.setSuccess(addResult.getCode() != null && addResult.getCode() == 0);
            resultItem.setMessage(addResult.getMessage());
            results.add(resultItem);

            if (Boolean.TRUE.equals(resultItem.getSuccess())) successCount++;
            else failCount++;
        }

        CartBatchAddVO vo = new CartBatchAddVO();
        vo.setItems(results);
        vo.setSuccessCount(successCount);
        vo.setFailCount(failCount);
        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> checkout(Long userId) {
        if (userId == null) return Result.error("未登录");

        List<CartItem> checkedItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getChecked, 1));

        if (checkedItems.isEmpty()) return Result.error("购物车为空或未勾选商品");

        Set<Long> skuIds = checkedItems.stream().map(CartItem::getSkuId).collect(Collectors.toSet());
        Map<Long, Sku> skuMap = skuMapper.selectBatchIds(skuIds).stream()
                .collect(Collectors.toMap(Sku::getSkuId, x -> x, (a, b) -> a));

        // 过滤掉 SKU 不存在或已停用的购物车项，并从购物车删除
        List<Long> invalidSkuIds = checkedItems.stream()
                .map(CartItem::getSkuId)
                .filter(id -> { Sku s = skuMap.get(id); return s == null || Integer.valueOf(0).equals(s.getStatus()); })
                .collect(Collectors.toList());
        if (!invalidSkuIds.isEmpty()) {
            cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId, userId)
                    .in(CartItem::getSkuId, invalidSkuIds));
            checkedItems = checkedItems.stream()
                    .filter(ci -> !invalidSkuIds.contains(ci.getSkuId()))
                    .collect(Collectors.toList());
        }
        if (checkedItems.isEmpty()) return Result.error("所选商品均已下架或不存在，购物车已自动清理");

        String orderNo = genOrderNo(userId);
        BigDecimal total = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        // 1) 锁库存（available-qty locked+qty）
        for (CartItem ci : checkedItems) {
            Sku sku = skuMap.get(ci.getSkuId());

            Long whId = stockMapper.selectWarehouseIdBySkuId(ci.getSkuId());
            if (whId == null) return Result.error("库存不存在 skuId=" + ci.getSkuId());

            Stock stock = stockMapper.selectByWarehouseAndSku(whId, ci.getSkuId());
            if (stock == null) return Result.error("库存记录不存在 skuId=" + ci.getSkuId());

            boolean lockOk = tryUpdateStockWithRetry(
                    stock.getStockId(),
                    () -> stockMapper.selectById(stock.getStockId()),
                    (id, version) -> stockMapper.updateAvailableAndLockedWithOptimisticLock(
                            id, version, -ci.getQuantity(), +ci.getQuantity())
            );
            if (!lockOk) return Result.error("库存不足或并发冲突，请重试 skuId=" + ci.getSkuId());

            StockTxn st = new StockTxn();
            st.setBizType(StockBizConstant.LOCK);
            st.setBizNo(orderNo);
            st.setWarehouseId(whId);
            st.setSkuId(ci.getSkuId());
            st.setDeltaAvailable(-ci.getQuantity());
            st.setDeltaLocked(+ci.getQuantity());
            st.setRemark("checkout lock");
            stockTxnMapper.insert(st);

            BigDecimal line = sku.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(line);

            OrderItem oi = new OrderItem();
            oi.setSkuId(ci.getSkuId());
            oi.setWarehouseId(whId);
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(sku.getPrice());
            oi.setAmount(line);
            orderItems.add(oi);
        }

        // 2) 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderConstant.UNPAID);
        order.setTotalAmount(total);
        orderMapper.insert(order);

        // 3) 创建订单项
        // for (OrderItem oi : orderItems) {
        //     oi.setOrderId(order.getOrderId());
        //     orderItemMapper.insert(oi);
        // }
        //批量插入
        orderItems.forEach(oi -> oi.setOrderId(order.getOrderId()));
        orderItemMapper.batchInsert(orderItems);

        // 4) 删除已勾选购物车项
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getChecked, 1));

        Map<String, Object> resp = new HashMap<>();
        resp.put("orderId", order.getOrderId());
        resp.put("orderNo", orderNo);
        resp.put("totalAmount", total);
        return Result.success(resp);
    }

    private String genOrderNo(Long userId) {
        return "OD" + System.currentTimeMillis() + "-" + userId + "-" + (int) (Math.random() * 10000);
    }

    private boolean tryUpdateStockWithRetry(Long stockId,
                                           Supplier<Stock> reselect,
                                           BiFunction<Long, Integer, Integer> updater) {
        int maxRetry = 5;
        for (int i = 0; i < maxRetry; i++) {
            Stock latest = reselect.get();
            if (latest == null) return false;
            int rows = updater.apply(stockId, latest.getVersion());
            if (rows == 1) return true;
        }
        return false;
    }
}
