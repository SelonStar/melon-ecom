package com.melonecom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melonecom.model.dto.StockTxnSearchDTO;
import com.melonecom.model.entity.StockTxn;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;

public interface IStockTxnService extends IService<StockTxn> {

    Result<PageResult<StockTxn>> getAllTxns(StockTxnSearchDTO dto);
}
