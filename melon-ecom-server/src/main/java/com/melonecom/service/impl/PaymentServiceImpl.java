package com.melonecom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.melonecom.constant.OrderConstant;
import com.melonecom.constant.PayTxnConstant;
import com.melonecom.constant.PayTypeConstant;
import com.melonecom.constant.StockBizConstant;
import com.melonecom.mapper.*;
import com.melonecom.model.entity.*;
import com.melonecom.mapper.WalletTxnMapper;
import com.melonecom.result.Result;
import com.melonecom.service.IPaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Service
public class PaymentServiceImpl implements IPaymentService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserWalletMapper userWalletMapper;
    private final PayTxnMapper payTxnMapper;
    private final StockMapper stockMapper;
    private final StockTxnMapper stockTxnMapper;
    private final WalletTxnMapper walletTxnMapper;

    public PaymentServiceImpl(OrderMapper orderMapper,
                              OrderItemMapper orderItemMapper,
                              UserWalletMapper userWalletMapper,
                              PayTxnMapper payTxnMapper,
                              StockMapper stockMapper,
                              StockTxnMapper stockTxnMapper,
                              WalletTxnMapper walletTxnMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userWalletMapper = userWalletMapper;
        this.payTxnMapper = payTxnMapper;
        this.stockMapper = stockMapper;
        this.stockTxnMapper = stockTxnMapper;
        this.walletTxnMapper = walletTxnMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> balancePay(Long userId, Long orderId) {
        if (userId == null) return Result.error("未登录");
        if (orderId == null) return Result.error("参数错误：orderId 为空");

        // 0) 查订单
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) return Result.error("订单不存在");

        // 已支付直接幂等返回
        if (order.getStatus() != null && order.getStatus() == OrderConstant.PAID) {
            return Result.success("已支付");
        }
        // 只允许未支付进入支付流程
        if (order.getStatus() == null || order.getStatus() != OrderConstant.UNPAID) {
            return Result.error("订单不可支付");
        }

        // 1) 幂等创建支付流水：order_no 唯一（并发不会抛异常）
        payTxnMapper.insertIgnore(
                order.getOrderNo(),
                userId,
                PayTypeConstant.BALANCE,
                order.getTotalAmount(),
                PayTxnConstant.PROCESSING
        );

        PayTxn existTxn = payTxnMapper.selectByOrderNo(order.getOrderNo());
        if (existTxn != null && existTxn.getStatus() == PayTxnConstant.SUCCESS) {
            return Result.success("已支付");
        }

        // 2) 抢支付权：UNPAID -> PAYING（只有一个线程能成功）
        int grab = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getOrderId, orderId)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getStatus, OrderConstant.UNPAID)
                        .set(Order::getStatus, OrderConstant.PAYING)
        );
        if (grab != 1) {
            // 被别的线程支付/取消了
            Order latest = orderMapper.selectById(orderId);
            if (latest != null && latest.getStatus() != null && latest.getStatus() == OrderConstant.PAID) {
                return Result.success("已支付");
            }
            return Result.error("订单状态已变更，无法支付");
        }

        // 3) 扣钱包（乐观锁重试）
        boolean walletOk = tryDeductWalletWithRetry(userId, order.getTotalAmount());
        if (!walletOk) {
            // 回退订单状态：PAYING -> UNPAID
            orderMapper.update(null,
                    new LambdaUpdateWrapper<Order>()
                            .eq(Order::getOrderId, orderId)
                            .eq(Order::getStatus, OrderConstant.PAYING)
                            .set(Order::getStatus, OrderConstant.UNPAID)
            );
            // 支付流水标失败
            payTxnMapper.update(null,
                    new LambdaUpdateWrapper<PayTxn>()
                            .eq(PayTxn::getOrderNo, order.getOrderNo())
                            .set(PayTxn::getStatus, PayTxnConstant.FAIL)
            );
            return Result.error("余额不足或并发冲突，请重试");
        }

        // 3.5) 写钱包流水
        UserWallet updatedWallet = userWalletMapper.selectById(userId);
        WalletTxn walletTxn = new WalletTxn();
        walletTxn.setUserId(userId);
        walletTxn.setAmount(order.getTotalAmount());
        walletTxn.setType("PAY");
        walletTxn.setBalanceAfter(updatedWallet != null ? updatedWallet.getBalance() : java.math.BigDecimal.ZERO);
        walletTxn.setRelatedOrderId(orderId);
        walletTxn.setRemark("订单支付 " + order.getOrderNo());
        walletTxnMapper.insert(walletTxn);

        // 4) 扣 locked（locked -= qty）
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));

        for (OrderItem oi : items) {
            Stock stock = stockMapper.selectByWarehouseAndSku(oi.getWarehouseId(), oi.getSkuId());
            if (stock == null) {
                throw new RuntimeException("库存不存在 skuId=" + oi.getSkuId() + ", warehouseId=" + oi.getWarehouseId());
            }

            boolean ok = tryUpdateStockWithRetry(
                    stock.getStockId(),
                    () -> stockMapper.selectById(stock.getStockId()),
                    (id, ver) -> stockMapper.updateAvailableAndLockedWithOptimisticLock(
                            id, ver, 0, -oi.getQuantity())
            );
            if (!ok) {
                throw new RuntimeException("扣减锁定库存失败 skuId=" + oi.getSkuId() + ", warehouseId=" + oi.getWarehouseId());
            }

            StockTxn st = new StockTxn();
            st.setBizType(StockBizConstant.DEDUCT);
            st.setBizNo(order.getOrderNo());
            st.setWarehouseId(oi.getWarehouseId());
            st.setSkuId(oi.getSkuId());
            st.setDeltaAvailable(0);
            st.setDeltaLocked(-oi.getQuantity());
            st.setRemark("pay deduct locked");
            stockTxnMapper.insert(st);
        }

        // 5) 订单 PAYING -> PAID
        orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getOrderId, orderId)
                        .eq(Order::getStatus, OrderConstant.PAYING)
                        .set(Order::getStatus, OrderConstant.PAID)
                        .set(Order::getPayTime, LocalDateTime.now())
        );

        // 6) 支付流水成功
        payTxnMapper.update(null,
                new LambdaUpdateWrapper<PayTxn>()
                        .eq(PayTxn::getOrderNo, order.getOrderNo())
                        .set(PayTxn::getStatus, PayTxnConstant.SUCCESS)
        );

        return Result.success("支付成功");
    }

    @Override
    public Result<?> getWallet(Long userId) {
        if (userId == null) return Result.error("未登录");
        UserWallet wallet = userWalletMapper.selectById(userId);
        java.math.BigDecimal balance = wallet == null ? java.math.BigDecimal.ZERO : wallet.getBalance();
        return Result.success(balance);
    }

    private boolean tryDeductWalletWithRetry(Long userId, BigDecimal amount) {
        int maxRetry = 5;
        for (int i = 0; i < maxRetry; i++) {
            UserWallet wallet = userWalletMapper.selectById(userId);
            if (wallet == null) {
                // 这里建议：钱包不存在就视为余额不足（不要在支付链路里偷偷创建钱包）
                return false;
            }
            int rows = userWalletMapper.deductBalance(userId, amount, wallet.getVersion());
            if (rows == 1) return true;
        }
        return false;
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
