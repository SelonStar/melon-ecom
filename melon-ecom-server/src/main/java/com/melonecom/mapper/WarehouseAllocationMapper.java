package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.WarehouseAllocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WarehouseAllocationMapper extends BaseMapper<WarehouseAllocation> {

    // 对应 XML: <select id="countAllocations">
    Long countAllocations(@Param("orderNo") String orderNo,
                          @Param("skuId") Long skuId,
                          @Param("warehouseId") Long warehouseId);

    // 对应 XML: <select id="selectAllocationsPage">
    List<WarehouseAllocation> selectAllocationsPage(@Param("orderNo") String orderNo,
                                                    @Param("skuId") Long skuId,
                                                    @Param("warehouseId") Long warehouseId,
                                                    @Param("offset") Integer offset,
                                                    @Param("limit") Integer limit);
}
