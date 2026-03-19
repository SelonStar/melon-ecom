package com.melonecom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melonecom.model.dto.StockAdjustDTO;
import com.melonecom.model.dto.StockSearchDTO;
import com.melonecom.model.entity.Stock;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;

public interface IStockService extends IService<Stock> {

    Result<Long> getAllStocksCount(Long skuId, Long warehouseId);

    Result<PageResult<Stock>> getAllStocks(StockSearchDTO dto);

    /**
     * 库存调整：delta>0 入库；delta<0 出库/修正
     * 典型实现：更新 tb_stock，同时写一条 tb_stock_txn(ADJUST/INBOUND/OUTBOUND)
     */
    Result<?> adjustStock(StockAdjustDTO dto);
}
