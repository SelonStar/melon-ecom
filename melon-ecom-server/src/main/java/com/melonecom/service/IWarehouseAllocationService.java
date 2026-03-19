package com.melonecom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melonecom.model.dto.WarehouseAllocationSearchDTO;
import com.melonecom.model.entity.WarehouseAllocation;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;

public interface IWarehouseAllocationService extends IService<WarehouseAllocation> {

    Result<PageResult<WarehouseAllocation>> getAllAllocations(WarehouseAllocationSearchDTO dto);
}
