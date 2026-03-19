package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {
     Stock selectByWarehouseAndSku(@Param("warehouseId") Long warehouseId,
                                  @Param("skuId") Long skuId);

    // 新增：同时更新 available 和 locked（乐观锁 + 不允许负数）
    int updateAvailableAndLockedWithOptimisticLock(@Param("stockId") Long stockId,
                                                   @Param("version") Integer version,
                                                   @Param("deltaAvailable") Integer deltaAvailable,
                                                   @Param("deltaLocked") Integer deltaLocked);
    // 对应 XML: <select id="countStocks">
    Long countStocks(@Param("skuId") Long skuId,
                     @Param("warehouseId") Long warehouseId);

    // 对应 XML: <select id="selectStocksPage">
    List<Stock> selectStocksPage(@Param("skuId") Long skuId,
                                 @Param("warehouseId") Long warehouseId,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    // // 对应 XML: <select id="selectByWarehouseAndSku">
    // Stock selectByWarehouseAndSku(@Param("warehouseId") Long warehouseId,
    //                               @Param("skuId") Long skuId);

    // 对应 XML: <update id="updateAvailableWithOptimisticLock">
    // 乐观锁 + 不允许负库存（available + delta >= 0）
    int updateAvailableWithOptimisticLock(@Param("stockId") Long stockId,
                                          @Param("version") Integer version,
                                          @Param("delta") Integer delta);

    // 对应 XML: <insert id="insertStock">
    int insertStock(Stock stock);

    Long selectWarehouseIdBySkuId(@Param("skuId") Long skuId);

    int decreaseAvailableByWarehouseAndSku(@Param("warehouseId") Long warehouseId,
                                           @Param("skuId") Long skuId,
                                           @Param("quantity") Integer quantity);

}
