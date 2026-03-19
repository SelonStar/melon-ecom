package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.StockTxn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StockTxnMapper extends BaseMapper<StockTxn> {

    // 对应 XML: <select id="countTxns">
    Long countTxns(@Param("bizType") Integer bizType,
                   @Param("bizNo") String bizNo,
                   @Param("skuId") Long skuId,
                   @Param("warehouseId") Long warehouseId,
                   @Param("timeStart") LocalDateTime timeStart,
                   @Param("timeEnd") LocalDateTime timeEnd);

    // 对应 XML: <select id="selectTxnsPage">
    List<StockTxn> selectTxnsPage(@Param("bizType") Integer bizType,
                                  @Param("bizNo") String bizNo,
                                  @Param("skuId") Long skuId,
                                  @Param("warehouseId") Long warehouseId,
                                  @Param("timeStart") LocalDateTime timeStart,
                                  @Param("timeEnd") LocalDateTime timeEnd,
                                  @Param("offset") Integer offset,
                                  @Param("limit") Integer limit);

    // 对应 XML: <insert id="insertTxn">
    int insertTxn(StockTxn txn);
}
