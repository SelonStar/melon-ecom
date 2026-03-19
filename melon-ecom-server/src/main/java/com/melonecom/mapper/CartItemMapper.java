package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    /** 购物车列表（用于购物车页面展示） */
    List<CartItem> selectByUserId(@Param("userId") Long userId);

    /** 查单条：同一用户同一 SKU 唯一 */
    CartItem selectByUserIdAndSkuId(@Param("userId") Long userId,
                                    @Param("skuId") Long skuId);

    /**
     * 数量增量（推荐）：quantity = quantity + delta
     * 约束：delta 可正可负；由上层保证 quantity 最终 >= 1
     */
    int incrQuantity(@Param("userId") Long userId,
                     @Param("skuId") Long skuId,
                     @Param("delta") Integer delta);

    /** 直接设置数量：quantity = newQuantity（由上层保证 >= 1） */
    int setQuantity(@Param("userId") Long userId,
                    @Param("skuId") Long skuId,
                    @Param("quantity") Integer quantity);

    /** 设置单个 SKU 勾选状态：1/0 */
    int setChecked(@Param("userId") Long userId,
                   @Param("skuId") Long skuId,
                   @Param("checked") Integer checked);

    /** 全选/全不选 */
    int setCheckedAll(@Param("userId") Long userId,
                      @Param("checked") Integer checked);

    /** 删除单个 SKU */
    int deleteByUserIdAndSkuId(@Param("userId") Long userId,
                               @Param("skuId") Long skuId);

    /** 批量删除多个 SKU */
    int deleteByUserIdAndSkuIds(@Param("userId") Long userId,
                                @Param("skuIds") List<Long> skuIds);

    /** 清空购物车 */
    int clearByUserId(@Param("userId") Long userId);

    /** 查询勾选项（下单前用） */
    List<CartItem> selectCheckedByUserId(@Param("userId") Long userId);
}
