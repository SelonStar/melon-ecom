package com.melonecom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melonecom.model.dto.WarehouseAddDTO;
import com.melonecom.model.dto.WarehouseSearchDTO;
import com.melonecom.model.dto.WarehouseUpdateDTO;
import com.melonecom.model.entity.Warehouse;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;

public interface IWarehouseService extends IService<Warehouse> {

    Result<Long> getAllWarehousesCount(Integer status);

    Result<PageResult<Warehouse>> getAllWarehouses(WarehouseSearchDTO dto);

    Result<?> addWarehouse(WarehouseAddDTO dto);

    Result<?> updateWarehouse(WarehouseUpdateDTO dto);

    Result<?> updateWarehouseStatus(Long warehouseId, Integer status);

    Result<?> deleteWarehouse(Long warehouseId);
}
