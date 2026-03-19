package com.melonecom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melonecom.mapper.StockReservationMapper;
import com.melonecom.model.dto.StockReservationSearchDTO;
import com.melonecom.model.entity.StockReservation;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IStockReservationService;
import org.springframework.stereotype.Service;

@Service
public class StockReservationServiceImpl
        extends ServiceImpl<StockReservationMapper, StockReservation>
        implements IStockReservationService {

    @Override
    public Result<PageResult<StockReservation>> getAllReservations(StockReservationSearchDTO dto) {
        // TODO: 分页查询 reservation（orderNo/status/skuId/warehouseId/expireAt）
        return Result.success(new PageResult<>());
    }
}
