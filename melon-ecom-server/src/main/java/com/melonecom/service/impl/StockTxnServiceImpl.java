package com.melonecom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melonecom.mapper.StockTxnMapper;
import com.melonecom.model.dto.StockTxnSearchDTO;
import com.melonecom.model.entity.StockTxn;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IStockTxnService;
import org.springframework.stereotype.Service;

@Service
public class StockTxnServiceImpl
        extends ServiceImpl<StockTxnMapper, StockTxn>
        implements IStockTxnService {

    @Override
    public Result<PageResult<StockTxn>> getAllTxns(StockTxnSearchDTO dto) {
        // TODO: 分页查询 txn（bizNo/bizType/skuId/warehouseId/时间范围）
        return Result.success(new PageResult<>());
    }
}
