package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /** 按订单ID查询订单项（订单详情页） */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /** 批量插入订单项（下单时用，性能更好） */
    int batchInsert(@Param("items") List<OrderItem> items);

    /** 按订单ID删除订单项（取消订单/回滚用） */
    int deleteByOrderId(@Param("orderId") Long orderId);
}
