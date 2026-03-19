package com.melonecom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melonecom.model.dto.StockReservationSearchDTO;
import com.melonecom.model.entity.StockReservation;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;

public interface IStockReservationService extends IService<StockReservation> {

    Result<PageResult<StockReservation>> getAllReservations(StockReservationSearchDTO dto);
}
