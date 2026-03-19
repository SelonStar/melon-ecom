package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.StockReservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StockReservationMapper extends BaseMapper<StockReservation> {
    // 查“已超时仍锁定”的记录（分页取一批，避免一次扫太多）
    List<StockReservation> selectExpiredLocked(@Param("now") LocalDateTime now,
                                               @Param("limit") Integer limit);

    // CAS 抢占：把 status 从 LOCKED(0) -> RELEASED(1)
    int markReleasedIfLocked(@Param("reservationId") Long reservationId,
                             @Param("now") LocalDateTime now);
    // 对应 XML: <select id="countReservations">
    Long countReservations(@Param("orderNo") String orderNo,
                           @Param("skuId") Long skuId,
                           @Param("warehouseId") Long warehouseId,
                           @Param("status") Integer status,
                           @Param("expireAtStart") LocalDateTime expireAtStart,
                           @Param("expireAtEnd") LocalDateTime expireAtEnd);

    // 对应 XML: <select id="selectReservationsPage">
    List<StockReservation> selectReservationsPage(@Param("orderNo") String orderNo,
                                                  @Param("skuId") Long skuId,
                                                  @Param("warehouseId") Long warehouseId,
                                                  @Param("status") Integer status,
                                                  @Param("expireAtStart") LocalDateTime expireAtStart,
                                                  @Param("expireAtEnd") LocalDateTime expireAtEnd,
                                                  @Param("offset") Integer offset,
                                                  @Param("limit") Integer limit);
}
