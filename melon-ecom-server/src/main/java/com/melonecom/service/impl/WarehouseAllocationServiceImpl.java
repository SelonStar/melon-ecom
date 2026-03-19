package com.melonecom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melonecom.mapper.WarehouseAllocationMapper;
import com.melonecom.model.dto.WarehouseAllocationSearchDTO;
import com.melonecom.model.entity.WarehouseAllocation;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IWarehouseAllocationService;
import org.springframework.stereotype.Service;

@Service
public class WarehouseAllocationServiceImpl
        extends ServiceImpl<WarehouseAllocationMapper, WarehouseAllocation>
        implements IWarehouseAllocationService {

    @Override
    public Result<PageResult<WarehouseAllocation>> getAllAllocations(WarehouseAllocationSearchDTO dto) {
        // TODO: 分页查询 allocation（orderNo/skuId/warehouseId）
        return Result.success(new PageResult<>());
    }
}
