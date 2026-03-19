package com.melonecom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melonecom.mapper.WarehouseMapper;
import com.melonecom.model.dto.WarehouseAddDTO;
import com.melonecom.model.dto.WarehouseSearchDTO;
import com.melonecom.model.dto.WarehouseUpdateDTO;
import com.melonecom.model.entity.Warehouse;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IWarehouseService;
import org.springframework.stereotype.Service;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements IWarehouseService {

    @Override
    public Result<Long> getAllWarehousesCount(Integer status) {
        // TODO: 根据 status 统计数量
        return Result.success(0L);
    }

    @Override
    public Result<PageResult<Warehouse>> getAllWarehouses(WarehouseSearchDTO dto) {
        // TODO: 分页+条件查询
        return Result.success(new PageResult<>());
    }

    @Override
    public Result<?> addWarehouse(WarehouseAddDTO dto) {
        // TODO: code 唯一性校验 -> insert
        return Result.success(null);
    }

    @Override
    public Result<?> updateWarehouse(WarehouseUpdateDTO dto) {
        // TODO: update
        return Result.success(null);
    }

    @Override
    public Result<?> updateWarehouseStatus(Long warehouseId, Integer status) {
        // TODO: update status
        return Result.success(null);
    }

    @Override
    public Result<?> deleteWarehouse(Long warehouseId) {
        // TODO: 删除前最好校验该仓是否还有库存记录/可售/锁定
        return Result.success(null);
    }
}
