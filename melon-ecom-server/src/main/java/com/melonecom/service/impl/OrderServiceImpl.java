package com.melonecom.service.impl;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melonecom.constant.OrderConstant;
import com.melonecom.constant.StockBizConstant;
import com.melonecom.mapper.*;
import com.melonecom.model.entity.*;
import com.melonecom.result.Result;
import com.melonecom.service.IOrderService;
import com.melonecom.model.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SkuMapper skuMapper;
    private final StockMapper stockMapper;
    private final StockTxnMapper stockTxnMapper;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            SkuMapper skuMapper,
                            StockMapper stockMapper,
                            StockTxnMapper stockTxnMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.skuMapper = skuMapper;
        this.stockMapper = stockMapper;
        this.stockTxnMapper = stockTxnMapper;
    }

    @Override
    public Result<?> listMyOrders(Long userId, Integer page, Integer size) {
        if (userId == null) return Result.error("未登录");

        Page<Order> p = new Page<>(page, size);
        Page<Order> res = orderMapper.selectPage(p, new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getOrderId));

        List<OrderListVO> list = res.getRecords().stream().map(o -> {
            OrderListVO vo = new OrderListVO();
            vo.setOrderId(o.getOrderId());
            vo.setOrderNo(o.getOrderNo());
            vo.setStatus(o.getStatus());
            vo.setTotalAmount(o.getTotalAmount());
            vo.setCreateTime(o.getCreateTime());
            vo.setPayTime(o.getPayTime());
            return vo;
        }).toList();

        return Result.success(list);
    }

    @Override
    public Result<?> getDetail(Long userId, Long orderId) {
        if (userId == null) return Result.error("未登录");

        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) return Result.error("订单不存在");

        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));

        Map<Long, Sku> skuMap = skuMapper.selectBatchIds(
                items.stream().map(OrderItem::getSkuId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(Sku::getSkuId, x -> x, (a, b) -> a));

        List<OrderItemVO> itemVOs = items.stream().map(oi -> {
            Sku sku = skuMap.get(oi.getSkuId());
            OrderItemVO vo = new OrderItemVO();
            vo.setSkuId(oi.getSkuId());
            vo.setSkuName(sku == null ? null : sku.getName());
            vo.setWarehouseId(oi.getWarehouseId());
            vo.setQuantity(oi.getQuantity());
            vo.setUnitPrice(oi.getUnitPrice());
            vo.setAmount(oi.getAmount());
            return vo;
        }).toList();

        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrderId(order.getOrderId() );
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setItems(itemVOs);

        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> cancel(Long userId, Long orderId) {
        if (userId == null) return Result.error("未登录");

        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) return Result.error("订单不存在");
        if (order.getStatus() != OrderConstant.UNPAID) return Result.error("仅未支付订单可取消");

        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));

        // 1) 释放库存（available+qty locked-qty）
        for (OrderItem oi : items) {
            Stock stock = stockMapper.selectByWarehouseAndSku(oi.getWarehouseId(), oi.getSkuId());
            if (stock == null) return Result.error("库存不存在 skuId=" + oi.getSkuId());

            boolean ok = tryUpdateStockWithRetry(
                    stock.getStockId(),
                    () -> stockMapper.selectById(stock.getStockId()),
                    (id, ver) -> stockMapper.updateAvailableAndLockedWithOptimisticLock(
                            id, ver, +oi.getQuantity(), -oi.getQuantity())
            );
            if (!ok) return Result.error("释放库存失败（并发冲突）skuId=" + oi.getSkuId());

            StockTxn st = new StockTxn();
            st.setBizType(StockBizConstant.RELEASE);
            st.setBizNo(order.getOrderNo());
            st.setWarehouseId(oi.getWarehouseId());
            st.setSkuId(oi.getSkuId());
            st.setDeltaAvailable(+oi.getQuantity());
            st.setDeltaLocked(-oi.getQuantity());
            st.setRemark("cancel release");
            stockTxnMapper.insert(st);
        }

        // 2) 条件更新订单状态（更严谨）
        int rows = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getOrderId, orderId)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getStatus, OrderConstant.UNPAID)
                        .set(Order::getStatus, OrderConstant.CANCELLED)
        );
        if (rows != 1) return Result.error("取消失败，订单状态已变更");

        return Result.success("取消成功");
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
