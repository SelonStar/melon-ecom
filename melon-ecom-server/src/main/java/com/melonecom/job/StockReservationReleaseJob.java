package com.melonecom.job;

import com.melonecom.constant.StockBizConstant;
import com.melonecom.constant.StockBizConstant;
import com.melonecom.mapper.StockMapper;
import com.melonecom.mapper.StockReservationMapper;
import com.melonecom.mapper.StockTxnMapper;
import com.melonecom.model.entity.Stock;
import com.melonecom.model.entity.StockReservation;
import com.melonecom.model.entity.StockTxn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StockReservationReleaseJob {

    @Autowired
    private StockReservationMapper stockReservationMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StockTxnMapper stockTxnMapper;

    /**
     * 每 30 秒扫一次超时锁定（你也可以改成 60s）
     */
    @Scheduled(fixedDelay = 30_000L)
    public void run() {
        // 每轮最多处理 200 条，避免长事务/长锁
        int batchSize = 200;

        while (true) {
            LocalDateTime now = LocalDateTime.now();
            List<StockReservation> expired = stockReservationMapper.selectExpiredLocked(now, batchSize);
            if (expired == null || expired.isEmpty()) return;

            for (StockReservation r : expired) {
                try {
                    releaseOne(r.getReservationId());
                } catch (Exception ignored) {
                    // 生产建议记录日志（orderNo/reservationId），这里先不打断批处理
                }
            }

            // 如果不足 batchSize，说明扫完了
            if (expired.size() < batchSize) return;
        }
    }

    /**
     * 释放一条锁定记录：CAS 抢占 -> 回滚库存 -> 写流水
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseOne(Long reservationId) {
        LocalDateTime now = LocalDateTime.now();

        // 1) CAS 抢占释放权（只有抢到的人才能继续）
        int updated = stockReservationMapper.markReleasedIfLocked(reservationId, now);
        if (updated == 0) {
            // 已被别的线程/实例释放或状态已变化，直接返回
            return;
        }

        // 2) 读取 reservation 最新数据（也可以在 selectExpiredLocked 里带全字段，这里稳妥再查一次）
        StockReservation r = stockReservationMapper.selectById(reservationId);
        if (r == null) return;

        Integer qty = r.getQty();
        if (qty == null || qty <= 0) return;

        // 3) 查库存行
        Stock stock = stockMapper.selectByWarehouseAndSku(r.getWarehouseId(), r.getSkuId());
        if (stock == null) {
            // 理论上不应发生：有锁定就应该有 stock 行
            // 可以选择：直接结束（或报警）
            return;
        }

        int beforeAvail = stock.getAvailable() == null ? 0 : stock.getAvailable();
        int beforeLocked = stock.getLocked() == null ? 0 : stock.getLocked();

        // 4) 回滚库存：available += qty，locked -= qty（乐观锁 + locked 不允许负数）
        int affected = stockMapper.updateAvailableAndLockedWithOptimisticLock(
                stock.getStockId(),
                stock.getVersion(),
                qty,
                -qty
        );

        if (affected == 0) {
            // 并发冲突：版本不一致 或 locked 不够（异常数据）
            // 这里选择：不抛异常，避免 job 频繁回滚；但建议记录日志做排查
            return;
        }

        // 5) 写流水（审计）
        Stock after = stockMapper.selectById(stock.getStockId());
        int afterAvail = after.getAvailable() == null ? 0 : after.getAvailable();
        int afterLocked = after.getLocked() == null ? 0 : after.getLocked();

        StockTxn txn = new StockTxn();
        txn.setBizType(StockBizConstant.RELEASE);
        txn.setBizNo(r.getOrderNo());              // 用订单号做关联
        txn.setWarehouseId(r.getWarehouseId());
        txn.setSkuId(r.getSkuId());

        txn.setDeltaAvailable(qty);
        txn.setDeltaLocked(-qty);

        txn.setBeforeAvailable(beforeAvail);
        txn.setBeforeLocked(beforeLocked);
        txn.setAfterAvailable(afterAvail);
        txn.setAfterLocked(afterLocked);

        txn.setRemark("reservation timeout release, reservationId=" + reservationId);

        stockTxnMapper.insertTxn(txn);
    }
}
