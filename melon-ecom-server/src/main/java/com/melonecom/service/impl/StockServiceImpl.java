package com.melonecom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melonecom.mapper.SkuMapper;
import com.melonecom.mapper.StockMapper;
import com.melonecom.mapper.StockTxnMapper;
import com.melonecom.mapper.WarehouseMapper;
import com.melonecom.model.dto.StockAdjustDTO;
import com.melonecom.model.dto.StockSearchDTO;
import com.melonecom.model.entity.Sku;
import com.melonecom.model.entity.Stock;
import com.melonecom.model.entity.StockTxn;
import com.melonecom.model.entity.Warehouse;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements IStockService {

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private StockTxnMapper stockTxnMapper;

    // 你可按自己常量调整：这里先给最小约束（只允许这三类）
    // 假设：1=ADJUST, 2=INBOUND, 3=OUTBOUND（你不一致就改这三数字）
    private static final Set<Integer> ALLOWED_BIZ_TYPES = Set.of(1, 2, 3);

    @Override
    public Result<Long> getAllStocksCount(Long skuId, Long warehouseId) {
        // TODO: 你自己按条件 count（可用 MyBatis-Plus QueryWrapper）
        return Result.success(0L);
    }

    @Override
    public Result<PageResult<Stock>> getAllStocks(StockSearchDTO dto) {
        // TODO: 你自己补分页查询
        return Result.success(new PageResult<>());
    }

    /**
     * 库存调整（只调整 available，不动 locked）
     * - 事务：更新库存 + 写流水 必须同生共死
     * - 乐观锁：version 不一致或库存不足会导致 affected=0
     * - 不允许负库存：依赖 updateAvailableWithOptimisticLock 的 SQL 条件保证
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> adjustStock(StockAdjustDTO dto) {

        // 0) 参数校验（最小）
        if (dto == null || dto.getSkuId() == null || dto.getWarehouseId() == null || dto.getDelta() == null) {
            return Result.error("参数不完整：warehouseId/skuId/delta 必填");
        }
        if (dto.getDelta() == 0) {
            return Result.error("delta 不能为 0");
        }

        // 0.1) bizType 校验（防止流水语义污染）
        if (dto.getBizType() == null || !ALLOWED_BIZ_TYPES.contains(dto.getBizType())) {
            return Result.error("bizType 非法（只允许 ADJUST/INBOUND/OUTBOUND）");
        }

        // 1) 校验 SKU 存在且启用
        Sku sku = skuMapper.selectById(dto.getSkuId());
        if (sku == null) {
            return Result.error("SKU 不存在");
        }
        if (sku.getStatus() != null && sku.getStatus() == 0) {
            return Result.error("SKU 已停用，禁止调整库存");
        }

        // 2) 校验仓库存在且启用
        Warehouse wh = warehouseMapper.selectById(dto.getWarehouseId());
        if (wh == null) {
            return Result.error("仓库不存在");
        }
        if (wh.getStatus() != null && wh.getStatus() == 0) {
            return Result.error("仓库已停用，禁止调整库存");
        }

        // 3) 查 tb_stock（该仓 + 该 sku 的库存行）
        Stock stock = this.baseMapper.selectByWarehouseAndSku(dto.getWarehouseId(), dto.getSkuId());
        if (stock == null) {
            // 策略：不存在就报错（你也可以改成自动初始化）
            return Result.error("该仓库下尚未初始化该 SKU 的库存记录");
        }

        // 4) 乐观锁更新（带最多 2 次重试：解决“版本冲突导致的误报”）
        //    注意：库存不足重试也没用，但我们会在重试前判断是否库存不足
        int maxRetry = 2;
        int attempt = 0;

        Integer beforeAvailable = stock.getAvailable() == null ? 0 : stock.getAvailable();
        Integer beforeLocked = stock.getLocked() == null ? 0 : stock.getLocked();

        while (attempt <= maxRetry) {
            attempt++;

            // 4.1) 先做一次“快速库存不足判断”（只对 delta<0 有意义）
            if (dto.getDelta() < 0) {
                int wouldBe = (stock.getAvailable() == null ? 0 : stock.getAvailable()) + dto.getDelta();
                if (wouldBe < 0) {
                    return Result.error("库存不足，无法扣减");
                }
            }

            // 4.2) 调用 Mapper：available += delta，version=version+1
            //      且 SQL 必须带：
            //      WHERE id=? AND version=? AND (available + delta) >= 0
            int affected = this.baseMapper.updateAvailableWithOptimisticLock(
                    stock.getStockId(),      // 你确认主键列是 id，这里传的就是实体主键值
                    stock.getVersion(),
                    dto.getDelta()
            );

            if (affected == 1) {
                // 5) 成功：查最新用于写流水
                Stock after = this.baseMapper.selectById(stock.getStockId());
                if (after == null) {
                    // 理论不应发生
                    throw new RuntimeException("库存更新后查询失败");
                }

                // 6) 写库存流水 tb_stock_txn（与你的 insertTxn 对齐）
                StockTxn txn = new StockTxn();
                txn.setBizType(dto.getBizType());
                txn.setBizNo(dto.getBizNo());              // 可空
                txn.setWarehouseId(dto.getWarehouseId());
                txn.setSkuId(dto.getSkuId());

                txn.setDeltaAvailable(dto.getDelta());
                txn.setDeltaLocked(0);

                txn.setBeforeAvailable(beforeAvailable);
                txn.setBeforeLocked(beforeLocked);
                txn.setAfterAvailable(after.getAvailable() == null ? 0 : after.getAvailable());
                txn.setAfterLocked(after.getLocked() == null ? 0 : after.getLocked());

                txn.setRemark(dto.getRemark());

                int ins = stockTxnMapper.insertTxn(txn);
                if (ins != 1) {
                    // 事务会回滚库存更新，保证一致性
                    throw new RuntimeException("写入库存流水失败");
                }

                return Result.success(null);
            }

            // 4.3) affected==0：可能是版本冲突，也可能是库存不足（delta<0）
            //      重新查一次最新 stock，再决定是否重试
            Stock latest = this.baseMapper.selectById(stock.getStockId());
            if (latest == null) {
                throw new RuntimeException("并发冲突后查询库存失败");
            }

            // 如果 delta<0 且确实不足，直接返回不足（不用重试）
            if (dto.getDelta() < 0) {
                int wouldBe = (latest.getAvailable() == null ? 0 : latest.getAvailable()) + dto.getDelta();
                if (wouldBe < 0) {
                    return Result.error("库存不足，无法扣减");
                }
            }

            // 更新 stock 引用后继续重试（仅处理 version 冲突）
            stock = latest;

            if (attempt > maxRetry) {
                return Result.error("发生并发冲突，请重试");
            }
        }

        return Result.error("库存调整失败");
    }
}
