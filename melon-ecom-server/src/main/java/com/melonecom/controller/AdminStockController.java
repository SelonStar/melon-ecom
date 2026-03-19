package com.melonecom.controller;

import com.melonecom.model.dto.*;
import com.melonecom.model.entity.Stock;
import com.melonecom.model.entity.StockReservation;
import com.melonecom.model.entity.StockTxn;
import com.melonecom.model.entity.Warehouse;
import com.melonecom.model.entity.WarehouseAllocation;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 多仓/库存相关后台接口（从 AdminController 拆分出来）
 */
@RestController
@RequestMapping("/admin/stock")
public class AdminStockController {

    // tb_warehouse
    @Autowired
    private IWarehouseService warehouseService;

    // tb_stock（仓库×sku汇总库存）
    @Autowired
    private IStockService stockService;

    // tb_stock_reservation（订单锁定记录）
    @Autowired
    private IStockReservationService stockReservationService;

    // tb_warehouse_allocation（订单分仓结果）
    @Autowired
    private IWarehouseAllocationService warehouseAllocationService;

    // tb_stock_txn（库存流水）
    @Autowired
    private IStockTxnService stockTxnService;

    // ======================================================================
    // A) 仓库管理（tb_warehouse）
    // ======================================================================

    /**
     * 获取仓库总数
     */
    @GetMapping("/getAllWarehousesCount")
    public Result<Long> getAllWarehousesCount(@RequestParam(required = false) Integer status) {
        return warehouseService.getAllWarehousesCount(status);
    }

    /**
     * 获取仓库列表（分页+筛选）
     */
    @PostMapping("/getAllWarehouses")
    public Result<PageResult<Warehouse>> getAllWarehouses(@RequestBody WarehouseSearchDTO warehouseSearchDTO) {
        return warehouseService.getAllWarehouses(warehouseSearchDTO);
    }

    /**
     * 新增仓库
     */
    @PostMapping("/addWarehouse")
    public Result<?> addWarehouse(@RequestBody WarehouseAddDTO warehouseAddDTO) {
        return warehouseService.addWarehouse(warehouseAddDTO);
    }

    /**
     * 更新仓库信息
     */
    @PutMapping("/updateWarehouse")
    public Result<?> updateWarehouse(@RequestBody WarehouseUpdateDTO warehouseUpdateDTO) {
        return warehouseService.updateWarehouse(warehouseUpdateDTO);
    }

    /**
     * 启用/停用仓库
     */
    @PatchMapping("/updateWarehouseStatus/{id}")
    public Result<?> updateWarehouseStatus(@PathVariable("id") Long warehouseId,
                                           @RequestParam("status") Integer status) {
        return warehouseService.updateWarehouseStatus(warehouseId, status);
    }

    /**
     * 删除仓库（谨慎：通常需要校验该仓是否还有库存）
     */
    @DeleteMapping("/deleteWarehouse/{id}")
    public Result<?> deleteWarehouse(@PathVariable("id") Long warehouseId) {
        return warehouseService.deleteWarehouse(warehouseId);
    }

    // ======================================================================
    // B) 库存管理（tb_stock + 库存调整写 tb_stock_txn）
    // ======================================================================

    /**
     * 获取库存记录总数（可按 sku / 仓库过滤）
     */
    @GetMapping("/getAllStocksCount")
    public Result<Long> getAllStocksCount(@RequestParam(required = false) Long skuId,
                                          @RequestParam(required = false) Long warehouseId) {
        return stockService.getAllStocksCount(skuId, warehouseId);
    }

    /**
     * 获取库存列表（分页+筛选：skuId/warehouseId）
     */
    @PostMapping("/getAllStocks")
    public Result<PageResult<Stock>> getAllStocks(@RequestBody StockSearchDTO stockSearchDTO) {
        return stockService.getAllStocks(stockSearchDTO);
    }

    /**
     * 库存调整（入库/盘点纠错/人工修正）
     * 典型行为：更新 tb_stock，同时写一条 tb_stock_txn(ADJUST/INBOUND/OUTBOUND)
     */
    @PutMapping("/adjustStock")
    public Result<?> adjustStock(@RequestBody StockAdjustDTO stockAdjustDTO) {
        return stockService.adjustStock(stockAdjustDTO);
    }

    // ======================================================================
    // C) 订单锁定记录管理（tb_stock_reservation）
    // ======================================================================

    /**
     * 查询锁定记录（用于排查：哪些订单占了库存、是否超时未释放）
     */
    @PostMapping("/getAllStockReservations")
    public Result<PageResult<StockReservation>> getAllStockReservations(
            @RequestBody StockReservationSearchDTO dto) {
        return stockReservationService.getAllReservations(dto);
    }

    // ======================================================================
    // D) 订单分仓结果管理（tb_warehouse_allocation）
    // ======================================================================

    /**
     * 查询分仓结果（订单每个 sku 由哪个仓发）
     */
    @PostMapping("/getAllWarehouseAllocations")
    public Result<PageResult<WarehouseAllocation>> getAllWarehouseAllocations(
            @RequestBody WarehouseAllocationSearchDTO dto) {
        return warehouseAllocationService.getAllAllocations(dto);
    }

    // ======================================================================
    // E) 库存流水管理（tb_stock_txn）
    // ======================================================================

    /**
     * 查询库存流水（审计/对账/排查库存异常）
     */
    @PostMapping("/getAllStockTxns")
    public Result<PageResult<StockTxn>> getAllStockTxns(@RequestBody StockTxnSearchDTO dto) {
        return stockTxnService.getAllTxns(dto);
    }
}
