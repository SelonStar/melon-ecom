package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {

    // 对应 XML: <select id="countWarehouses">
    Long countWarehouses(@Param("status") Integer status);

    // 对应 XML: <select id="selectWarehousesPage">
    List<Warehouse> selectWarehousesPage(@Param("status") Integer status,
                                         @Param("keyword") String keyword,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

    // 对应 XML: <insert id="insertWarehouse">
    int insertWarehouse(Warehouse warehouse);

    // 对应 XML: <update id="updateWarehouseById">
    int updateWarehouseById(Warehouse warehouse);

    // 对应 XML: <update id="updateWarehouseStatus">
    int updateWarehouseStatus(@Param("warehouseId") Long warehouseId,
                              @Param("status") Integer status);

    // 对应 XML: <delete id="deleteWarehouseById">
    int deleteWarehouseById(@Param("warehouseId") Long warehouseId);

    // 对应 XML: <select id="existsByCode">
    Integer existsByCode(@Param("code") String code,
                         @Param("excludeId") Long excludeId);
}
